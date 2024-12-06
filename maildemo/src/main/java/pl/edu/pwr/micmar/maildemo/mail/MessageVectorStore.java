package pl.edu.pwr.micmar.maildemo.mail;

import javax.mail.Message;
import java.util.ArrayList;
import java.util.List;

public class MessageVectorStore {
    private List<MessageVector> messageVectors = new ArrayList<>();

    public void addMessageVector(Message message, double similarity) {
        messageVectors.add(new MessageVector(message, similarity));
    }

    public List<MessageVector> getMessageVectors() {
        return messageVectors;
    }
}