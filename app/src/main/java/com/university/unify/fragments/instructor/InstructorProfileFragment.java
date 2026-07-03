package com.university.unify.fragments.instructor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.activities.LoginActivity;
import com.university.unify.firebase.FirebaseRefs;
import com.university.unify.fragments.common.CourseCalendarFragment;
import com.university.unify.network.ApiConfig;
import com.university.unify.network.VolleyMultipartRequest;
import com.university.unify.utils.ImageLoaderUtil;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class InstructorProfileFragment extends Fragment {

    private ShapeableImageView imageProfile;
    private TextView buttonChangePhoto;
    private TextView textName;
    private TextView textEmail;
    private TextView textFaculty;
    private TextView textRole;
    private TextView textRoleBadge;
    private ProgressBar progressBar;
    private Button buttonLogout;
    private MaterialButton buttonOpenCalendar;
    private String currentUserId = "";
    private String currentProfileImageUrl = "";

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public InstructorProfileFragment() {
        super(R.layout.fragment_instructor_profile);
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
                            uploadProfileImage(imageUri);
                        }
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageProfile = view.findViewById(R.id.imageInstructorProfile);
        buttonChangePhoto = view.findViewById(R.id.buttonInstructorChangePhoto);
        textName = view.findViewById(R.id.textInstructorProfileName);
        textEmail = view.findViewById(R.id.textInstructorProfileEmail);
        textFaculty = view.findViewById(R.id.textInstructorProfileFaculty);
        textRole = view.findViewById(R.id.textInstructorProfileRole);
        textRoleBadge = view.findViewById(R.id.textInstructorProfileRoleBadge);
        progressBar = view.findViewById(R.id.progressBarInstructorProfile);
        buttonLogout = view.findViewById(R.id.buttonInstructorLogout);

        buttonOpenCalendar = view.findViewById(R.id.buttonInstructorOpenCalendar);
        buttonLogout.setOnClickListener(v -> logout());

        buttonOpenCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCalendar();
            }
        });
        buttonChangePhoto.setOnClickListener(v -> openImagePicker());
        imageProfile.setOnClickListener(v -> openImagePicker());

        loadProfile();
    }

    private void openCalendar() {
        View parent = (View) requireView().getParent();

        getParentFragmentManager()
                .beginTransaction()
                .replace(parent.getId(), new CourseCalendarFragment())
                .addToBackStack(null)
                .commit();
    }
    private void loadProfile() {
        showLoading(true);

        DatabaseHelper db = new DatabaseHelper(requireContext());
        currentUserId = safe(db.getLoggedInUserId());

        if (currentUserId.isEmpty()) {
            showLoading(false);
            loadProfileFromSession();
            return;
        }

        String url = ApiConfig.GET_USER_PROFILE + "?user_id=" + currentUserId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    if (!isAdded()) return;

                    showLoading(false);

                    try {
                        JSONObject obj = new JSONObject(cleanJson(response));
                        boolean success = obj.optBoolean("success", false);

                        if (!success) {
                            loadProfileFromSession();
                            return;
                        }

                        JSONObject data = obj.optJSONObject("data");

                        if (data == null) {
                            loadProfileFromSession();
                            return;
                        }

                        String fullName = safe(data.optString("full_name", ""));
                        String email = safe(data.optString("email", ""));
                        String facultyName = safe(data.optString("faculty_name", ""));
                        String role = safe(data.optString("role", "INSTRUCTOR"));
                        String imageUrl = safe(data.optString("profile_image_url", ""));

                        textName.setText(fullName.isEmpty() ? "-" : fullName);
                        textEmail.setText(email.isEmpty() ? "-" : email);
                        textFaculty.setText(
                                getString(R.string.faculty) + ": " +
                                        (facultyName.isEmpty() ? "-" : facultyName)
                        );
                        textRole.setText(
                                getString(R.string.role) + ": " +
                                        (role.isEmpty() ? "INSTRUCTOR" : role)
                        );
                        textRoleBadge.setText(role.isEmpty() ? "INSTRUCTOR" : role);

                        currentProfileImageUrl = imageUrl;

                        if (!TextUtils.isEmpty(imageUrl)) {
                            loadProfileImage(imageUrl);
                        }

                    } catch (Exception e) {
                        loadProfileFromSession();
                    }
                },
                error -> {
                    if (!isAdded()) return;

                    showLoading(false);
                    loadProfileFromSession();
                    Toast.makeText(
                            requireContext(),
                            getString(R.string.error_loading_profile),
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void loadProfileFromSession() {
        DatabaseHelper db = new DatabaseHelper(requireContext());

        String fullName = safe(db.getLoggedInFullName());
        String email = safe(db.getLoggedInEmail());
        String role = safe(db.getLoggedInRole());

        textName.setText(fullName.isEmpty() ? "-" : fullName);
        textEmail.setText(email.isEmpty() ? "-" : email);
        textFaculty.setText(getString(R.string.faculty) + ": -");
        textRole.setText(getString(R.string.role) + ": " + (role.isEmpty() ? "INSTRUCTOR" : role));
        textRoleBadge.setText(role.isEmpty() ? "INSTRUCTOR" : role);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(requireContext(), getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] imageBytes = getBytesFromUri(imageUri);

        if (imageBytes == null) {
            Toast.makeText(requireContext(), getString(R.string.failed_to_upload_image), Toast.LENGTH_SHORT).show();
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

        Volley.newRequestQueue(requireContext()).add(request);
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
                String imageUrl = safe(obj.optString("profile_image_url", ""));

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
        if (!isAdded()) {
            return;
        }

        String cleanUrl = ImageLoaderUtil.normalizeImageUrl(imageUrl);

        if (TextUtils.isEmpty(cleanUrl)) {
            imageProfile.setImageResource(R.drawable.baseline_person_24);
            return;
        }

        ImageLoaderUtil.loadImage(requireContext(), imageProfile, cleanUrl, false);
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

    private void logout() {
        DatabaseHelper db = new DatabaseHelper(requireContext());
        db.clearLoggedInUser();

        FirebaseRefs.auth().signOut();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        requireActivity().finish();
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
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

    private String safe(String value) {
        return value == null || value.equalsIgnoreCase("null") ? "" : value.trim();
    }
}