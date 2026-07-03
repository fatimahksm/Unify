package com.university.unify.fragments.admin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.activities.LoginActivity;
import com.university.unify.firebase.FirebaseRefs;
import com.university.unify.network.ApiConfig;
import com.university.unify.network.VolleyMultipartRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AdminProfileFragment extends Fragment {

    private ImageView imageProfile;
    private TextView textAdminInitial;
    private TextView buttonChangePhoto;

    private TextView textAdminName;
    private TextView textAdminEmail;
    private TextView textAdminRole;
    private ProgressBar progressBarProfile;
    private MaterialButton buttonLogout;

    private TextView textAdminNameDetail;
    private TextView textAdminEmailDetail;
    private TextView textAdminRoleDetail;

    private RequestQueue queue;
    private DatabaseHelper db;

    private String currentUserId = "";
    private String currentProfileImageUrl = "";

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public AdminProfileFragment() {
        super(R.layout.fragment_admin_profile);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();

                        if (imageUri != null) {
                            imageProfile.setImageURI(imageUri);
                            textAdminInitial.setVisibility(View.GONE);
                            uploadProfileImage(imageUri);
                        }
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageProfile = view.findViewById(R.id.imageAdminProfile);
        textAdminInitial = view.findViewById(R.id.textAdminInitial);
        buttonChangePhoto = view.findViewById(R.id.buttonAdminChangePhoto);

        textAdminName = view.findViewById(R.id.textAdminName);
        textAdminEmail = view.findViewById(R.id.textAdminEmail);
        textAdminRole = view.findViewById(R.id.textAdminRole);

        progressBarProfile = view.findViewById(R.id.progressBarAdminProfile);
        buttonLogout = view.findViewById(R.id.buttonAdminLogout);

        textAdminNameDetail = view.findViewById(R.id.textAdminNameDetail);
        textAdminEmailDetail = view.findViewById(R.id.textAdminEmailDetail);
        textAdminRoleDetail = view.findViewById(R.id.textAdminRoleDetail);

        queue = Volley.newRequestQueue(requireContext());
        db = new DatabaseHelper(requireContext());

        buttonLogout.setOnClickListener(v -> logout());

        imageProfile.setOnClickListener(v -> openImagePicker());
        buttonChangePhoto.setOnClickListener(v -> openImagePicker());

        loadProfile();
    }

    private void loadProfile() {
        String email = getSavedEmail();

        if (email.isEmpty()) {
            logout();
            return;
        }

        currentUserId = safeRaw(db.getLoggedInUserId());

        showLoading(true);

        String url = ApiConfig.GET_USER_BY_EMAIL + "?email=" + Uri.encode(email);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    if (!isAdded()) return;

                    showLoading(false);

                    try {
                        JSONArray array = new JSONArray(cleanJsonArray(response));

                        if (array.length() == 0) {
                            Toast.makeText(
                                    requireContext(),
                                    getString(R.string.error_loading_profile),
                                    Toast.LENGTH_SHORT
                            ).show();
                            loadProfileFromSession();
                            return;
                        }

                        JSONObject obj = array.getJSONObject(0);

                        String userId = safeRaw(obj.optString("user_id"));
                        String fullName = display(obj.optString("full_name"));
                        String userEmail = display(obj.optString("email"));
                        String role = display(obj.optString("role"));
                        String imageUrl = safeRaw(obj.optString("profile_image_url"));

                        if (!TextUtils.isEmpty(userId)) {
                            currentUserId = userId;
                        }

                        bindProfile(fullName, userEmail, role);

                        currentProfileImageUrl = imageUrl;

                        if (!TextUtils.isEmpty(imageUrl)) {
                            loadProfileImage(imageUrl);
                        } else {
                            textAdminInitial.setVisibility(View.VISIBLE);
                        }

                    } catch (Exception e) {
                        Toast.makeText(
                                requireContext(),
                                getString(R.string.profile_parse_error),
                                Toast.LENGTH_SHORT
                        ).show();
                        loadProfileFromSession();
                    }
                },
                error -> {
                    if (!isAdded()) return;

                    showLoading(false);
                    Toast.makeText(
                            requireContext(),
                            getString(R.string.error_loading_profile),
                            Toast.LENGTH_SHORT
                    ).show();
                    loadProfileFromSession();
                }
        );

        queue.add(request);
    }

    private void loadProfileFromSession() {
        String fullName = display(db.getLoggedInFullName());
        String email = display(db.getLoggedInEmail());
        String role = display(db.getLoggedInRole());

        bindProfile(fullName, email, role.isEmpty() ? "SUPER_ADMIN" : role);
    }

    private void bindProfile(String fullName, String email, String role) {
        textAdminInitial.setText(getInitial(fullName));

        textAdminName.setText(display(fullName));
        textAdminEmail.setText(display(email));
        textAdminRole.setText(display(role));

        textAdminNameDetail.setText(display(fullName));
        textAdminEmailDetail.setText(display(email));
        textAdminRoleDetail.setText(display(role));
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(
                    requireContext(),
                    getString(R.string.user_data_not_found),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        byte[] imageBytes = getBytesFromUri(imageUri);

        if (imageBytes == null) {
            Toast.makeText(
                    requireContext(),
                    getString(R.string.failed_to_upload_image),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Toast.makeText(requireContext(), getString(R.string.uploading_image), Toast.LENGTH_SHORT).show();
        showLoading(true);

        VolleyMultipartRequest request = new VolleyMultipartRequest(
                Request.Method.POST,
                ApiConfig.UPLOAD_PROFILE_IMAGE,
                this::handleUploadImageResponse,
                error -> {
                    if (!isAdded()) return;

                    showLoading(false);
                    Toast.makeText(
                            requireContext(),
                            getString(R.string.failed_to_upload_image),
                            Toast.LENGTH_SHORT
                    ).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", currentUserId);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put(
                        "profile_image",
                        new DataPart(
                                "profile_image.jpg",
                                imageBytes,
                                "image/jpeg"
                        )
                );
                return params;
            }
        };

        queue.add(request);
    }

    private void handleUploadImageResponse(NetworkResponse response) {
        if (!isAdded()) return;

        showLoading(false);

        try {
            String responseText = new String(response.data, "UTF-8");
            JSONObject obj = new JSONObject(cleanJson(responseText));

            boolean success = obj.optBoolean("success", false);
            String message = obj.optString(
                    "message",
                    success
                            ? getString(R.string.profile_image_updated)
                            : getString(R.string.failed_to_upload_image)
            );

            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

            if (success) {
                String imageUrl = safeRaw(obj.optString("profile_image_url"));

                if (!TextUtils.isEmpty(imageUrl)) {
                    currentProfileImageUrl = imageUrl;
                    loadProfileImage(imageUrl);
                }
            }

        } catch (Exception e) {
            Toast.makeText(
                    requireContext(),
                    getString(R.string.failed_to_upload_image),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void loadProfileImage(String imageUrl) {
        ImageRequest imageRequest = new ImageRequest(
                imageUrl,
                bitmap -> {
                    if (isAdded()) {
                        imageProfile.setImageBitmap(bitmap);
                        textAdminInitial.setVisibility(View.GONE);
                    }
                },
                350,
                350,
                ImageView.ScaleType.CENTER_CROP,
                Bitmap.Config.ARGB_8888,
                error -> {
                    if (isAdded()) {
                        textAdminInitial.setVisibility(View.VISIBLE);
                    }
                }
        );

        queue.add(imageRequest);
    }

    private byte[] getBytesFromUri(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
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

    private String getSavedEmail() {
        if (db == null) {
            return "";
        }

        return safeRaw(db.getLoggedInEmail());
    }

    private void logout() {
        if (db != null) {
            db.clearLoggedInUser();
        }

        FirebaseRefs.auth().signOut();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        requireActivity().finish();
    }

    private void showLoading(boolean loading) {
        if (progressBarProfile != null) {
            progressBarProfile.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private String getInitial(String value) {
        String cleaned = display(value);

        if (cleaned.equals("-")) {
            return "A";
        }

        return cleaned.substring(0, 1).toUpperCase();
    }

    private String safeRaw(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return "";
        }

        return value.trim();
    }

    private String display(String value) {
        String cleaned = safeRaw(value);

        if (cleaned.isEmpty()) {
            return "-";
        }

        return cleaned;
    }

    private String cleanJson(String response) {
        if (response == null) {
            return "{}";
        }

        String clean = response.trim();

        int start = clean.indexOf("{");
        int end = clean.lastIndexOf("}");

        if (start != -1 && end != -1 && end > start) {
            clean = clean.substring(start, end + 1);
        }

        return clean;
    }

    private String cleanJsonArray(String response) {
        if (response == null) {
            return "[]";
        }

        String clean = response.trim();

        int start = clean.indexOf("[");
        int end = clean.lastIndexOf("]");

        if (start != -1 && end != -1 && end > start) {
            clean = clean.substring(start, end + 1);
        }

        return clean;
    }
}