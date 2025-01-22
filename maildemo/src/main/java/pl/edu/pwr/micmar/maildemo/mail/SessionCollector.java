package pl.edu.pwr.micmar.maildemo.mail;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Properties;

public class SessionCollector {
    public static ArrayList<SingleSession> sessions = new ArrayList<>();

    public static void addSession(Properties properties, String host, String username, String password) throws MessagingException {
        sessions.add(new SingleSession(properties, host, username, password));
    }

}

