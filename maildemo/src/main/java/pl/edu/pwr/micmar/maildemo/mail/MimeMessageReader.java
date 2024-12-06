package pl.edu.pwr.micmar.maildemo.mail;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import org.jsoup.Jsoup;

public class MimeMessageReader {
    public static String getTextFromMessage(Message message, boolean isHTML) throws MessagingException, IOException {
        Object content = message.getContent();
        if (message.isMimeType("text/plain") || message.isMimeType("text/html")) {
            return content.toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) content;
            return isHTML ? getTextFromMimeMultipart(mimeMultipart) : getPlainTextFromMimeMultipart(mimeMultipart);
        }
        return "";
    }
    public static String getTextFromMessage(MimeMessage mimeMessage, boolean isHTML) throws MessagingException, IOException {
        return getTextFromMessage((Message) mimeMessage, isHTML);
    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder htmlContent = new StringBuilder();
        StringBuilder plainContent = new StringBuilder();
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            Object content = bodyPart.getContent();
            if (bodyPart.isMimeType("text/html")) {
                htmlContent.append(content.toString());
            } else if (bodyPart.isMimeType("text/plain")) {
                plainContent.append(content.toString());
            } else if (content instanceof MimeMultipart) {
                String nestedContent = getTextFromMimeMultipart((MimeMultipart) content);
                if (bodyPart.isMimeType("text/html")) {
                    htmlContent.append(nestedContent);
                } else if (bodyPart.isMimeType("text/plain")) {
                    plainContent.append(nestedContent);
                }
            }
        }
        return htmlContent.length() > 0 ? htmlContent.toString() : plainContent.toString();
    }

    private static String getPlainTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder plainContent = new StringBuilder();
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            Object content = bodyPart.getContent();
            if (bodyPart.isMimeType("text/plain")) {
                plainContent.append(content.toString());
            } else if (bodyPart.isMimeType("text/html")) {
                plainContent.append(Jsoup.parse(content.toString()).text());
            } else if (content instanceof MimeMultipart) {
                plainContent.append(getPlainTextFromMimeMultipart((MimeMultipart) content));
            }
        }
        return plainContent.toString();
    }
}