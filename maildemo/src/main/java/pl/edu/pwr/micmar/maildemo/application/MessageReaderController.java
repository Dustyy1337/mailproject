package pl.edu.pwr.micmar.maildemo.application;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import pl.edu.pwr.micmar.maildemo.mail.MimeMessageReader;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageReaderController {
    @FXML
    protected WebView messageViewer;
    @FXML
    protected VBox readerWindow;
    @FXML
    protected HBox attachmentBox;
    protected Map<BodyPart, Hyperlink> attachments = new HashMap<>();

    @FXML
    void initialize() {
        messageViewer.prefHeightProperty().bind(readerWindow.heightProperty().multiply(0.6));
        messageViewer.getEngine().setUserAgent("MyApp Web Browser 1.0");
        WebEngine webEngine = messageViewer.getEngine();
    }
    public void getAttachments(List<BodyPart> attachments) throws MessagingException {
        for (BodyPart attachment : attachments) {
            Hyperlink attachmentLink = new Hyperlink(attachment.getFileName());
            attachmentLink.setOnAction(event -> {
                Stage stage = (Stage) attachmentLink.getScene().getWindow();
                try {
                    MimeMessageReader.saveAttachment(attachment, stage);
                } catch (IOException | MessagingException e) {
                    throw new RuntimeException(e);
                }
            });
            this.attachments.put(attachment, attachmentLink);
            attachmentBox.getChildren().add(attachmentLink);
        }
    }
}