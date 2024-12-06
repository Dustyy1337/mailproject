package pl.edu.pwr.micmar.maildemo.mail;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailReader {

    public static void connectToMail(String host, String username, String accessToken, String refreshTokem, long expiration) throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.ssl.enable", "true"); // required for Gmail
        props.put("mail.imap.sasl.enable", "true");
        //props.put("mail.imap.ssl.protocols", "TLSv1.2"); // Wymuszenie TLS 1.2
        props.put("mail.imap.compression.enable", "true");
        props.put("mail.imap.auth.mechanisms", "XOAUTH2");
        props.put("mail.imap.fetchsize", "1024");

        SessionCollector.addSession(props, host, username, accessToken);

        /*Session session = Session.getInstance(props);
        Store store = session.getStore("imap");
        store.connect(host, username, accessToken);
        Message[] messages = fetchMessages(store);
        System.out.println(messages[messages.length-1].getSubject());
        System.out.println(messages[13].getMessageNumber());
        return session;*/
    }

    public static Message[] fetchMessages(Store store) throws Exception {
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);
        return folder.getMessages();
    }
}
