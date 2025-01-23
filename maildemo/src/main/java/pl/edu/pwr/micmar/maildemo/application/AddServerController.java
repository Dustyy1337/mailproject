package pl.edu.pwr.micmar.maildemo.application;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import pl.edu.pwr.micmar.maildemo.db.SQLiteConnection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddServerController {
    @FXML
    protected TextField imapServer, imapPort, smtpServer, smtpPort;
    @FXML
    protected CheckBox imapSSL, imapTLS, smtpSSL, smtpTLS;
    public String regex;

    @FXML
    protected void addServers() throws SQLException {
        if (imapServer.getText().isEmpty() || imapPort.getText().isEmpty() || smtpServer.getText().isEmpty() || smtpPort.getText().isEmpty()) {
            System.out.println("Fill all fields");
        } else {
            String sql = "INSERT INTO domains (regex, imapServer, smtpServer, imapPort, smtpPort, imapSSL, imapTLS, smtpSSL, smtpTLS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = SQLiteConnection.connection.prepareStatement(sql)) {
                preparedStatement.setString(1, regex);
                preparedStatement.setString(2, imapServer.getText());
                preparedStatement.setString(3, smtpServer.getText());
                preparedStatement.setInt(4, Integer.parseInt(imapPort.getText()));
                preparedStatement.setInt(5, Integer.parseInt(smtpPort.getText()));
                preparedStatement.setBoolean(6, imapSSL.isSelected());
                preparedStatement.setBoolean(7, imapTLS.isSelected());
                preparedStatement.setBoolean(8, smtpSSL.isSelected());
                preparedStatement.setBoolean(9, smtpTLS.isSelected());
                preparedStatement.executeUpdate();
                Stage stage = (Stage) imapServer.getScene().getWindow();
                stage.close();
            }
        }
    }
}