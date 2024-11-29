package pl.edu.pwr.micmar.maildemo.mail;

import javax.mail.Message;

public class MessageVector {
    private Message message;
    private float[] vector;

    public MessageVector(Message message, float[] vector) {
        this.message = message;
        this.vector = vector;
    }

    public Message getMessage() {
        return message;
    }

    public float[] getVector() {
        return vector;
    }
}