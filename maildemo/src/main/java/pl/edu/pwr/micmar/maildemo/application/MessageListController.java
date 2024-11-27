package pl.edu.pwr.micmar.maildemo.application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import pl.edu.pwr.micmar.maildemo.mail.MimeMessageReader;
import pl.edu.pwr.micmar.maildemo.mail.SessionCollector;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import java.io.IOException;

public class MessageListController {
    @FXML
    TableView<MessageView> messageTable;
    @FXML
    TableColumn<MessageView, String> dateColumn, headerColumn, senderColumn;
    @FXML
    Pagination messagePaginator;
    @FXML
    TextField searchBar;


    ObservableList<MessageView> messageList = FXCollections.observableArrayList();
    ObservableList<MessageView> searchList;

    protected Store store;
    protected Message[] messages;

    void changePage() throws MessagingException {
        int beginIndex = messages.length - 1 - (20 * messagePaginator.getCurrentPageIndex());
        int endIndex = beginIndex - 20;
        messageList.clear();
        for(int i = beginIndex; i>=endIndex; i--) {
            messageList.add(new MessageView(messages[i].getReceivedDate().toString(), messages[i].getSubject(), messages[i].getFrom()[0].toString(), messages[i].getMessageNumber()));
        }
    }

    @FXML
    void initialize() throws MessagingException, IOException {
            dateColumn.maxWidthProperty().bind(messageTable.widthProperty().multiply(0.2));
            headerColumn.maxWidthProperty().bind(messageTable.widthProperty().multiply(0.6));
            senderColumn.maxWidthProperty().bind(messageTable.widthProperty().multiply(0.2));
            store = SessionCollector.sessions.get(0).getStore("imap");
            Folder folder = store.getFolder("[Gmail]");
            for (Folder subfolder : folder.list()) {
                System.out.println(subfolder.getName());
            }
            Folder inbox = folder.getFolder("Wszystkie");
            inbox.open(Folder.READ_ONLY);
            messages = inbox.getMessages();
            int pages = messages.length / 20;
            messagePaginator.setPageCount(pages);
            for (int i = messages.length - 1; i >= messages.length - 20; i--) {
                messageList.add(new MessageView(messages[i].getReceivedDate().toString(), messages[i].getSubject(), messages[i].getFrom()[0].toString(), messages[i].getMessageNumber()));
            }
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("timeDate"));
            headerColumn.setCellValueFactory(new PropertyValueFactory<>("header"));
            senderColumn.setCellValueFactory(new PropertyValueFactory<>("sender"));
            messageTable.setItems(messageList);
            messagePaginator.currentPageIndexProperty().addListener((obs) -> {
                try {
                    changePage();
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            });
            messageTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                //System.out.println(newSelection.messageIndex);
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("message-reader.fxml"));
                    Parent root = fxmlLoader.load();
                    MessageReaderController controller = fxmlLoader.getController();
                    controller.messageViewer.getEngine().loadContent(MimeMessageReader.getTextFromMessage(inbox.getMessage(newSelection.messageIndex), true), "text/html");
                    Stage stage = new Stage();
                    stage.setTitle("Second Window");
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException | MessagingException e) {
                    e.printStackTrace();
                }

            });


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
