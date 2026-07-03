package com.university.unify.model;

public class PresenceModel {

    private boolean online;
    private long lastSeen;
    private String currentChatId;
    private String typingInChatId;

    // Empty constructor required for Firebase
    public PresenceModel() {
    }

    public PresenceModel(boolean online, long lastSeen, String currentChatId, String typingInChatId) {
        this.online = online;
        this.lastSeen = lastSeen;
        this.currentChatId = currentChatId;
        this.typingInChatId = typingInChatId;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getCurrentChatId() {
        return currentChatId;
    }

    public void setCurrentChatId(String currentChatId) {
        this.currentChatId = currentChatId;
    }

    public String getTypingInChatId() {
        return typingInChatId;
    }

    public void setTypingInChatId(String typingInChatId) {
        this.typingInChatId = typingInChatId;
    }
}