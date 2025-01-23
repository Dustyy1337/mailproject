package pl.edu.pwr.micmar.maildemo.application;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pl.edu.pwr.micmar.maildemo.mail.SessionCollector;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MessageWriterController {
    @FXML
    protected Label sender, errorText;
    @FXML
    protected TextField receivers, subject;
    @FXML
    protected HTMLEditor textEditor;
    @FXML
    protected HBox attachments;
    Multipart multipart = new MimeMultipart();
    List<MimeBodyPart> attachmentList = new ArrayList<>();
    int userID;
    String senderAddress;
    @FXML
    protected void addAttachment() throws MessagingException, IOException {
        FileChooser fileChooser = new FileChooser();

        List<File> files = fileChooser.showOpenMultipleDialog(null);
        for(File file : files) {
            Hyperlink link = new Hyperlink(file.getName());
            attachments.getChildren().add(link);
            MimeBodyPart attachment = new MimeBodyPart();
            attachment.attachFile(file);
            attachmentList.add(attachment);
            link.setOnAction(event -> {
                attachments.getChildren().remove(link);
                attachmentList.remove(attachment);
            });

        }
    }
    @FXML
    protected void send() throws MessagingException {
        if(receivers.getText().isEmpty() || subject.getText().isEmpty()) {
            Thread thread = new Thread(new ShowError("Fill all fields"));
            thread.start();
        }
        else {
            Message message = new MimeMessage(SessionCollector.sessions.get(userID).smtpSession);
            String[] receiversList = receivers.getText().split(" ");
            Address[] senders = new InternetAddress[receiversList.length];
            message.setFrom(new InternetAddress(SessionCollector.sessions.get(userID).username));
            for(int i = 0; i < receiversList.length; i++) {
                try {
                    senders[i] = new InternetAddress(receiversList[i]);
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
            message.setRecipients(Message.RecipientType.TO, senders);
            message.setSubject(subject.getText());
            MimeBodyPart textContent = new MimeBodyPart();
            textContent.setContent(textEditor.getHtmlText(), "text/html");
            multipart.addBodyPart(textContent);
            for(MimeBodyPart attachment : attachmentList) {
                multipart.addBodyPart(attachment);
            }
            message.setContent(multipart);
            try {
                Transport.send(message);
                System.out.println("Message sent");
                Stage stage = (Stage) sender.getScene().getWindow();
                stage.close();
            } catch (SendFailedException e) {
                new Thread(new ShowError("Sending failed")).start();
            }
        }
    }
    @FXML
    void initialize() {
        MessageListController controller = Application.mainController.getController();
        userID = controller.selectUser.getSelectionModel().getSelectedIndex();
        senderAddress = controller.selectUser.getValue();
        sender.setText("From: " + senderAddress);
    }
    class ShowError implements Runnable {
        String message;
        ShowError(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            errorText.setText(message);
            errorText.setVisible(true);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            errorText.setVisible(false);
        }
    }

}
