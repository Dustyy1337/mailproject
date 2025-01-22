package pl.edu.pwr.micmar.maildemo.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection {
    public static Connection connection;
    public static void connect() {
        try {
            String url = "jdbc:sqlite:mailapp.db";
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
