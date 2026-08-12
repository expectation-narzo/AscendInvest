package com.ascend.invest.handlers;

public class Announcement {
    private String id;
    private String title;
    private String message;
    private long timestamp;

    public Announcement() {}

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}
