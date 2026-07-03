package com.university.unify.firebase;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.university.unify.firebase.FirebaseRefs;
import com.google.firebase.database.FirebaseDatabase;
import com.university.unify.constants.ChatConstants;
import com.university.unify.constants.NotificationConstants;
import com.university.unify.model.NotificationModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class NotificationHelper {

    private static final String TAG = "NOTIFICATION_HELPER";

    private static final String DATABASE_URL =
            FirebaseRefs.REALTIME_DB_URL;

    private NotificationHelper() {
    }

    private static DatabaseReference notificationsRef() {
        return FirebaseDatabase
                .getInstance(DATABASE_URL)
                .getReference(NotificationConstants.NOTIFICATIONS);
    }

    private static DatabaseReference chatsRef() {
        return FirebaseDatabase
                .getInstance(DATABASE_URL)
                .getReference(ChatConstants.CHATS);
    }

    public static void sendToUser(String receiverId,
                                  String type,
                                  String title,
                                  String body,
                                  String referenceType,
                                  String referenceId,
                                  String chatId,
                                  String chatTitle,
                                  String chatType,
                                  String courseId,
                                  String courseTitle,
                                  String courseCode) {

        if (isEmpty(receiverId)) {
            return;
        }

        DatabaseReference userRef = notificationsRef().child(receiverId);
        String notificationId = userRef.push().getKey();

        if (notificationId == null) {
            return;
        }

        NotificationModel notification = new NotificationModel();

        notification.setNotificationId(notificationId);
        notification.setUserId(receiverId);

        notification.setType(clean(type));
        notification.setTitle(clean(title));
        notification.setBody(clean(body));

        notification.setReferenceType(clean(referenceType));
        notification.setReferenceId(clean(referenceId));

        notification.setChatId(clean(chatId));
        notification.setChatTitle(clean(chatTitle));
        notification.setChatType(clean(chatType));

        notification.setCourseId(clean(courseId));
        notification.setCourseTitle(clean(courseTitle));
        notification.setCourseCode(clean(courseCode));

        notification.setRead(false);
        notification.setCreatedAt(System.currentTimeMillis());

        userRef.child(notificationId).setValue(notification)
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to send notification: " + e.getMessage())
                );
    }

    public static void sendPrivateChatNotification(String receiverId,
                                                   String senderName,
                                                   String messageText,
                                                   String chatId,
                                                   String chatTitle,
                                                   String chatType,
                                                   String courseId) {

        sendToUser(
                receiverId,
                NotificationConstants.TYPE_PRIVATE_MESSAGE,
                senderName,
                messageText,
                NotificationConstants.REF_CHAT,
                chatId,
                chatId,
                chatTitle,
                chatType,
                courseId,
                "",
                ""
        );
    }

    public static void sendGroupChatNotificationToMembers(String chatId,
                                                          String senderId,
                                                          String senderName,
                                                          String messageText,
                                                          String chatTitle,
                                                          String chatType,
                                                          String courseId) {

        if (isEmpty(chatId) || isEmpty(senderId)) {
            return;
        }

        chatsRef().child(chatId).child("members").get()
                .addOnSuccessListener(snapshot -> {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String receiverId = child.getKey();

                        if (isEmpty(receiverId)) {
                            continue;
                        }

                        if (receiverId.equals(senderId)) {
                            continue;
                        }

                        sendToUser(
                                receiverId,
                                NotificationConstants.TYPE_GROUP_MESSAGE,
                                senderName,
                                messageText,
                                NotificationConstants.REF_CHAT,
                                chatId,
                                chatId,
                                chatTitle,
                                chatType,
                                courseId,
                                "",
                                ""
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to read group members: " + e.getMessage())
                );
    }

    public static void sendCourseMaterialNotificationToStudents(Context context,
                                                                String courseId,
                                                                String courseTitle,
                                                                String courseCode,
                                                                String materialTitle,
                                                                String senderId) {

        String title = "New material";
        String body = isEmpty(materialTitle)
                ? "A new material was added to " + safeTitle(courseTitle)
                : materialTitle + " was added to " + safeTitle(courseTitle);

        sendCourseEventNotificationToStudents(
                context,
                courseId,
                courseTitle,
                courseCode,
                senderId,
                NotificationConstants.TYPE_COURSE_MATERIAL,
                title,
                body,
                NotificationConstants.REF_MATERIALS
        );
    }

    public static void sendCourseAnnouncementNotificationToStudents(Context context,
                                                                    String courseId,
                                                                    String courseTitle,
                                                                    String courseCode,
                                                                    String announcementTitle,
                                                                    String senderId) {

        String title = "New announcement";
        String body = isEmpty(announcementTitle)
                ? "A new announcement was posted in " + safeTitle(courseTitle)
                : announcementTitle;

        sendCourseEventNotificationToStudents(
                context,
                courseId,
                courseTitle,
                courseCode,
                senderId,
                NotificationConstants.TYPE_COURSE_ANNOUNCEMENT,
                title,
                body,
                NotificationConstants.REF_ANNOUNCEMENT
        );
    }

    public static void sendGradeNotificationToStudent(String studentId,
                                                      String courseId,
                                                      String courseTitle,
                                                      String courseCode) {

        sendToUser(
                studentId,
                NotificationConstants.TYPE_GRADE_UPDATED,
                "Grade updated",
                "Your grade was updated in " + safeTitle(courseTitle),
                NotificationConstants.REF_GRADES,
                courseId,
                "",
                "",
                "",
                courseId,
                courseTitle,
                courseCode
        );
    }

    private static void sendCourseEventNotificationToStudents(Context context,
                                                              String courseId,
                                                              String courseTitle,
                                                              String courseCode,
                                                              String senderId,
                                                              String type,
                                                              String title,
                                                              String body,
                                                              String referenceType) {

        if (context == null || isEmpty(courseId)) {
            return;
        }

        String url = ApiConfig.GET_COURSE_STUDENTS + "?course_id=" + courseId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(cleanJson(response));
                        boolean success = obj.optBoolean("success", false);

                        if (!success) {
                            return;
                        }

                        JSONArray data = obj.optJSONArray("data");

                        if (data == null) {
                            return;
                        }

                        Set<String> sentIds = new HashSet<>();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.optJSONObject(i);

                            if (item == null) {
                                continue;
                            }

                            String studentId = clean(item.optString("user_id", ""));

                            if (isEmpty(studentId)) {
                                continue;
                            }

                            if (studentId.equals(senderId)) {
                                continue;
                            }

                            if (sentIds.contains(studentId)) {
                                continue;
                            }

                            sentIds.add(studentId);

                            sendToUser(
                                    studentId,
                                    type,
                                    title,
                                    body,
                                    referenceType,
                                    courseId,
                                    "",
                                    "",
                                    "",
                                    courseId,
                                    courseTitle,
                                    courseCode
                            );
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Course notification parse error", e);
                    }
                },
                error -> Log.e(TAG, "Failed to load course students: " + error.toString())
        );

        Volley.newRequestQueue(context.getApplicationContext()).add(request);
    }

    private static String cleanJson(String response) {
        if (response == null) {
            return "{}";
        }

        String clean = response.trim();

        int start = clean.indexOf("{");
        int end = clean.lastIndexOf("}");

        if (start != -1 && end != -1 && end > start) {
            return clean.substring(start, end + 1);
        }

        return clean;
    }

    private static String safeTitle(String value) {
        if (isEmpty(value)) {
            return "the course";
        }

        return value.trim();
    }

    private static String clean(String value) {
        if (value == null || value.equalsIgnoreCase("null")) {
            return "";
        }

        return value.trim();
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null");
    }
}