package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class Message implements Serializable {

    private final Long timestamp;

    private int id;

    private final User sentBy;

    private final User sendTo;

    private final String data;

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return this.getSentBy().getName() + " : " + this.getData();
    }

    public Message(Long timestamp, User sentBy, User sendTo, String data, int id) {
        this.timestamp = timestamp;
        this.id = id;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
    }

    public Message(Long timestamp, User sentBy, User sendTo, String data) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public User getSentBy() {
        return sentBy;
    }

    public User getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }
}
