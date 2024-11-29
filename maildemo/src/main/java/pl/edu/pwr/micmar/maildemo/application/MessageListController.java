package pl.edu.pwr.micmar.maildemo.application;

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

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    ObservableList<MessageView> messageList = FXCollections.observableArrayList();
    ObservableList<MessageView> searchList = FXCollections.observableArrayList();

    protected Store store;
    protected MessageVectorStore messageVectorStore = new MessageVectorStore();

    void changePage() throws MessagingException {
        int beginIndex = messageVectorStore.getMessageVectors().size() - 1 - (20 * messagePaginator.getCurrentPageIndex());
        int endIndex = beginIndex - 20;
        messageList.clear();
        for (int i = beginIndex; i >= endIndex; i--) {
            Message message = messageVectorStore.getMessageVectors().get(i).getMessage();
            messageList.add(new MessageView(message.getReceivedDate().toString(), message.getSubject(), message.getFrom()[0].toString(), message.getMessageNumber()));
        }
    }

    @FXML
    void initialize() throws MessagingException, IOException {
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
        for (Message message : messages) {
            messageVectorStore.addMessageVector(message, null); // Assuming vector is null for now
        }
        int pages = messages.length / 20;
        messagePaginator.setPageCount(pages);
        messagePaginator.setCurrentPageIndex(0);
        for (int i = messages.length - 1; i >= messages.length - 20; i--) {
            messageList.add(new MessageView(messages[i].getReceivedDate().toString(), messages[i].getSubject(), messages[i].getFrom()[0].toString(), messages[i].getMessageNumber()));
        }
        messageTable.setItems(messageList);
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
