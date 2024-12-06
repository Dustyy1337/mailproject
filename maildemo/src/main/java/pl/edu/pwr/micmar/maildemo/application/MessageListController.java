package pl.edu.pwr.micmar.maildemo.application;


import ai.djl.inference.Predictor;
import ai.djl.translate.TranslateException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import pl.edu.pwr.micmar.maildemo.mail.MessageVectorStore;
import pl.edu.pwr.micmar.maildemo.mail.MimeMessageReader;
import pl.edu.pwr.micmar.maildemo.mail.SessionCollector;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageListController {
    @FXML
    TableView<MessageView> messageTable;
    @FXML
    TableColumn<MessageView, String> dateColumn, headerColumn, senderColumn;
    @FXML
    Pagination messagePaginator;
    @FXML
    TextField searchBar;
    @FXML
    TreeView<String> inboxNames;

    Predictor<String, float[]> predictor;

    ObservableList<MessageView> messageList = FXCollections.observableArrayList();

    protected Store store;
    protected MessageVectorStore messageVectorStore = new MessageVectorStore();
    protected Map<String, MessageVectorStore> embeddedStores = new HashMap<>();

    void changePage() throws MessagingException {
        int beginIndex = messageVectorStore.getMessageVectors().size() - 1 - (20 * messagePaginator.getCurrentPageIndex());
        int endIndex = beginIndex - 20;
        messageList.clear();
        for (int i = beginIndex; i >= endIndex; i--) {
            Message message = messageVectorStore.getMessageVectors().get(i).getMessage();
            messageList.add(new MessageView(message.getReceivedDate().toString(), message.getSubject(), message.getFrom()[0].toString(), message.getMessageNumber(), 0));
        }
    }
    private static double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    @FXML
    void search() {
        new Thread(new EmbeddClass()).start();
    }

    @FXML
    void initialize() throws MessagingException, IOException {
        predictor = Application.model.newPredictor();
        dateColumn.maxWidthProperty().bind(messageTable.widthProperty().multiply(0.2));
        headerColumn.maxWidthProperty().bind(messageTable.widthProperty().multiply(0.6));
        senderColumn.maxWidthProperty().bind(messageTable.widthProperty().multiply(0.2));
        store = SessionCollector.sessions.get(0).getStore("imap");
        Folder[] folders = store.getDefaultFolder().list();
        TreeItem<String> rootItem = new TreeItem<>("Messages");
        if (folders.length == 1) {
            rootItem = new TreeItem<>("INBOX");
        } else {
            List<TreeItem<String>> mainFolders = new ArrayList<>();
            for (Folder folder : folders) {
                if (!folder.getName().equals("INBOX")) {
                    TreeItem<String> folderRoot = new TreeItem<>(folder.getName());
                    Folder[] subfolders = folder.list();
                    for (Folder subfolder : subfolders) {
                        folderRoot.getChildren().add(new TreeItem<>(subfolder.getName()));
                    }
                    mainFolders.add(folderRoot);
                }
            }
            rootItem.getChildren().addAll(mainFolders);
        }
        inboxNames.setRoot(rootItem);
        //new Thread(new CachingClass(store.getFolder("INBOX"))).start();
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("timeDate"));
        headerColumn.setCellValueFactory(new PropertyValueFactory<>("header"));
        senderColumn.setCellValueFactory(new PropertyValueFactory<>("sender"));
        inboxNames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.getChildren().isEmpty()) {
                Folder selectedFolder;
                try {
                    if (newValue.getParent() == null) selectedFolder = store.getFolder(newValue.getValue());
                    else selectedFolder = store.getFolder(newValue.getParent().getValue()).getFolder(newValue.getValue());
                    selectedFolder.open(Folder.READ_ONLY);
                    setMessageList(selectedFolder);
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Selected node: " + newValue.getValue());
            }
        });
        messagePaginator.currentPageIndexProperty().addListener((obs) -> {
            try {
                changePage();
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        });
        messageTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("message-reader.fxml"));
                Parent root = fxmlLoader.load();
                MessageReaderController controller = fxmlLoader.getController();
                controller.messageViewer.getEngine().loadContent(MimeMessageReader.getTextFromMessage(messageVectorStore.getMessageVectors().get(newSelection.getMessageIndex() - 1).getMessage(), true), "text/html");
                Stage stage = new Stage();
                stage.setTitle("Second Window");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException | MessagingException e) {
                e.printStackTrace();
            }
        });
    }

    void setMessageList(Folder selectedFolder) throws MessagingException {
        messageList.clear();
        messageVectorStore = new MessageVectorStore();
        Message[] messages = selectedFolder.getMessages();
        FetchProfile fetchProfile = new FetchProfile();
        fetchProfile.add(FetchProfile.Item.CONTENT_INFO);
        selectedFolder.fetch(messages, fetchProfile);
        for (Message message : messages) {
            messageVectorStore.addMessageVector(message, 0); // Assuming vector is null for now
        }
        int pages = messages.length / 20;
        messagePaginator.setPageCount(pages);
        messagePaginator.setCurrentPageIndex(0);
        for (int i = messages.length - 1; i >= messages.length - 20; i--) {
            messageList.add(new MessageView(messages[i].getReceivedDate().toString(), messages[i].getSubject(), messages[i].getFrom()[0].toString(), messages[i].getMessageNumber(), 0));
        }
        messageTable.setItems(messageList);
    }
    class EmbeddClass implements Runnable {
        @Override
        public void run() {
            messageList.clear();
            String searchQuery = searchBar.getText();
            float[] searchEmbedding;
            try {
                searchEmbedding = predictor.predict(searchQuery);
                System.out.println("Search embedding calculated for query: " + searchQuery);
            } catch (TranslateException e) {
                throw new RuntimeException(e);
            }
            try {
                Folder folder = store.getFolder("INBOX");
                folder.open(Folder.READ_ONLY);
                Message[] messages = folder.getMessages();
                System.out.println("Number of messages in INBOX: " + messages.length);
                File directory = new File("cache/" + SessionCollector.sessions.get(0).username);
                if (directory.exists() && directory.isDirectory()) {
                    File[] files = directory.listFiles();
                    if (files != null) {
                        System.out.println("Number of files in cache: " + files.length);
                        for (File file : files) {
                            String messageId = file.getName();
                            for (Message message : messages) {
                                if (message instanceof MimeMessage && ((MimeMessage) message).getMessageID().replaceAll("/", "_").equals(messageId)) {
                                    String fileContent = Files.readString(Paths.get(file.getPath()));
                                    float[] embedding = predictor.predict(fileContent);
                                    double similarity = cosineSimilarity(searchEmbedding, embedding);
                                    // Add the message to the message list or perform any other required action
                                    messageList.add(new MessageView(
                                            message.getReceivedDate().toString(),
                                            message.getSubject(),
                                            message.getFrom()[0].toString(),
                                            message.getMessageNumber(),
                                            similarity
                                    ));
                                    FXCollections.sort(messageList, (m1, m2) -> Double.compare(m2.similarity, m1.similarity));
                                    messageTable.setItems(messageList);
                                    System.out.println("Message embedded");
                                }
                                else System.out.println("Message not embedded");
                            }
                        }
                    } else {
                        System.out.println("No files found in cache directory.");
                    }
                } else {
                    System.out.println("Cache directory does not exist or is not a directory.");
                }
            } catch (MessagingException | IOException | TranslateException e) {
                throw new RuntimeException(e);
            }
        }
    }
    class CachingClass implements Runnable {
        private final Folder folder;

        CachingClass(Folder folder) {
            this.folder = folder;
        }

        @Override
        public void run() {
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            try {
                folder.open(Folder.READ_ONLY);
                Message[] messages = folder.getMessages();
                List<Future<Void>> futures = new ArrayList<>();
                int totalMessages = messages.length;
                AtomicInteger processedMessages = new AtomicInteger(0);

                for (Message message : messages) {
                    futures.add(executor.submit(new MessageDownloader((MimeMessage) message, processedMessages, totalMessages)));
                }

                for (Future<Void> future : futures) {
                    future.get(); // Wait for all tasks to complete
                }

                System.out.println("All messages downloaded from " + folder.getName());
            } catch (MessagingException | InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } finally {
                executor.shutdown();
            }
        }

        class MessageDownloader implements Callable<Void> {
            private final MimeMessage mimeMessage;
            private final AtomicInteger processedMessages;
            private final int totalMessages;

            MessageDownloader(MimeMessage mimeMessage, AtomicInteger processedMessages, int totalMessages) {
                this.mimeMessage = mimeMessage;
                this.processedMessages = processedMessages;
                this.totalMessages = totalMessages;
            }

            @Override
            public Void call() throws Exception {
                File directory = new File("cache/" + SessionCollector.sessions.get(0).username);
                if (!directory.exists()) directory.mkdirs();
                String messageId = mimeMessage.getMessageID().replaceAll("/", "_");
                File messageFile = new File(directory, messageId + ".eml");
                if (!messageFile.exists()) {
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(messageFile))) {
                        String messageContent = MimeMessageReader.getTextFromMessage(mimeMessage, false);
                        bos.write(messageContent.getBytes());
                    }
                }
                int processed = processedMessages.incrementAndGet();
                System.out.println("Processed messages: " + processed + "/" + totalMessages);
                return null;
            }
        }
    }
}
class SearchSimilarity {
    Message message;
    double similarity;
    SearchSimilarity(Message message, double similarity) {
        this.message = message;
        this.similarity = similarity;
    }
}
