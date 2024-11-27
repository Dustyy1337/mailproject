package pl.edu.pwr.micmar.maildemo.mail;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
public class MimeMessageReader {
    public static String getTextFromMessage(Message message, boolean isHTML) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("text/html")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            if (isHTML) {
                return getTextFromMimeMultipart(mimeMultipart);
            } else {
                return getPlainTextFromMimeMultipart(mimeMultipart);
            }
        }
        return "";
    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String htmlContent = "";
        String plainContent = "";
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/html")) {
                htmlContent = bodyPart.getContent().toString();
            } else if (bodyPart.isMimeType("text/plain")) {
                plainContent = bodyPart.getContent().toString();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                String nestedContent = getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
                if (bodyPart.isMimeType("text/html")) {
                    htmlContent = nestedContent;
                } else if (bodyPart.isMimeType("text/plain")) {
                    plainContent = nestedContent;
                }
            }
        }
        return !htmlContent.isEmpty() ? htmlContent : plainContent;
    }
    private static String getPlainTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String plainContent = "";
        String htmlContent = "";
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                plainContent = bodyPart.getContent().toString();
            } else if (bodyPart.isMimeType("text/html")) {
                htmlContent = bodyPart.getContent().toString();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                String nestedContent = getPlainTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
                if (bodyPart.isMimeType("text/plain")) {
                    plainContent = nestedContent;
                } else if (bodyPart.isMimeType("text/html")) {
                    htmlContent = nestedContent;
                }
            }
        }
        return !plainContent.isEmpty() ? plainContent : htmlContent;
    }
}
