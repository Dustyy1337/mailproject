package pl.edu.pwr.micmar.maildemo.mail;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Properties;

public class SessionCollector {
    public static ArrayList<SingleSession> sessions = new ArrayList<>();

    public static void addSession(Properties imapProps, String imapHost, Properties smtpProps, String smtpHost,  String username, String password) throws MessagingException {
        sessions.add(new SingleSession(imapProps, imapHost, smtpProps, smtpHost, username, password));
    }

}

