package com.ascend.invest.handlers;

public class SupportTicket {
    private String id;
    private String title;
    private String description;
    private String status; // "Pending", "Resolved", "In Progress"
    private String adminReply;
    private long timestamp;

    public SupportTicket() {
        // Required for Firebase
    }

    public SupportTicket(String id, String title, String description, String status, long timestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.timestamp = timestamp;
        this.adminReply = "";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
