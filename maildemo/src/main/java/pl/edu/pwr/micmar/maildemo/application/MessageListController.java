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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import pl.edu.pwr.micmar.maildemo.db.SQLiteConnection;
import pl.edu.pwr.micmar.maildemo.mail.MessageVectorStore;
import pl.edu.pwr.micmar.maildemo.mail.MimeMessageReader;
import pl.edu.pwr.micmar.maildemo.mail.SessionCollector;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
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
    @FXML
    ChoiceBox<String> selectUser;
    Thread embedding = new Thread();

    Predictor<String, float[]> predictor;

    ObservableList<MessageView> messageList = FXCollections.observableArrayList();

    private int currentUserId;

    protected Store store;
    protected MessageVectorStore messageVectorStore = new MessageVectorStore();

    void changePage() throws MessagingException {
        int beginIndex = messageVectorStore.getMessageVectors().size() - 1 - (20 * messagePaginator.getCurrentPageIndex());
        int endIndex = beginIndex - 20;
        messageList.clear();
        for (int i = beginIndex; i >= endIndex; i--) {
            Message message = messageVectorStore.getMessageVectors().get(i).getMessage();
            messageList.add(new MessageView(message.getReceivedDate().toString(), message.getSubject(), message.getFrom()[0].toString(), message.getMessageNumber(), 0, message));
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
    void search() throws MessagingException {
        if(embedding.isAlive()) {
            embedding.interrupt();
        }
        embedding = new Thread(new EmbeddClass(currentUserId, store.getFolder("INBOX")));
        embedding.start();

        messagePaginator.setVisible(false);
    }
    private void addTreeItem(TreeItem<String> root, Folder folder) {
        TreeItem<String> folderRoot;
        if(folder.getName().isEmpty()) folderRoot = root;
        else{
            folderRoot = new TreeItem<>(folder.getName());
            root.getChildren().add(folderRoot);
        }
        try {
            Folder[] subfolders = folder.list();
            for (Folder subfolder : subfolders) {
                addTreeItem(folderRoot, subfolder);
            }
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
    private Folder getSelectedFolder(TreeItem<String> node) throws MessagingException {
        if (node.getParent() == null || Objects.equals(node.getParent().getValue(), "Messages")) {
            return store.getFolder(node.getValue());
        } else {
            return getSelectedFolder(node.getParent()).getFolder(node.getValue());
        }
    }
    @FXML
    protected void addNewUser() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-view.fxml"));
        Stage loginStage = new Stage();
        loginStage.setTitle("Login");
        loginStage.setScene(new Scene(fxmlLoader.load()));
        loginStage.show();
    }
    private void loadNewUser() throws MessagingException {
        currentUserId = selectUser.getSelectionModel().getSelectedIndex();
        store = SessionCollector.sessions.get(currentUserId).imapStore;
        TreeItem<String> rootItem = new TreeItem<>("Messages");
        addTreeItem(rootItem, store.getDefaultFolder());
        inboxNames.setRoot(rootItem);
        if(embedding.isAlive()) embedding.interrupt();
        messageTable.getItems().clear();
    }
    public void initiateNewUser() throws MessagingException {
        selectUser.getSelectionModel().select(selectUser.getItems().size()-1);
        loadNewUser();
        new CachingClass(store.getFolder("INBOX"), currentUserId).run();
    }
    @FXML
    void initialize() throws MessagingException, IOException {
        currentUserId = 0;
        selectUser.getItems().add(SessionCollector.sessions.get(currentUserId).username);
        selectUser.getSelectionModel().select(0);
        predictor = Application.model.newPredictor();
        dateColumn.maxWidthProperty().bind(messageTable.widthProperty().multiply(0.2));
        headerColumn.maxWidthProperty().bind(messageTable.widthProperty().multiply(0.6));
        senderColumn.maxWidthProperty().bind(messageTable.widthProperty().multiply(0.2));
        store = SessionCollector.sessions.get(currentUserId).imapStore;
        TreeItem<String> rootItem = new TreeItem<>("Messages");
        addTreeItem(rootItem, store.getDefaultFolder());
        inboxNames.setRoot(rootItem);
        new Thread(new CachingClass(store.getFolder("INBOX"), currentUserId)).start();
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("timeDate"));
        headerColumn.setCellValueFactory(new PropertyValueFactory<>("header"));
        senderColumn.setCellValueFactory(new PropertyValueFactory<>("sender"));
        inboxNames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.getChildren().isEmpty()) {
                Folder selectedFolder;
                if(embedding.isAlive()) embedding.interrupt();
                try {
                    selectedFolder = getSelectedFolder(newValue);
                    selectedFolder.open(Folder.READ_ONLY);
                    setMessageList(selectedFolder);
                    messagePaginator.setVisible(true);
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
                var content = MimeMessageReader.getTextFromMessage(newSelection.message, true);
                System.out.println(content);
                controller.messageViewer.getEngine().loadContent(content, "text/html");
                controller.getAttachments(MimeMessageReader.getAttachments(newSelection.message));
                Stage stage = new Stage();
                stage.setTitle("Second Window");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException | MessagingException e) {
                e.printStackTrace();
            }
        });
        selectUser.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            try {
                loadNewUser();
            } catch (MessagingException e) {
                throw new RuntimeException(e);
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
            messageVectorStore.addMessageVector(message, 0);
        }
        int pages = messages.length / 20;
        messagePaginator.setPageCount(pages);
        messagePaginator.setCurrentPageIndex(0);
        for (int i = messages.length - 1; i >= messages.length - 20; i--) {
            if(i<0) break;
            messageList.add(new MessageView(messages[i].getReceivedDate().toString(), messages[i].getSubject(), messages[i].getFrom()[0].toString(), messages[i].getMessageNumber(), 0, messages[i]));
        }
        messageTable.setItems(messageList);
    }
    class EmbeddClass implements Runnable {
        int userId;
        Folder folder;
        EmbeddClass(int userId, Folder folder) {
            this.userId = userId;
            this.folder = folder;
        }
        @Override
        public void run() {
            messageList.clear();
            messageVectorStore = new MessageVectorStore();
            Message[] messages;
            String searchQuery = searchBar.getText();
            float[] searchEmbedding;
            try {
                searchEmbedding = predictor.predict(searchQuery);
                System.out.println("Search embedding calculated for query: " + searchQuery);
            } catch (TranslateException e) {
                throw new RuntimeException(e);
            }
            try {
                folder.open(Folder.READ_ONLY);
                messages = folder.getMessages();
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            String sql = "SELECT mimeID, content FROM messages WHERE username = ?";
            try (PreparedStatement preparedStatement = SQLiteConnection.connection.prepareStatement(sql)) {
                preparedStatement.setString(1, SessionCollector.sessions.get(userId).username);
                var resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String mimeID = resultSet.getString("mimeID");
                    String content = resultSet.getString("content");
                    float[] messageEmbedding = predictor.predict(content);
                    double similarity = content.isEmpty() ? 0 : cosineSimilarity(searchEmbedding, messageEmbedding);
                    if (similarity >= 0.3){
                        for (Message message : messages) {
                            if (((MimeMessage) message).getMessageID().equals(mimeID) && !Thread.currentThread().isInterrupted()) {
                                messageVectorStore.addMessageVector(message, similarity);
                                messageVectorStore.getMessageVectors().sort((o1, o2) -> Double.compare(o2.getSimilarity(), o1.getSimilarity()));
                                messageList.add(new MessageView(message.getReceivedDate().toString(), message.getSubject(), message.getFrom()[0].toString(), message.getMessageNumber(), similarity, message));
                                messageList.sort((o1, o2) -> Double.compare(o2.similarity, o1.similarity));
                                break;
                            }
                        }
                }
                }
            } catch (SQLException | TranslateException | MessagingException e) {
                throw new RuntimeException(e);
            }
        }
    }
    static class CachingClass implements Runnable {
        private final Folder folder;
        int userId;

        CachingClass(Folder folder, int userId) {
            this.folder = folder;
            this.userId = userId;
        }

        @Override
        public void run() {
            try {
                int i=0;
                folder.open(Folder.READ_ONLY);
                Message[] messages = folder.getMessages();
                List<String> mimeIDs = new ArrayList<>();
                String username = SessionCollector.sessions.get(userId).username;
                String sql = "SELECT mimeID FROM messages WHERE username = ?";
                try(PreparedStatement preparedStatement = SQLiteConnection.connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, username);
                    var resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        mimeIDs.add(resultSet.getString("mimeID"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                for (Message message : messages) {

                    String mimeID = ((MimeMessage) message).getMessageID();
                    if(mimeIDs.contains(mimeID)) {
                        i++;
                        System.out.println("Message already cached: " + i + "/" + messages.length);
                        continue;
                    }

                    String content = Jsoup.parse(MimeMessageReader.getTextFromMessage(message, false)).text();
                    if(!content.isEmpty()) content = message.getSubject() + "\n" + content;

                            // Assuming you have a method to get a database connection
                    sql = "INSERT INTO messages (username, mimeID, content) VALUES (?, ?, ?)";
                    try (PreparedStatement preparedStatement = SQLiteConnection.connection.prepareStatement(sql)) {
                        preparedStatement.setString(1, username);
                        preparedStatement.setString(2, mimeID);
                        preparedStatement.setString(3, content);
                        preparedStatement.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    i++;
                    System.out.println("Message cached: " + i + "/" + messages.length);
                }
            } catch (MessagingException | IOException e) {
                throw new RuntimeException(e);
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
