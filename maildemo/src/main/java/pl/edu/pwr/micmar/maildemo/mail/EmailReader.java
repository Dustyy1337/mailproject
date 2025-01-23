package pl.edu.pwr.micmar.maildemo.mail;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailReader {

    public static void connectToMail(String imapHost, String smtpHost, int imapPort, int smtpPort, boolean imapSSL, boolean imapTLS, boolean smtpSSL, boolean smtpTLS, String username, String accessToken, String refreshTokem, long expiration) throws Exception {
        Properties imapProps = new Properties();
        Properties smtpProps = new Properties();
        imapProps.put("mail.store.protocol", "imap");
        imapProps.put("mail.imap.ssl.enable", imapSSL ? "true" : "false"); // required for Gmail
        imapProps.put("mail.imap.starttls.enable", imapTLS ? "true" : "false");
        //props.put("mail.imap.sasl.enable", "true");
        imapProps.put("mail.imap.port", String.valueOf(imapPort));
        //props.put("mail.imap.ssl.protocols", "TLSv1.2"); // Wymuszenie TLS 1.2
        //props.put("mail.imap.compression.enable", "true");
        if (expiration != 0) imapProps.put("mail.imap.auth.mechanisms", "XOAUTH2");
        //props.put("mail.imap.fetchsize", "1024");


        smtpProps.put("mail.smtp.ssl.enable", smtpSSL ? "true" : "false");
        smtpProps.put("mail.smtp.ssl.trust", smtpHost);
        smtpProps.put("mail.smtp.auth", "true");
        smtpProps.put("mail.smtp.starttls.enable", smtpTLS ? "true" : "false");
        smtpProps.put("mail.smtp.host", smtpHost);
        smtpProps.put("mail.smtp.port", String.valueOf(smtpPort));
        if (expiration != 0) smtpProps.put("mail.smtp.auth.mechanisms", "XOAUTH2");

        SessionCollector.addSession(imapProps, imapHost, smtpProps, smtpHost, username, accessToken);

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
