package pl.edu.pwr.micmar.maildemo.mail;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jsoup.Jsoup;

public class MimeMessageReader {
    public static String getTextFromMessage(Message message, boolean isHTML) throws MessagingException, IOException {
        Object content = message.getContent();
        if (message.isMimeType("text/plain") || message.isMimeType("text/html") || message.isMimeType("application/xhtml+xml")) {
            return content.toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) content;
            return isHTML ? getTextFromMimeMultipart(mimeMultipart) : getPlainTextFromMimeMultipart(mimeMultipart);
        }
        return "Brak treści";
    }
    public static String getTextFromMessage(MimeMessage mimeMessage, boolean isHTML) throws MessagingException, IOException {
        return getTextFromMessage((Message) mimeMessage, isHTML);
    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder htmlContent = new StringBuilder();
        StringBuilder plainContent = new StringBuilder();
        Map<String, String> cidMap = new HashMap<>(); 

        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            Object content = bodyPart.getContent();

            // Pomijanie załączników
            if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) ||
                    Part.INLINE.equalsIgnoreCase(bodyPart.getDisposition()) ||
                    (bodyPart.getDisposition() == null && bodyPart.getFileName() != null)) { 
                if (bodyPart instanceof MimeBodyPart) {
                    MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;
                    if (mimeBodyPart.getContentID() != null) {
                        String cid = mimeBodyPart.getContentID().replaceAll("[<>]", "");
                        File tempFile = File.createTempFile("email-image-", mimeBodyPart.getFileName());
                        mimeBodyPart.saveFile(tempFile);
                        cidMap.put(cid, tempFile.toURI().toString());
                        System.out.println("Dodano CID bez dyspozycji: " + cid + " -> " + tempFile.toURI());
                    }
                }
                continue;
            }

            if (bodyPart.isMimeType("text/html") || bodyPart.isMimeType("application/xhtml+xml")) {
                htmlContent.append(content.toString());
            } else if (bodyPart.isMimeType("text/plain")) {
                plainContent.append(content.toString());
            } else if (content instanceof MimeMultipart) {
                String nestedContent = getTextFromMimeMultipart((MimeMultipart) content);
                return nestedContent;
            }
        }

        // Zastąp odwołania do CID w treści HTML
        if (htmlContent.length() > 0) {
            org.jsoup.nodes.Document document = org.jsoup.Jsoup.parse(htmlContent.toString());
            document.select("img[src^=cid:]").forEach(img -> {
                String cid = img.attr("src").substring(4); 
                String localUri = cidMap.get(cid); 
                if (localUri != null) {
                    img.attr("src", localUri); 
                } else {
                    System.out.println("Nie znaleziono lokalnego URI dla CID: " + cid);
                }
            });
            htmlContent = new StringBuilder(document.html()); 
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
            } else if (content instanceof MimeMultipart) {
                plainContent.append(getPlainTextFromMimeMultipart((MimeMultipart) content));
            } else if (bodyPart.isMimeType("text/html") || bodyPart.isMimeType("application/xhtml+xml")) {
                plainContent.append(Jsoup.parse(content.toString()).text());
            }
        }
        return plainContent.toString();
    }
    public static List<BodyPart> getAttachments(Message message) throws MessagingException, IOException {
        List<BodyPart> attachments = new ArrayList<>();
        if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            for (int i = 0; i < mimeMultipart.getCount(); i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) ||
                        (bodyPart.getDisposition() == null && bodyPart.getFileName() != null)) {
                    attachments.add(bodyPart);
                }
            }
        }
        return attachments;
    }
    public static void saveAttachment(BodyPart bodyPart, Stage stage) throws IOException, MessagingException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(bodyPart.getFileName());
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (InputStream inputStream = bodyPart.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
