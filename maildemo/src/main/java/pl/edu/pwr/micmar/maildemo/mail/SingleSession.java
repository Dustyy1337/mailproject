package pl.edu.pwr.micmar.maildemo.mail;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

public class SingleSession {
    public Session session;
    public String host, username, password, refreshToken;
    public boolean isOauth2;
    public long expiration;

    public SingleSession(Properties properties, String host, String username, String password) {
        this.session = Session.getInstance(properties);
        this.host = host;
        this.username = username;
        this.password = password;
        isOauth2=false;
    }
    public SingleSession(Properties properties, String host, String username, String password, String refreshToken, long expiration) {
        this.session = Session.getInstance(properties);
        this.host = host;
        this.username = username;
        this.password = password;
        this.refreshToken = refreshToken;
        this.expiration = expiration;
        isOauth2=true;
    }
    public Store getStore(String protocol) throws MessagingException {
        Store store = session.getStore(protocol);
        store.connect(host, username, password);
        return store;
    }
}
