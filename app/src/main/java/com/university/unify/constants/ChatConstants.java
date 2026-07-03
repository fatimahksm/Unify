package com.university.unify.constants;

public class ChatConstants {

    public static final String CHATS = "chats";
    public static final String MESSAGES = "messages";

    public static final String TYPE_PRIVATE = "PRIVATE";
    public static final String TYPE_COURSE_GROUP = "COURSE_GROUP";

    public static String privateChatId(String firstUserId, String secondUserId) {
        int first = toInt(firstUserId);
        int second = toInt(secondUserId);

        if (first <= second) {
            return "private_" + firstUserId + "_" + secondUserId;
        } else {
            return "private_" + secondUserId + "_" + firstUserId;
        }
    }

    public static String courseGroupChatId(String courseId) {
        return "course_" + courseId + "_group";
    }

    private static int toInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }
}