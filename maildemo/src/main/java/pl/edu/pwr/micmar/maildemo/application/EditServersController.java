package pl.edu.pwr.micmar.maildemo.application;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import pl.edu.pwr.micmar.maildemo.db.SQLiteConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class EditServersController {
    @FXML
    protected CheckBox imapSSL, imapTLS, smtpSSL, smtpTLS;
    @FXML
    protected ChoiceBox<String> selectRegex;
    @FXML
    protected TextField imapServer, imapPort, smtpServer, smtpPort;
    Map<String, ServerInfo> serverInfoMap = new HashMap<>();
    @FXML
    protected void saveChanges() {
        String sql = "UPDATE domains SET imapServer = ?, smtpServer = ?, imapPort = ?, smtpPort = ?, imapSSL = ?, imapTLS = ?, smtpSSL = ?, smtpTLS = ? WHERE regex = ?";
        try(PreparedStatement preparedStatement = SQLiteConnection.connection.prepareStatement(sql)) {
            preparedStatement.setString(1, imapServer.getText());
            preparedStatement.setString(2, smtpServer.getText());
            preparedStatement.setInt(3, Integer.parseInt(imapPort.getText()));
            preparedStatement.setInt(4, Integer.parseInt(smtpPort.getText()));
            preparedStatement.setBoolean(5, imapSSL.isSelected());
            preparedStatement.setBoolean(6, imapTLS.isSelected());
            preparedStatement.setBoolean(7, smtpSSL.isSelected());
            preparedStatement.setBoolean(8, smtpTLS.isSelected());
            preparedStatement.setString(9, selectRegex.getValue());
            preparedStatement.executeUpdate();
            serverInfoMap.replace(selectRegex.getValue(), new ServerInfo(imapServer.getText(), smtpServer.getText(), Integer.parseInt(imapPort.getText()), Integer.parseInt(smtpPort.getText()), imapSSL.isSelected(), imapTLS.isSelected(), smtpSSL.isSelected(), smtpTLS.isSelected()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void initialize() throws SQLException {
        String sql = "SELECT * FROM domains";
        try(PreparedStatement preparedStatement = SQLiteConnection.connection.prepareStatement(sql)) {
            var resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                String regex = resultSet.getString("regex");
                serverInfoMap.put(regex, new ServerInfo(
                        resultSet.getString("imapServer"),
                        resultSet.getString("smtpServer"),
                        resultSet.getInt("imapPort"),
                        resultSet.getInt("smtpPort"),
                        resultSet.getBoolean("imapSSL"),
                        resultSet.getBoolean("imapTLS"),
                        resultSet.getBoolean("smtpSSL"),
                        resultSet.getBoolean("smtpTLS")
                ));
                selectRegex.getItems().add(regex);
            }
        }
        selectRegex.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            ServerInfo serverInfo = serverInfoMap.get(newValue);
            imapServer.setText(serverInfo.imapServer);
            imapPort.setText(String.valueOf(serverInfo.imapPort));
            smtpServer.setText(serverInfo.smtpServer);
            smtpPort.setText(String.valueOf(serverInfo.smtpPort));
            imapSSL.setSelected(serverInfo.imapSSL);
            imapTLS.setSelected(serverInfo.imapTLS);
            smtpSSL.setSelected(serverInfo.smtpSSL);
            smtpTLS.setSelected(serverInfo.smtpTLS);
        });
        selectRegex.getSelectionModel().select(0);
    }
    class ServerInfo {
        String imapServer, smtpServer;
        int imapPort, smtpPort;
        boolean imapSSL, imapTLS, smtpSSL, smtpTLS;
        ServerInfo(String imapServer, String smtpServer, int imapPort, int smtpPort, boolean imapSSL, boolean imapTLS, boolean smtpSSL, boolean smtpTLS) {
            this.imapServer = imapServer;
            this.smtpServer = smtpServer;
            this.imapPort = imapPort;
            this.smtpPort = smtpPort;
            this.imapSSL = imapSSL;
            this.imapTLS = imapTLS;
            this.smtpSSL = smtpSSL;
            this.smtpTLS = smtpTLS;
        }
    }
}
