package pl.edu.pwr.micmar.maildemo.application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.edu.pwr.micmar.maildemo.db.SQLiteConnection;
import pl.edu.pwr.micmar.maildemo.mail.EmailReader;
import pl.edu.pwr.micmar.maildemo.mail.GmailOauth2;
import pl.edu.pwr.micmar.maildemo.mail.SessionCollector;

import javax.mail.AuthenticationFailedException;
import javax.mail.Session;
import java.io.IOException;
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
    private String smtpServer = "";
    private int imapPort = 0;
    private int smtpPort = 0;
    private boolean imapSSL, imapTLS, smtpSSL, smtpTLS;

    @FXML
    protected void configureServers() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LoginController.class.getResource("edit-servers.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setTitle("Edit servers");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(emailAddress.getScene().getWindow());
        stage.showAndWait();
    }
    @FXML
    protected void loginAuth()  {
        try {
            String login = emailAddress.getText();
            if (Objects.equals(loginButton.getText(), "Login")) {
                loginButton.setDisable(true);
                backButton.setDisable(true);
                password.setEditable(false);
                EmailReader.connectToMail(imapServer,smtpServer, imapPort, smtpPort, imapSSL, imapTLS, smtpSSL, smtpTLS, login, password.getText(), "", 0);
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
                }

                else {
                    loginButton.setDisable(true);
                    emailAddress.setEditable(false);
                    String domain = login.split("@")[1];
                    String sql = "SELECT imapServer, smtpServer, imapPort, smtpPort, imapSSL, imapTLS, smtpSSL, smtpTLS FROM domains WHERE regex = ?";
                    try (PreparedStatement preparedStatement = SQLiteConnection.connection.prepareStatement(sql)) {
                        preparedStatement.setString(1, domain);
                        var resultSet = preparedStatement.executeQuery();
                        if (!resultSet.isBeforeFirst()) {
                            System.out.println("Brak serwera w bazie danych");
                            emailAddress.setEditable(true);
                            loginButton.setDisable(false);
                            FXMLLoader fxmlLoader = new FXMLLoader(LoginController.class.getResource("add-server.fxml"));
                            Stage stage = new Stage();
                            Scene scene = new Scene(fxmlLoader.load());
                            AddServerController controller = fxmlLoader.getController();
                            controller.regex = login.split("@")[1];
                            stage.setScene(scene);
                            stage.setTitle("Add server");
                            stage.initModality(Modality.APPLICATION_MODAL);
                            stage.initOwner(emailAddress.getScene().getWindow());
                            stage.showAndWait();
                        } else {
                            System.out.println("Znaleziono serwer");
                            imapServer = resultSet.getString("imapServer");
                            smtpServer = resultSet.getString("smtpServer");
                            imapPort = resultSet.getInt("imapPort");
                            smtpPort = resultSet.getInt("smtpPort");
                            imapSSL = resultSet.getBoolean("imapSSL");
                            imapTLS = resultSet.getBoolean("imapTLS");
                            smtpSSL = resultSet.getBoolean("smtpSSL");
                            smtpTLS = resultSet.getBoolean("smtpTLS");
                            if (imapServer.equals("imap.gmail.com")) {
                                GmailOauth2.getAccessToken(login);
                                System.out.println("Połączono pomyślnie");
                                Stage stage = (Stage) emailAddress.getScene().getWindow();
                                if(Application.mainController == null) {
                                    System.out.println("mainController is null");
                                    Application.mainController = new FXMLLoader(LoginController.class.getResource("message-list.fxml"));
                                    stage = (Stage) emailAddress.getScene().getWindow();
                                    stage.setScene(new Scene(Application.mainController.load()));
                                }
                                else {
                                    System.out.println("mainController is not null");
                                    MessageListController controller = Application.mainController.getController();
                                    System.out.println("pobrano kontroler");
                                    controller.selectUser.getItems().add(login);
                                    System.out.println("dodano użytkownika");
                                    controller.initiateNewUser();
                                    System.out.println("zainicjowano nowego użytkownika");
                                    stage.close();
                                    System.out.println("zamknięto okno");
                                }
                            }
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
            if(e.getClass().equals(AuthenticationFailedException.class)) {
                System.out.println("Błędne dane logowania");
            }
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