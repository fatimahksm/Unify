package com.university.unify.fragments.student;

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
import com.android.volley.toolbox.JsonObjectRequest;
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

public class StudentProfileFragment extends Fragment {

    private ShapeableImageView imageProfile;
    private TextView textProfileInitial;
    private TextView buttonChangePhoto;
    private TextView textFullName;
    private TextView textEmail;
    private MaterialButton buttonOpenCalendar;
    private TextView textRole;
    private TextView textFaculty;
    private TextView textMajor;
    private TextView textStudyYear;
    private TextView textStudentNumber;
    private TextView textPhone;
    private ProgressBar progressBar;
    private MaterialButton buttonLogout;

    private DatabaseHelper db;
    private RequestQueue queue;

    private String currentUserId = "";
    private String currentProfileImageUrl = "";

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public StudentProfileFragment() {
        super(R.layout.fragment_student_profile);
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
                            textProfileInitial.setVisibility(View.GONE);
                            uploadProfileImage(imageUri);
                        }
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DatabaseHelper(requireContext());
        queue = Volley.newRequestQueue(requireContext());

        initViews(view);
        setupListeners();

        bindLocalProfileData();
        loadProfileFromApi();
    }

    private void initViews(View view) {
        imageProfile = view.findViewById(R.id.imageProfile);
        textProfileInitial = view.findViewById(R.id.textProfileInitial);
        buttonChangePhoto = view.findViewById(R.id.buttonStudentChangePhoto);

        textFullName = view.findViewById(R.id.textFullName);
        textEmail = view.findViewById(R.id.textEmail);
        textRole = view.findViewById(R.id.textRole);

        textFaculty = view.findViewById(R.id.textFaculty);
        textMajor = view.findViewById(R.id.textMajor);
        textStudyYear = view.findViewById(R.id.textStudyYear);
        textStudentNumber = view.findViewById(R.id.textStudentNumber);
        textPhone = view.findViewById(R.id.textPhone);
        buttonOpenCalendar = view.findViewById(R.id.buttonOpenCalendar);
        progressBar = view.findViewById(R.id.progressStudentProfile);
        buttonLogout = view.findViewById(R.id.buttonLogout);
    }

    private void setupListeners() {
        buttonLogout.setOnClickListener(v -> logout());

        imageProfile.setOnClickListener(v -> openImagePicker());
        buttonChangePhoto.setOnClickListener(v -> openImagePicker());

        buttonOpenCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCalendar();
            }
        });
    }


    private void openCalendar() {
        View parent = (View) requireView().getParent();

        getParentFragmentManager()
                .beginTransaction()
                .replace(parent.getId(), new CourseCalendarFragment())
                .addToBackStack(null)
                .commit();
    }
    private void bindLocalProfileData() {
        currentUserId = safeRaw(db.getLoggedInUserId());

        String fullName = display(db.getLoggedInFullName());
        String email = display(db.getLoggedInEmail());
        String role = display(db.getLoggedInRole());
        String studyYear = display(db.getLoggedInStudyYear());

        textProfileInitial.setText(getInitial(fullName));

        textFullName.setText(getString(R.string.full_name_value, fullName));
        textEmail.setText(getString(R.string.email_value, email));
        textRole.setText(getString(R.string.role_value, role));

        textFaculty.setText(getString(R.string.faculty_value, getString(R.string.not_available_short)));
        textMajor.setText(getString(R.string.major_value, getString(R.string.not_available_short)));
        textStudyYear.setText(getString(R.string.study_year_value, studyYear));
        textStudentNumber.setText(getString(R.string.student_number_value, getString(R.string.not_available_short)));
        textPhone.setText(getString(R.string.phone_value, getString(R.string.not_available_short)));
    }

    private void loadProfileFromApi() {
        String userId = safeRaw(db.getLoggedInUserId());

        if (userId.isEmpty()) {
            return;
        }

        currentUserId = userId;

        String url = ApiConfig.GET_STUDENT_PROFILE + "?user_id=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (!isAdded()) return;

                    try {
                        boolean success = response.optBoolean("success", false);

                        if (!success) {
                            Toast.makeText(
                                    requireContext(),
                                    getString(R.string.failed_to_load_profile),
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        JSONObject data = response.optJSONObject("data");

                        if (data == null) {
                            Toast.makeText(
                                    requireContext(),
                                    getString(R.string.failed_to_load_profile),
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        bindRemoteProfileData(data);

                    } catch (Exception e) {
                        Toast.makeText(
                                requireContext(),
                                getString(R.string.profile_parse_error),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                },
                error -> Toast.makeText(
                        requireContext(),
                        getString(R.string.failed_to_load_profile),
                        Toast.LENGTH_SHORT
                ).show()
        );

        queue.add(request);
    }

    private void bindRemoteProfileData(JSONObject data) {
        String fullName = display(data.optString("full_name"));
        String email = display(data.optString("email"));
        String role = display(data.optString("role"));
        String facultyName = display(data.optString("faculty_name"));
        String majorName = display(data.optString("major_name"));
        String studyYear = display(data.optString("study_year"));
        String studentNumber = display(data.optString("student_number"));
        String phone = display(data.optString("phone_number"));
        String imageUrl = safeRaw(data.optString("profile_image_url"));

        textProfileInitial.setText(getInitial(fullName));

        textFullName.setText(getString(R.string.full_name_value, fullName));
        textEmail.setText(getString(R.string.email_value, email));
        textRole.setText(getString(R.string.role_value, role));
        textFaculty.setText(getString(R.string.faculty_value, facultyName));
        textMajor.setText(getString(R.string.major_value, majorName));
        textStudyYear.setText(getString(R.string.study_year_value, studyYear));
        textStudentNumber.setText(getString(R.string.student_number_value, studentNumber));
        textPhone.setText(getString(R.string.phone_value, phone));

        currentProfileImageUrl = imageUrl;

        if (!TextUtils.isEmpty(imageUrl)) {
            loadProfileImage(imageUrl);
        } else {
            textProfileInitial.setVisibility(View.VISIBLE);
        }
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
                    success ? "Profile image updated successfully" : "Failed to upload image"
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
                    "Failed to upload image",
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
            textProfileInitial.setVisibility(View.VISIBLE);
            imageProfile.setImageResource(R.drawable.baseline_person_24);
            return;
        }

        ImageLoaderUtil.loadImage(requireContext(), imageProfile, cleanUrl, false);
        textProfileInitial.setVisibility(View.GONE);
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
        if (db != null) {
            db.clearLoggedInUser();
        }

        FirebaseRefs.auth().signOut();

        Toast.makeText(
                requireContext(),
                getString(R.string.logout_success),
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        requireActivity().finish();
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String getInitial(String value) {
        String unavailable = getString(R.string.not_available_short);

        if (value == null || value.trim().isEmpty() || value.equals(unavailable)) {
            return getString(R.string.participant_default_initial);
        }

        return value.trim().substring(0, 1).toUpperCase();
    }

    private String display(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return getString(R.string.not_available_short);
        }

        return value.trim();
    }

    private String safeRaw(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return "";
        }

        return value.trim();
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


}