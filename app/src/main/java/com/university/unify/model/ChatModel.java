package com.university.unify.model;

import java.util.HashMap;
import java.util.Map;

public class ChatModel {

    private String chatId;
    private String type;
    private String courseId;
    private String title;
    private String createdBy;
    private long createdAt;

    private String lastMessageText;
    private long lastMessageAt;
    private String lastMessageSenderId;

    /**
     * For PRIVATE chats — the OTHER party's avatar URL, filled in client-side
     * by ConversationsFragment so the row can show a real photo instead of just initials.
     * For COURSE_GROUP chats this stays empty (we show a "G" badge).
     */
    private String otherUserProfileImageUrl;

    private Map<String, Boolean> members;
    private Map<String, Integer> unreadCounts;

    public ChatModel() {
        members = new HashMap<>();
        unreadCounts = new HashMap<>();
    }

    public ChatModel(String chatId,
                     String type,
                     String courseId,
                     String title,
                     String createdBy,
                     long createdAt) {
        this.chatId = chatId;
        this.type = type;
        this.courseId = courseId;
        this.title = title;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.members = new HashMap<>();
        this.unreadCounts = new HashMap<>();
    }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getLastMessageText() { return lastMessageText; }
    public void setLastMessageText(String lastMessageText) { this.lastMessageText = lastMessageText; }

    public long getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(long lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public String getLastMessageSenderId() { return lastMessageSenderId; }
    public void setLastMessageSenderId(String lastMessageSenderId) { this.lastMessageSenderId = lastMessageSenderId; }

    public String getOtherUserProfileImageUrl() { return otherUserProfileImageUrl; }
    public void setOtherUserProfileImageUrl(String otherUserProfileImageUrl) {
        this.otherUserProfileImageUrl = otherUserProfileImageUrl;
    }

    public Map<String, Boolean> getMembers() { return members; }
    public void setMembers(Map<String, Boolean> members) { this.members = members; }

    public Map<String, Integer> getUnreadCounts() { return unreadCounts; }
    public void setUnreadCounts(Map<String, Integer> unreadCounts) { this.unreadCounts = unreadCounts; }
}
