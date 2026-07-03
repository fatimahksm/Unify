package com.university.unify.fragments.student;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.activities.PostCommentsActivity;
import com.university.unify.adapter.StudentPostAdapter;
import com.university.unify.model.StudentPostModel;
import com.university.unify.network.ApiConfig;
import com.university.unify.network.VolleyMultipartRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StudentSocialFragment extends Fragment {

    private LinearLayout layoutAddPostCard;
    private ProgressBar progressSocial;
    private LinearLayout layoutSocialEmpty;
    private RecyclerView recyclerSocialPosts;

    private StudentPostAdapter adapter;
    private final ArrayList<StudentPostModel> posts = new ArrayList<>();

    private String currentUserId = "";

    private Uri selectedPostImageUri;
    private ImageView currentPreviewImage;
    private Button currentRemoveImageButton;

    private final ActivityResultLauncher<String> pickPostImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPostImageUri = uri;

                    if (currentPreviewImage != null) {
                        currentPreviewImage.setImageURI(uri);
                        currentPreviewImage.setVisibility(View.VISIBLE);
                    }

                    if (currentRemoveImageButton != null) {
                        currentRemoveImageButton.setVisibility(View.VISIBLE);
                    }
                }
            });

    public StudentSocialFragment() {
        super(R.layout.fragment_student_social);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadCurrentUser();
        initViews(view);
        setupRecycler();
        setupListeners();
        loadPosts();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!TextUtils.isEmpty(currentUserId)) {
            loadPosts();
        }
    }

    private void loadCurrentUser() {
        DatabaseHelper db = new DatabaseHelper(requireContext());
        currentUserId = safe(db.getLoggedInUserId());
    }

    private void initViews(View view) {
        layoutAddPostCard = view.findViewById(R.id.layoutAddPostCard);
        progressSocial = view.findViewById(R.id.progressSocial);
        layoutSocialEmpty = view.findViewById(R.id.layoutSocialEmpty);
        recyclerSocialPosts = view.findViewById(R.id.recyclerSocialPosts);
    }

    private void setupRecycler() {
        adapter = new StudentPostAdapter(
                requireContext(),
                posts,
                new StudentPostAdapter.OnPostActionListener() {
                    @Override
                    public void onLikeClicked(StudentPostModel post) {
                        toggleLike(post);
                    }

                    @Override
                    public void onCommentClicked(StudentPostModel post) {
                        openComments(post);
                    }
                }
        );

        recyclerSocialPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerSocialPosts.setAdapter(adapter);
    }

    private void setupListeners() {
        layoutAddPostCard.setOnClickListener(v -> showCreatePostDialog());
    }

    private void showCreatePostDialog() {
        selectedPostImageUri = null;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_post, null);

        EditText editContent = dialogView.findViewById(R.id.editDialogPostContent);
        ImageView imagePreview = dialogView.findViewById(R.id.imagePostPreview);
        Button buttonPickImage = dialogView.findViewById(R.id.buttonPickPostImage);
        Button buttonRemoveImage = dialogView.findViewById(R.id.buttonRemovePostImage);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancelPost);
        Button buttonPublish = dialogView.findViewById(R.id.buttonPublishPost);

        currentPreviewImage = imagePreview;
        currentRemoveImageButton = buttonRemoveImage;

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        buttonPickImage.setOnClickListener(v -> pickPostImageLauncher.launch("image/*"));

        buttonRemoveImage.setOnClickListener(v -> {
            selectedPostImageUri = null;
            imagePreview.setImageDrawable(null);
            imagePreview.setVisibility(View.GONE);
            buttonRemoveImage.setVisibility(View.GONE);
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonPublish.setOnClickListener(v -> {
            String content = editContent.getText().toString().trim();

            if (TextUtils.isEmpty(content)) {
                editContent.setError(getString(R.string.error_required));
                return;
            }

            createPost(content, dialog, buttonPublish);
        });

        dialog.setOnDismissListener(d -> {
            currentPreviewImage = null;
            currentRemoveImageButton = null;
            selectedPostImageUri = null;
        });

        dialog.setOnShowListener(dialogInterface -> {
            Window shownWindow = dialog.getWindow();
            if (shownWindow != null) {
                shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });

        dialog.show();
    }

    private void createPost(String content, AlertDialog dialog, Button buttonPublish) {
        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(requireContext(), getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPostImageUri == null) {
            createTextOnlyPost(content, dialog, buttonPublish);
        } else {
            createPostWithImage(content, dialog, buttonPublish);
        }
    }

    private void createTextOnlyPost(String content, AlertDialog dialog, Button buttonPublish) {
        buttonPublish.setEnabled(false);
        buttonPublish.setText("...");

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.CREATE_STUDENT_POST,
                response -> {
                    buttonPublish.setEnabled(true);
                    buttonPublish.setText(getString(R.string.publish));

                    handleCreatePostResponse(response, dialog);
                },
                error -> {
                    buttonPublish.setEnabled(true);
                    buttonPublish.setText(getString(R.string.publish));
                    Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", currentUserId);
                params.put("content", content);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void createPostWithImage(String content, AlertDialog dialog, Button buttonPublish) {
        buttonPublish.setEnabled(false);
        buttonPublish.setText("...");

        VolleyMultipartRequest request = new VolleyMultipartRequest(
                Request.Method.POST,
                ApiConfig.CREATE_STUDENT_POST,
                response -> {
                    buttonPublish.setEnabled(true);
                    buttonPublish.setText(getString(R.string.publish));

                    String responseText = new String(response.data, StandardCharsets.UTF_8);
                    handleCreatePostResponse(responseText, dialog);
                },
                error -> {
                    buttonPublish.setEnabled(true);
                    buttonPublish.setText(getString(R.string.publish));
                    Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", currentUserId);
                params.put("content", content);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                byte[] imageData = getBytesFromUri(selectedPostImageUri);

                if (imageData != null) {
                    params.put(
                            "post_image",
                            new DataPart("post_image.jpg", imageData, "image/jpeg")
                    );
                }

                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void handleCreatePostResponse(String response, AlertDialog dialog) {
        try {
            JSONObject obj = new JSONObject(cleanJson(response));
            boolean success = obj.optBoolean("success", false);
            String message = obj.optString("message", getString(R.string.something_went_wrong));

            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

            if (success) {
                dialog.dismiss();
                loadPosts();
            }

        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getBytesFromUri(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len;

            while (inputStream != null && (len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }

            if (inputStream != null) {
                inputStream.close();
            }

            return byteBuffer.toByteArray();

        } catch (Exception e) {
            return null;
        }
    }

    private void loadPosts() {
        if (TextUtils.isEmpty(currentUserId)) {
            showEmpty(true);
            return;
        }

        progressSocial.setVisibility(View.VISIBLE);
        layoutSocialEmpty.setVisibility(View.GONE);

        String url = ApiConfig.GET_FACULTY_POSTS + "?user_id=" + currentUserId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressSocial.setVisibility(View.GONE);
                    posts.clear();

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

                                StudentPostModel post = new StudentPostModel();

                                post.setPostId(item.optString("post_id"));
                                post.setUserId(item.optString("user_id"));
                                post.setFacultyId(item.optString("faculty_id"));
                                post.setContent(item.optString("content"));
                                post.setImageUrl(item.optString("image_url"));
                                post.setLikesCount(item.optString("likes_count"));
                                post.setCommentsCount(item.optString("comments_count"));
                                post.setCreatedAt(item.optString("created_at"));
                                post.setFullName(item.optString("full_name"));
                                post.setEmail(item.optString("email"));
                                post.setProfileImageUrl(item.optString("profile_image_url"));
                                post.setLikedByMe(item.optInt("liked_by_me", 0) == 1);

                                posts.add(post);
                            }
                        }

                        adapter.notifyDataSetChanged();
                        showEmpty(posts.isEmpty());

                    } catch (Exception e) {
                        adapter.notifyDataSetChanged();
                        showEmpty(true);
                    }
                },
                error -> {
                    progressSocial.setVisibility(View.GONE);
                    showEmpty(true);
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void toggleLike(StudentPostModel post) {
        if (post == null || TextUtils.isEmpty(post.getPostId())) {
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.TOGGLE_POST_LIKE,
                response -> loadPosts(),
                error -> Toast.makeText(
                        requireContext(),
                        getString(R.string.something_went_wrong),
                        Toast.LENGTH_SHORT
                ).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("post_id", post.getPostId());
                params.put("user_id", currentUserId);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void openComments(StudentPostModel post) {
        if (post == null || TextUtils.isEmpty(post.getPostId())) {
            return;
        }

        Intent intent = new Intent(requireContext(), PostCommentsActivity.class);
        intent.putExtra("post_id", post.getPostId());
        startActivity(intent);
    }

    private void showEmpty(boolean empty) {
        layoutSocialEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerSocialPosts.setVisibility(empty ? View.GONE : View.VISIBLE);
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