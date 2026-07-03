package com.university.unify.model;

public class ChatMemberModel {

    private String userId;
    private String memberRole;
    private long joinedAt;
    private long lastReadAt;
    private String lastReadMessageId;
    private boolean isMuted;
    private boolean isActive;

    // Empty constructor required for Firebase
    public ChatMemberModel() {
    }

    public ChatMemberModel(String userId, String memberRole, long joinedAt,
                           long lastReadAt, String lastReadMessageId,
                           boolean isMuted, boolean isActive) {
        this.userId = userId;
        this.memberRole = memberRole;
        this.joinedAt = joinedAt;
        this.lastReadAt = lastReadAt;
        this.lastReadMessageId = lastReadMessageId;
        this.isMuted = isMuted;
        this.isActive = isActive;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(String memberRole) {
        this.memberRole = memberRole;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(long joinedAt) {
        this.joinedAt = joinedAt;
    }

    public long getLastReadAt() {
        return lastReadAt;
    }

    public void setLastReadAt(long lastReadAt) {
        this.lastReadAt = lastReadAt;
    }

    public String getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(String lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}