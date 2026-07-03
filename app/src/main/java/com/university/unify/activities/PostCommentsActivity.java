package com.university.unify.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.adapter.PostCommentAdapter;
import com.university.unify.model.PostCommentModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostCommentsActivity extends AppCompatActivity {

    private ImageButton buttonBack;
    private ProgressBar progressComments;
    private TextView textNoComments;
    private RecyclerView recyclerComments;
    private EditText editComment;
    private Button buttonSendComment;

    private PostCommentAdapter adapter;
    private final ArrayList<PostCommentModel> comments = new ArrayList<>();

    private String postId = "";
    private String currentUserId = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comments);

        postId = safe(getIntent().getStringExtra("post_id"));
        loadCurrentUser();

        initViews();
        setupRecycler();
        setupListeners();

        loadComments();
    }

    private void loadCurrentUser() {
        DatabaseHelper db = new DatabaseHelper(this);
        currentUserId = safe(db.getLoggedInUserId());
    }

    private void initViews() {
        buttonBack = findViewById(R.id.buttonBackComments);
        progressComments = findViewById(R.id.progressComments);
        textNoComments = findViewById(R.id.textNoComments);
        recyclerComments = findViewById(R.id.recyclerComments);
        editComment = findViewById(R.id.editComment);
        buttonSendComment = findViewById(R.id.buttonSendComment);
    }

    private void setupRecycler() {
        adapter = new PostCommentAdapter(this, comments);
        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerComments.setAdapter(adapter);
    }

    private void setupListeners() {
        buttonBack.setOnClickListener(v -> finish());
        buttonSendComment.setOnClickListener(v -> addComment());
    }

    private void loadComments() {
        if (TextUtils.isEmpty(postId)) {
            showEmpty(true);
            return;
        }

        progressComments.setVisibility(View.VISIBLE);
        textNoComments.setVisibility(View.GONE);

        String url = ApiConfig.GET_POST_COMMENTS + "?post_id=" + postId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressComments.setVisibility(View.GONE);
                    comments.clear();

                    try {
                        boolean success = response.optBoolean("success", false);

                        if (!success) {
                            adapter.notifyDataSetChanged();
                            showEmpty(true);
                            return;
                        }

                        JSONArray data = response.optJSONArray("data");

                        if (data != null) {
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject item = data.optJSONObject(i);

                                if (item == null) {
                                    continue;
                                }

                                PostCommentModel comment = new PostCommentModel();

                                comment.setCommentId(item.optString("comment_id"));
                                comment.setPostId(item.optString("post_id"));
                                comment.setUserId(item.optString("user_id"));
                                comment.setCommentText(item.optString("comment_text"));
                                comment.setCreatedAt(item.optString("created_at"));
                                comment.setFullName(item.optString("full_name"));
                                comment.setEmail(item.optString("email"));
                                comment.setProfileImageUrl(item.optString("profile_image_url"));

                                comments.add(comment);
                            }
                        }

                        adapter.notifyDataSetChanged();
                        showEmpty(comments.isEmpty());

                    } catch (Exception e) {
                        adapter.notifyDataSetChanged();
                        showEmpty(true);
                    }
                },
                error -> {
                    progressComments.setVisibility(View.GONE);
                    showEmpty(true);
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void addComment() {
        String text = editComment.getText().toString().trim();

        if (TextUtils.isEmpty(postId)) {
            Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(text)) {
            editComment.setError(getString(R.string.error_required));
            return;
        }

        buttonSendComment.setEnabled(false);
        buttonSendComment.setText("...");

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADD_POST_COMMENT,
                response -> {
                    buttonSendComment.setEnabled(true);
                    buttonSendComment.setText(getString(R.string.send));

                    try {
                        JSONObject obj = new JSONObject(cleanJson(response));
                        boolean success = obj.optBoolean("success", false);
                        String message = obj.optString("message", getString(R.string.something_went_wrong));

                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                        if (success) {
                            editComment.setText("");
                            loadComments();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    buttonSendComment.setEnabled(true);
                    buttonSendComment.setText(getString(R.string.send));
                    Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("post_id", postId);
                params.put("user_id", currentUserId);
                params.put("comment_text", text);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void showEmpty(boolean empty) {
        textNoComments.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerComments.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private String cleanJson(String response) {
        if (response == null) {
            return "{}";
        }

        String clean = response.trim();
        int start = clean.indexOf("{");
        int end = clean.lastIndexOf("}");

        if (start >= 0 && end > start) {
            return clean.substring(start, end + 1);
        }

        return clean;
    }

    private String safe(String value) {
        if (value == null || value.equalsIgnoreCase("null")) {
            return "";
        }

        return value.trim();
    }
}