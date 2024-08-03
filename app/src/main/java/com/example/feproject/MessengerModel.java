package com.example.feproject;

public class MessengerModel {
    private String message;
    private int sender;
    private String time;

    public MessengerModel(String message, int sender, String time) {
        this.message = message;
        this.sender = sender;
        this.time = time;
    }

    public MessengerModel() {
    }

    public String getMessage() {
        return message;
    }

    public int getSender() {
        return sender;
    }

    public String getTime() {
        return time;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
