package pl.edu.pwr.micmar.maildemo.application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pl.edu.pwr.micmar.maildemo.mail.EmailReader;
import pl.edu.pwr.micmar.maildemo.mail.GmailOauth2;
import pl.edu.pwr.micmar.maildemo.mail.SessionCollector;

import javax.mail.Session;

public class LoginController {
    @FXML
    private TextField emailAddress;
    @FXML
    private PasswordField password;
    @FXML
    private CheckBox checkBox;
    @FXML
    private Button loginButton;


    @FXML
    protected void loginAuth() throws Exception {
        GmailOauth2.getAccessToken("mchlmarczak@gmail.com");
        int size = SessionCollector.sessions.size();
        /*while(SessionCollector.sessions.isEmpty()) {
            // wait
            System.out.println(SessionCollector.sessions.size());
        }*/
        System.out.println("Połączono pomyślnie");
        FXMLLoader fxmlLoader = new FXMLLoader(LoginController.class.getResource("message-list.fxml"));
        Stage stage = (Stage) emailAddress.getScene().getWindow();
        stage.setScene(new Scene(fxmlLoader.load()));
    }

}