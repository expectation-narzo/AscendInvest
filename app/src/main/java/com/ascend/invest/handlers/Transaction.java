package com.ascend.invest.handlers;

public class Transaction {
    private String id;
    private String title;
    private String subtitle;
    private String amount;
    private String status;
    private String type; // "deposit" or "withdrawal"
    private long timestamp;
    private String transactionId;

    public Transaction() {
        // Required for Firebase
    }

    public Transaction(String id, String title, String subtitle, String amount, String status, String type, long timestamp) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.amount = amount;
        this.status = status;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getType() { return type; }
    public long getTimestamp() { return timestamp; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
