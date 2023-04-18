package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class Message implements Serializable {

    private final Long timestamp;

    private int id;

    private final int sentBy;

    private final int sendTo;

    private final String data;

    public int getId() {
        return id;
    }

    public Message(Long timestamp, int sentBy, int sendTo, String data, int id) {
        this.timestamp = timestamp;
        this.id = id;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
    }

    public Message(Long timestamp, int sentBy, int sendTo, String data) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public int getSentBy() {
        return sentBy;
    }

    public int getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }
}
