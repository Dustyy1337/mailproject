package pl.edu.pwr.micmar.maildemo.mail;

import javax.mail.*;
import java.util.Properties;

public class SingleSession {
    public Session imapSession, smtpSession;
    public String imapHost, smtpHost, username, password, refreshToken;
    public boolean isOauth2;
    public long expiration;
    public Store imapStore;


    public SingleSession(Properties imapProps, String imapHost, Properties smtpProps, String smtpHost, String username, String password) throws MessagingException {
        this.imapSession = Session.getInstance(imapProps);
        this.imapHost = imapHost;
        this.username = username;
        this.password = password;
        this.smtpHost = smtpHost;
        isOauth2=false;
        imapStore = this.getStore("imap");
        smtpSession = Session.getInstance(smtpProps, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }
    public SingleSession(Properties imapProps, String imapHost, Properties smtpProps, String smtpHost, String username, String password, String refreshToken, long expiration) throws MessagingException {
        this.imapSession = Session.getInstance(imapProps);
        this.imapHost = imapHost;
        this.smtpHost = smtpHost;
        this.username = username;
        this.password = password;
        this.refreshToken = refreshToken;
        this.expiration = expiration;
        isOauth2=true;
        imapStore = this.getStore("imap");
        smtpSession = Session.getInstance(smtpProps, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }
    public Store getStore(String protocol) throws MessagingException {
        Store store = imapSession.getStore(protocol);
        store.connect(imapHost, username, password);
        return store;
    }
}
