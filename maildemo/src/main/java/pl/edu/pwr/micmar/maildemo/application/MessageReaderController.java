package pl.edu.pwr.micmar.maildemo.application;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import javax.mail.Message;

public class MessageReaderController {
    @FXML
    protected WebView messageViewer;
    @FXML
    protected VBox readerWindow;

    @FXML
    void initialize() {
        messageViewer.prefHeightProperty().bind(readerWindow.heightProperty().multiply(0.9));
        messageViewer.getEngine().setUserAgent("MyApp Web Browser 1.0");
    }
}