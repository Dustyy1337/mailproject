package pl.edu.pwr.micmar.maildemo.mail;

import javax.mail.Message;

public class MessageVector {
    private Message message;
    private double similarity;

    public MessageVector(Message message, double similarity) {
        this.message = message;
        this.similarity = similarity;
    }

    public Message getMessage() {
        return message;
    }

    public double getSimilarity() {
        return similarity;
    }
}