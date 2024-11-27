package pl.edu.pwr.micmar.maildemo.mail;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

public class SingleSession {
    private Session session;
    private String host, username, password;

    public SingleSession(Properties properties, String host, String username, String password) {
        this.session = Session.getInstance(properties);
        this.host = host;
        this.username = username;
        this.password = password;
    }
    public Store getStore(String protocol) throws MessagingException {
        Store store = session.getStore(protocol);
        store.connect(host, username, password);
        return store;
    }
}
