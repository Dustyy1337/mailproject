package pl.edu.pwr.micmar.maildemo.mail;

import javax.mail.Message;
import java.util.ArrayList;
import java.util.List;

public class MessageVectorStore {
    private List<MessageVector> messageVectors = new ArrayList<>();

    public void addMessageVector(Message message, float[] vector) {
        messageVectors.add(new MessageVector(message, vector));
    }

    public List<MessageVector> getMessageVectors() {
        return messageVectors;
    }
}