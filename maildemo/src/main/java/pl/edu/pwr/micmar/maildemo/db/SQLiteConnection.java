package pl.edu.pwr.micmar.maildemo.db;

import pl.edu.pwr.micmar.maildemo.application.Application;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection {
    public static Connection connection;
    public static void connect() {
        try {
            String url = "jdbc:sqlite:" + Application.getJARpath() + "/mailapp.db";
            System.out.println(url);
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
