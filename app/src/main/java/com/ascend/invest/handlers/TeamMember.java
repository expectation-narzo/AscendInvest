package com.ascend.invest.handlers;

public class TeamMember {
    private String uid;
    private String username;
    private String email;
    private int level;
    private boolean active;

    public TeamMember() {}

    public TeamMember(String uid, String username, String email, int level, boolean active) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.level = level;
        this.active = active;
    }

    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public int getLevel() { return level; }
    public boolean isActive() { return active; }
}
