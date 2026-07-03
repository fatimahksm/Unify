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
import com.university.unify.adapter.ConversationAdapter;
import com.university.unify.firebase.FirebaseRefs;
import com.university.unify.model.ChatModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConversationsActivity extends AppCompatActivity {

    private ImageButton buttonBack;
    private ProgressBar progressBar;
    private TextView textEmpty;
    private RecyclerView recyclerConversations;

    private ConversationAdapter adapter;
    private final List<ChatModel> chatList = new ArrayList<>();

    private DatabaseReference chatsRef;

    private String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        loadCurrentUser();

        initViews();
        setupRecycler();
        setupFirebase();
        setupListeners();

        listenConversations();
    }

    private void loadCurrentUser() {
        DatabaseHelper db = new DatabaseHelper(this);
        currentUserId = safe(db.getLoggedInUserId());
    }

    private void initViews() {
        buttonBack = findViewById(R.id.buttonBackConversations);
        progressBar = findViewById(R.id.progressConversations);
        textEmpty = findViewById(R.id.textNoConversations);
        recyclerConversations = findViewById(R.id.recyclerConversations);
    }

    private void setupRecycler() {
        adapter = new ConversationAdapter(
                this,
                chatList,
                currentUserId,
                new ConversationAdapter.OnConversationClickListener() {
                    @Override
                    public void onConversationClicked(ChatModel chat) {
                        openChat(chat);
                    }
                }
        );

        recyclerConversations.setLayoutManager(new LinearLayoutManager(this));
        recyclerConversations.setAdapter(adapter);
    }

    private void setupFirebase() {
        chatsRef = FirebaseRefs.chats();
    }

    private void setupListeners() {
        buttonBack.setOnClickListener(v -> finish());
    }

    private void listenConversations() {
        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            showLoading(false);
            showEmpty(true);
            return;
        }

        showLoading(true);

        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    if (child.child("members").child(currentUserId).exists()) {
                        ChatModel chat = child.getValue(ChatModel.class);

                        if (chat != null) {
                            chatList.add(chat);
                        }
                    }
                }

                Collections.sort(chatList, new Comparator<ChatModel>() {
                    @Override
                    public int compare(ChatModel first, ChatModel second) {
                        return Long.compare(second.getLastMessageAt(), first.getLastMessageAt());
                    }
                });

                adapter.notifyDataSetChanged();

                showLoading(false);
                showEmpty(chatList.isEmpty());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                showEmpty(true);

                Toast.makeText(
                        ConversationsActivity.this,
                        getString(R.string.failed_to_load_chat),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void openChat(ChatModel chat) {
        if (chat == null || TextUtils.isEmpty(chat.getChatId())) {
            return;
        }

        Intent intent = new Intent(this, SingleChatActivity.class);
        intent.putExtra("chat_id", chat.getChatId());
        intent.putExtra("chat_type", chat.getType());
        intent.putExtra("course_id", chat.getCourseId());
        intent.putExtra("chat_title", chat.getTitle());

        startActivity(intent);
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerConversations.setVisibility(loading ? View.GONE : View.VISIBLE);
        textEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(boolean empty) {
        textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerConversations.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private String safe(String value) {
        if (value == null || value.equalsIgnoreCase("null")) {
            return "";
        }
        return value.trim();
    }
}