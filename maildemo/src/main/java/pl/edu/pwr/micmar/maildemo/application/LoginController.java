package pl.edu.pwr.micmar.maildemo.application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pl.edu.pwr.micmar.maildemo.db.SQLiteConnection;
import pl.edu.pwr.micmar.maildemo.mail.EmailReader;
import pl.edu.pwr.micmar.maildemo.mail.GmailOauth2;
import pl.edu.pwr.micmar.maildemo.mail.SessionCollector;

import javax.mail.Session;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class LoginController {
    @FXML
    private TextField emailAddress;
    @FXML
    private PasswordField password;
    @FXML
    private Button loginButton;
    @FXML
    private Button backButton;
    private String imapServer = "";


    @FXML
    protected void loginAuth()  {
        try {
            String login = emailAddress.getText();
            if (Objects.equals(loginButton.getText(), "Login")) {
                loginButton.setDisable(true);
                backButton.setDisable(true);
                password.setEditable(false);
                EmailReader.connectToMail(imapServer, login, password.getText(), "", 0);
                System.out.println("Połączono pomyślnie");
                Stage stage = (Stage) emailAddress.getScene().getWindow();
                if(Application.mainController == null) {
                    Application.mainController = new FXMLLoader(LoginController.class.getResource("message-list.fxml"));
                    stage = (Stage) emailAddress.getScene().getWindow();
                    stage.setScene(new Scene(Application.mainController.load()));
                }
                else {
                    MessageListController controller = Application.mainController.getController();
                    controller.selectUser.getItems().add(login);
                    controller.initiateNewUser();
                    stage.close();
                }
            } else {
                if (login.isEmpty() || !login.matches("(.*)@(.*)")) {
                    System.out.println("Niepoprawny adres email");
                    loginButton.setDisable(false);
                    backButton.setDisable(false);
                    return;
                }
                if (login.endsWith("gmail.com")) {
                    GmailOauth2.getAccessToken(login);
                    System.out.println("Połączono pomyślnie");
                    Stage stage = (Stage) emailAddress.getScene().getWindow();
                    if(Application.mainController == null) {
                        Application.mainController = new FXMLLoader(LoginController.class.getResource("message-list.fxml"));
                        stage = (Stage) emailAddress.getScene().getWindow();
                        stage.setScene(new Scene(Application.mainController.load()));
                    }
                    else {
                        MessageListController controller = Application.mainController.getController();
                        controller.selectUser.getItems().add(login);
                        controller.initiateNewUser();
                        stage.close();
                    }
                }
                else {
                    loginButton.setDisable(true);
                    emailAddress.setEditable(false);
                    String domain = login.split("@")[1];
                    String sql = "SELECT imapServer FROM domains WHERE regex = ?";
                    try (PreparedStatement preparedStatement = SQLiteConnection.connection.prepareStatement(sql)) {
                        preparedStatement.setString(1, domain);
                        var resultSet = preparedStatement.executeQuery();
                        if (!resultSet.isBeforeFirst()) {
                            System.out.println("Brak serwera w bazie danych");
                            emailAddress.setEditable(true);
                            loginButton.setDisable(false);
                        } else {
                            System.out.println("Znaleziono serwer");
                            imapServer = resultSet.getString("imapServer");
                            loginButton.setText("Login");
                            loginButton.setDisable(false);
                            backButton.setVisible(true);
                            password.setVisible(true);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            loginButton.setDisable(false);
            backButton.setDisable(false);
            password.setEditable(true);
        }
    }
    @FXML
    protected void goBack() {
        loginButton.setText("Next");
        loginButton.setDisable(false);
        backButton.setVisible(false);
        password.setVisible(false);
        emailAddress.setEditable(true);
    }
    @FXML
    void initialize() {
        while(Application.model == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}