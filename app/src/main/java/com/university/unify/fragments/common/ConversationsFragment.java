package com.university.unify.fragments.common;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.activities.SingleChatActivity;
import com.university.unify.adapter.ConversationAdapter;
import com.university.unify.constants.ChatConstants;
import com.university.unify.firebase.FirebaseRefs;
import com.university.unify.model.ChatModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationsFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView textEmpty;
    private RecyclerView recyclerConversations;

    private ConversationAdapter adapter;
    private final List<ChatModel> chatList = new ArrayList<>();

    private DatabaseReference chatsRef;
    private String currentUserId = "";

    private final Map<String, String> avatarCache = new HashMap<>();

    public ConversationsFragment() {
        super(R.layout.fragment_conversations);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadCurrentUser();
        initViews(view);
        setupRecycler();
        setupFirebase();
        listenConversations();
    }

    private void loadCurrentUser() {
        DatabaseHelper db = new DatabaseHelper(requireContext());
        currentUserId = safe(db.getLoggedInUserId());
    }

    private void initViews(View view) {
        progressBar = view.findViewById(R.id.progressConversations);
        textEmpty = view.findViewById(R.id.textNoConversations);
        recyclerConversations = view.findViewById(R.id.recyclerConversations);
    }

    private void setupRecycler() {
        adapter = new ConversationAdapter(
                requireContext(),
                chatList,
                currentUserId,
                new ConversationAdapter.OnConversationClickListener() {
                    @Override
                    public void onConversationClicked(ChatModel chat) {
                        openChat(chat);
                    }
                }
        );

        recyclerConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerConversations.setAdapter(adapter);
    }

    private void setupFirebase() {
        chatsRef = FirebaseRefs.chats();
    }

    private void listenConversations() {
        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(requireContext(), getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            showEmpty(true);
            return;
        }

        showLoading(true);

        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                chatList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    if (child.child("members").child(currentUserId).exists()) {
                        ChatModel chat = child.getValue(ChatModel.class);

                        if (chat != null) {
                            chatList.add(chat);
                            fetchOtherUserAvatarIfNeeded(chat);
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
                if (!isAdded()) return;
                showLoading(false);
                showEmpty(true);
            }
        });
    }

    /**
     * For private chats: find the other participant and fetch their profile photo URL
     * from MySQL, then stamp it on the ChatModel so the row shows a real avatar
     * instead of just an initial. Cached per user_id so it's only fetched once.
     */
    private void fetchOtherUserAvatarIfNeeded(final ChatModel chat) {
        if (chat == null) return;
        if (!ChatConstants.TYPE_PRIVATE.equals(chat.getType())) return;
        if (chat.getMembers() == null || chat.getMembers().isEmpty()) return;

        String otherId = null;
        for (String memberId : chat.getMembers().keySet()) {
            if (memberId != null && !memberId.equals(currentUserId)) {
                otherId = memberId;
                break;
            }
        }
        if (otherId == null || otherId.isEmpty()) return;

        if (avatarCache.containsKey(otherId)) {
            chat.setOtherUserProfileImageUrl(avatarCache.get(otherId));
            adapter.notifyDataSetChanged();
            return;
        }

        String url = ApiConfig.GET_USER_PROFILE + "?user_id=" + otherId;
        final String otherIdFinal = otherId;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    JSONObject data = response.optJSONObject("data");
                    if (data == null) return;

                    String imageUrl = data.optString("profile_image_url", "");
                    avatarCache.put(otherIdFinal, imageUrl);
                    chat.setOtherUserProfileImageUrl(imageUrl);

                    if (isAdded()) adapter.notifyDataSetChanged();
                },
                error -> { /* silent — keeps initial */ }
        );

        queue.add(req);
    }

    private void openChat(ChatModel chat) {
        if (chat == null || TextUtils.isEmpty(chat.getChatId())) {
            return;
        }

        Intent intent = new Intent(requireContext(), SingleChatActivity.class);
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