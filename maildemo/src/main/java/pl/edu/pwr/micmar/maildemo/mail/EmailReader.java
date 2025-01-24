package pl.edu.pwr.micmar.maildemo.mail;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailReader {

    public static void connectToMail(String imapHost, String smtpHost, int imapPort, int smtpPort, boolean imapSSL, boolean imapTLS, boolean smtpSSL, boolean smtpTLS, String username, String accessToken, String refreshTokem, long expiration) throws Exception {
        Properties imapProps = new Properties();
        Properties smtpProps = new Properties();
        imapProps.put("mail.store.protocol", "imap");
        imapProps.put("mail.imap.ssl.enable", imapSSL ? "true" : "false"); 
        imapProps.put("mail.imap.starttls.enable", imapTLS ? "true" : "false");
        imapProps.put("mail.imap.port", String.valueOf(imapPort));

        if (expiration != 0) imapProps.put("mail.imap.auth.mechanisms", "XOAUTH2");


        smtpProps.put("mail.smtp.ssl.enable", smtpSSL ? "true" : "false");
        smtpProps.put("mail.smtp.ssl.trust", smtpHost);
        smtpProps.put("mail.smtp.auth", "true");
        smtpProps.put("mail.smtp.starttls.enable", smtpTLS ? "true" : "false");
        smtpProps.put("mail.smtp.host", smtpHost);
        smtpProps.put("mail.smtp.port", String.valueOf(smtpPort));
        if (expiration != 0) smtpProps.put("mail.smtp.auth.mechanisms", "XOAUTH2");

        SessionCollector.addSession(imapProps, imapHost, smtpProps, smtpHost, username, accessToken);

       
    }

}
