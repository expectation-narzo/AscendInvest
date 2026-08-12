package com.ascend.invest.admin;

public class ChatMessage {
    public String senderId;
    public String message;
    public long timestamp;
    public boolean isAdmin;
    public String replyToMsg;
    public String replyToUser;

    public ChatMessage() {}

    public ChatMessage(String senderId, String message, boolean isAdmin) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.isAdmin = isAdmin;
    }

    public ChatMessage(String senderId, String message, boolean isAdmin, String replyToMsg, String replyToUser) {
        this(senderId, message, isAdmin);
        this.replyToMsg = replyToMsg;
        this.replyToUser = replyToUser;
    }
}
