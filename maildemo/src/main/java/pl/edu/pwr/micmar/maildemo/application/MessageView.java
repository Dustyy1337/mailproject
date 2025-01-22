package pl.edu.pwr.micmar.maildemo.application;

import javax.mail.Message;

public class MessageView {
    public String timeDate;
    public String header;
    public String sender;
    public int messageIndex;
    public double similarity;
    public Message message;
    public MessageView(String timeDate, String header, String sender, int messageIndex, double similarity, Message message) {
        this.timeDate = timeDate;
        this.header = header;
        this.sender = sender;
        this.messageIndex = messageIndex;
        this.similarity=similarity;
        this.message = message;
    }

    public String getHeader() {
        return header;
    }

    public String getSender() {
        return sender;
    }

    public String getTimeDate() {
        return timeDate;
    }

    public int getMessageIndex() {
        return messageIndex;
    }
}
