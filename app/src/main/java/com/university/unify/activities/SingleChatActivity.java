package com.university.unify.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.adapter.MessageAdapter;
import com.university.unify.constants.ChatConstants;
import com.university.unify.firebase.FirebaseRefs;
import com.university.unify.firebase.NotificationHelper;
import com.university.unify.model.MessageModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleChatActivity extends AppCompatActivity {

    private ImageButton buttonBack;
    private TextView textTitle;
    private TextView textSubtitle;
    private TextView textNoMessages;
    private RecyclerView recyclerMessages;
    private EditText editMessage;
    private Button buttonSend;

    private MessageAdapter adapter;
    private final List<MessageModel> messageList = new ArrayList<>();

    private DatabaseReference chatsRef;
    private DatabaseReference messagesRef;

    private String chatId = "";
    private String chatTitle = "";
    private String chatType = "";
    private String courseId = "";
    private String otherUserId = "";
    private String otherUserName = "";

    private String currentUserId = "";
    private String currentUserName = "";
    private String currentUserAvatar = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);

        readIntentData();
        loadCurrentUser();

        initViews();
        setupRecycler();
        setupFirebase();
        setupListeners();

        prepareChat();
        listenMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        markCurrentChatAsRead();
    }

    private void readIntentData() {
        chatId = safe(getIntent().getStringExtra("chat_id"));
        chatTitle = safe(getIntent().getStringExtra("chat_title"));
        chatType = safe(getIntent().getStringExtra("chat_type"));
        courseId = safe(getIntent().getStringExtra("course_id"));
        otherUserId = safe(getIntent().getStringExtra("other_user_id"));
        otherUserName = safe(getIntent().getStringExtra("other_user_name"));
    }

    private void loadCurrentUser() {
        DatabaseHelper db = new DatabaseHelper(this);

        currentUserId = safe(db.getLoggedInUserId());
        currentUserName = safe(db.getLoggedInFullName());

        if (currentUserName.isEmpty()) {
            currentUserName = safe(db.getLoggedInEmail());
        }

        if (currentUserName.isEmpty()) {
            currentUserName = "User";
        }

        currentUserAvatar = safe(db.getLoggedInProfileImageUrl());
    }

    private void initViews() {
        buttonBack = findViewById(R.id.buttonBackChat);
        textTitle = findViewById(R.id.textChatTitle);
        textSubtitle = findViewById(R.id.textChatSubtitle);
        textNoMessages = findViewById(R.id.textNoMessages);
        recyclerMessages = findViewById(R.id.recyclerMessages);
        editMessage = findViewById(R.id.editMessage);
        buttonSend = findViewById(R.id.buttonSendMessage);

        if (chatTitle.isEmpty()) {
            chatTitle = getString(R.string.chat);
        }

        textTitle.setText(chatTitle);

        if (ChatConstants.TYPE_COURSE_GROUP.equals(chatType)) {
            textSubtitle.setText(getString(R.string.course_group_chat));
        } else {
            textSubtitle.setText(getString(R.string.private_chat));
        }
    }

    private void setupRecycler() {
        adapter = new MessageAdapter(this, messageList, currentUserId);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);

        recyclerMessages.setLayoutManager(manager);
        recyclerMessages.setAdapter(adapter);
    }

    private void setupFirebase() {
        chatsRef = FirebaseRefs.chats();
        messagesRef = FirebaseRefs.messages();
    }

    private void setupListeners() {
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void prepareChat() {
        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (TextUtils.isEmpty(chatType)) {
            chatType = ChatConstants.TYPE_PRIVATE;
        }

        if (TextUtils.isEmpty(chatId)) {
            if (ChatConstants.TYPE_COURSE_GROUP.equals(chatType)) {
                if (TextUtils.isEmpty(courseId)) {
                    Toast.makeText(this, "Course id is empty", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                chatId = ChatConstants.courseGroupChatId(courseId);
            } else {
                if (TextUtils.isEmpty(otherUserId)) {
                    Toast.makeText(this, "Other user id is empty", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                chatId = ChatConstants.privateChatId(currentUserId, otherUserId);
            }
        }

        if (TextUtils.isEmpty(chatTitle)) {
            chatTitle = getString(R.string.chat);
        }

        long now = System.currentTimeMillis();

        Map<String, Object> updates = new HashMap<>();
        updates.put("chatId", chatId);
        updates.put("type", chatType);
        updates.put("courseId", courseId);
        updates.put("title", chatTitle);
        updates.put("createdBy", currentUserId);
        updates.put("createdAt", now);
        updates.put("members/" + currentUserId, true);
        updates.put("unreadCounts/" + currentUserId, 0);

        if (!TextUtils.isEmpty(otherUserId)) {
            updates.put("members/" + otherUserId, true);
        }

        chatsRef.child(chatId).updateChildren(updates)
                .addOnFailureListener(e -> Toast.makeText(
                        SingleChatActivity.this,
                        "Chat prepare failed: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());

        markCurrentChatAsRead();
    }

    private void listenMessages() {
        if (TextUtils.isEmpty(chatId)) {
            return;
        }

        messagesRef.child(chatId)
                .orderByChild("createdAt")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageList.clear();

                        for (DataSnapshot child : snapshot.getChildren()) {
                            MessageModel message = child.getValue(MessageModel.class);

                            if (message != null && !message.isDeleted()) {
                                messageList.add(message);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if (messageList.isEmpty()) {
                            textNoMessages.setVisibility(View.VISIBLE);
                        } else {
                            textNoMessages.setVisibility(View.GONE);
                            recyclerMessages.scrollToPosition(messageList.size() - 1);
                        }

                        markCurrentChatAsRead();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(
                                SingleChatActivity.this,
                                getString(R.string.failed_to_load_chat),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void sendMessage() {
        String text = editMessage.getText().toString().trim();

        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Message is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(chatId)) {
            Toast.makeText(this, "Chat id is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (messagesRef == null) {
            Toast.makeText(this, "Firebase messages reference is null", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonSend.setEnabled(false);
        buttonSend.setText("...");

        DatabaseReference newMessageRef = messagesRef.child(chatId).push();
        String messageId = newMessageRef.getKey();

        if (messageId == null) {
            buttonSend.setEnabled(true);
            buttonSend.setText(getString(R.string.send));
            Toast.makeText(this, getString(R.string.failed_to_send_message), Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();

        MessageModel message = new MessageModel(
                messageId,
                chatId,
                currentUserId,
                currentUserName,
                currentUserAvatar,
                text,
                now,
                false
        );

        newMessageRef.setValue(message)
                .addOnCompleteListener(task -> {
                    buttonSend.setEnabled(true);
                    buttonSend.setText(getString(R.string.send));

                    if (task.isSuccessful()) {
                        editMessage.setText("");
                        updateLastMessage(text, now);
                        updateUnreadCountsAfterSend();
                        sendNotificationAfterMessage(text);

                    } else {
                        String errorMessage = "Unknown error";

                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }

                        Toast.makeText(
                                SingleChatActivity.this,
                                "Send failed: " + errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void updateLastMessage(String text, long time) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessageText", text);
        updates.put("lastMessageAt", time);
        updates.put("lastMessageSenderId", currentUserId);

        chatsRef.child(chatId).updateChildren(updates);
    }

    private void updateUnreadCountsAfterSend() {
        if (TextUtils.isEmpty(chatId) || TextUtils.isEmpty(currentUserId)) {
            return;
        }

        if (ChatConstants.TYPE_PRIVATE.equals(chatType)) {
            if (TextUtils.isEmpty(otherUserId)) {
                return;
            }

            incrementUnreadForUser(otherUserId);

            chatsRef.child(chatId)
                    .child("unreadCounts")
                    .child(currentUserId)
                    .setValue(0);

            return;
        }

        if (ChatConstants.TYPE_COURSE_GROUP.equals(chatType)) {
            chatsRef.child(chatId)
                    .child("members")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String memberId = child.getKey();

                            if (TextUtils.isEmpty(memberId)) {
                                continue;
                            }

                            if (memberId.equals(currentUserId)) {
                                continue;
                            }

                            incrementUnreadForUser(memberId);
                        }

                        chatsRef.child(chatId)
                                .child("unreadCounts")
                                .child(currentUserId)
                                .setValue(0);
                    });
        }
    }

    private void incrementUnreadForUser(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return;
        }

        chatsRef.child(chatId)
                .child("unreadCounts")
                .child(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    int oldCount = 0;

                    try {
                        Integer value = snapshot.getValue(Integer.class);
                        oldCount = value == null ? 0 : value;
                    } catch (Exception ignored) {
                    }

                    chatsRef.child(chatId)
                            .child("unreadCounts")
                            .child(userId)
                            .setValue(oldCount + 1);
                });
    }

    private void markCurrentChatAsRead() {
        if (TextUtils.isEmpty(chatId) || TextUtils.isEmpty(currentUserId) || chatsRef == null) {
            return;
        }

        chatsRef.child(chatId)
                .child("unreadCounts")
                .child(currentUserId)
                .setValue(0);
    }

    private void sendNotificationAfterMessage(String text) {
        if (TextUtils.isEmpty(chatId) || TextUtils.isEmpty(currentUserId)) {
            return;
        }

        if (ChatConstants.TYPE_PRIVATE.equals(chatType)) {
            if (TextUtils.isEmpty(otherUserId)) {
                return;
            }

            NotificationHelper.sendPrivateChatNotification(
                    otherUserId,
                    currentUserName,
                    text,
                    chatId,
                    chatTitle,
                    chatType,
                    courseId
            );

            return;
        }

        if (ChatConstants.TYPE_COURSE_GROUP.equals(chatType)) {
            NotificationHelper.sendGroupChatNotificationToMembers(
                    chatId,
                    currentUserId,
                    currentUserName,
                    text,
                    chatTitle,
                    chatType,
                    courseId
            );
        }
    }

    private String safe(String value) {
        if (value == null || value.equalsIgnoreCase("null")) {
            return "";
        }

        return value.trim();
    }
}