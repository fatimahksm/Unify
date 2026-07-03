package com.university.unify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.adapter.NotificationAdapter;
import com.university.unify.constants.NotificationConstants;
import com.university.unify.firebase.FirebaseRefs;
import com.university.unify.model.NotificationModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private ImageButton buttonBack;
    private ProgressBar progressBar;
    private TextView textEmpty;
    private RecyclerView recyclerNotifications;

    private NotificationAdapter adapter;
    private final List<NotificationModel> notificationList = new ArrayList<>();

    private DatabaseReference notificationsRef;

    private String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        loadCurrentUser();

        initViews();
        setupRecycler();
        setupFirebase();
        setupListeners();

        listenNotifications();
    }

    private void loadCurrentUser() {
        DatabaseHelper db = new DatabaseHelper(this);
        currentUserId = safe(db.getLoggedInUserId());
    }

    private void initViews() {
        buttonBack = findViewById(R.id.buttonBackNotifications);
        progressBar = findViewById(R.id.progressNotifications);
        textEmpty = findViewById(R.id.textNoNotifications);
        recyclerNotifications = findViewById(R.id.recyclerNotifications);
    }

    private void setupRecycler() {
        adapter = new NotificationAdapter(
                this,
                notificationList,
                notification -> handleNotificationClick(notification)
        );

        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotifications.setAdapter(adapter);
    }

    private void setupFirebase() {
        notificationsRef = FirebaseRefs.notifications();
    }

    private void setupListeners() {
        buttonBack.setOnClickListener(v -> finish());
    }

    private void listenNotifications() {
        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            showEmpty(true);
            return;
        }

        showLoading(true);

        notificationsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    NotificationModel item = child.getValue(NotificationModel.class);

                    if (item != null) {
                        notificationList.add(item);
                    }
                }

                Collections.sort(notificationList, new Comparator<NotificationModel>() {
                    @Override
                    public int compare(NotificationModel first, NotificationModel second) {
                        return Long.compare(second.getCreatedAt(), first.getCreatedAt());
                    }
                });

                adapter.notifyDataSetChanged();

                showLoading(false);
                showEmpty(notificationList.isEmpty());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                showEmpty(true);
            }
        });
    }

    private void handleNotificationClick(NotificationModel notification) {
        if (notification == null) {
            return;
        }

        markAsRead(notification);

        String referenceType = safe(notification.getReferenceType());

        if (NotificationConstants.REF_CHAT.equals(referenceType)) {
            openChat(notification);
            return;
        }

        if (NotificationConstants.REF_MATERIALS.equals(referenceType)) {
            openCourseMaterials(notification);
            return;
        }

        if (NotificationConstants.REF_GRADES.equals(referenceType)) {
            Toast.makeText(this, getString(R.string.open_course_grades_hint), Toast.LENGTH_SHORT).show();
            return;
        }

        if (NotificationConstants.REF_ANNOUNCEMENT.equals(referenceType)) {
            Toast.makeText(this, getString(R.string.open_course_announcements_hint), Toast.LENGTH_SHORT).show();
        }
    }

    private void markAsRead(NotificationModel notification) {
        String notificationId = safe(notification.getNotificationId());

        if (TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(notificationId)) {
            return;
        }

        notificationsRef.child(currentUserId)
                .child(notificationId)
                .child("read")
                .setValue(true);
    }

    private void openChat(NotificationModel notification) {
        String chatId = safe(notification.getChatId());

        if (TextUtils.isEmpty(chatId)) {
            chatId = safe(notification.getReferenceId());
        }

        if (TextUtils.isEmpty(chatId)) {
            Toast.makeText(this, getString(R.string.failed_to_load_chat), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, SingleChatActivity.class);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("chat_type", notification.getChatType());
        intent.putExtra("course_id", notification.getCourseId());
        intent.putExtra("chat_title", notification.getChatTitle());

        startActivity(intent);
    }

    private void openCourseMaterials(NotificationModel notification) {
        String courseId = safe(notification.getCourseId());

        if (TextUtils.isEmpty(courseId)) {
            Toast.makeText(this, getString(R.string.course_id_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, StudentCourseMaterialsActivity.class);
        intent.putExtra("course_id", courseId);
        intent.putExtra("course_title", notification.getCourseTitle());
        intent.putExtra("course_code", notification.getCourseCode());

        startActivity(intent);
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerNotifications.setVisibility(loading ? View.GONE : View.VISIBLE);
        textEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(boolean empty) {
        textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerNotifications.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private String safe(String value) {
        if (value == null || value.equalsIgnoreCase("null")) {
            return "";
        }
        return value.trim();
    }
}