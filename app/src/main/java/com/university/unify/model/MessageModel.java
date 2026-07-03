package com.university.unify.model;

public class MessageModel {

    private String messageId;
    private String chatId;
    private String senderId;
    private String senderName;
    private String senderProfileImageUrl;
    private String text;
    private long createdAt;
    private boolean deleted;

    public MessageModel() {
    }

    public MessageModel(String messageId,
                        String chatId,
                        String senderId,
                        String senderName,
                        String senderProfileImageUrl,
                        String text,
                        long createdAt,
                        boolean deleted) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderProfileImageUrl = senderProfileImageUrl;
        this.text = text;
        this.createdAt = createdAt;
        this.deleted = deleted;
    }

    // ===== Backwards-compatible constructor (no image) =====
    public MessageModel(String messageId,
                        String chatId,
                        String senderId,
                        String senderName,
                        String text,
                        long createdAt,
                        boolean deleted) {
        this(messageId, chatId, senderId, senderName, "", text, createdAt, deleted);
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderProfileImageUrl() {
        return senderProfileImageUrl;
    }

    public void setSenderProfileImageUrl(String senderProfileImageUrl) {
        this.senderProfileImageUrl = senderProfileImageUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
