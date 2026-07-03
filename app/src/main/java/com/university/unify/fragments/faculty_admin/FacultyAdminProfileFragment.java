package com.university.unify.fragments.faculty_admin;

import android.app.Activity;
import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FacultyAdminProfileFragment extends Fragment {

    private ShapeableImageView imageProfile;
    private TextView           textInitial;
    private TextView           buttonChangePhoto;

    private TextView       textName;
    private TextView       textEmail;
    private TextView       textFaculty;
    private TextView       textRole;
    private ProgressBar    progressBar;
    private MaterialButton buttonLogout;
    private MaterialButton buttonOpenCalendar;   // NEW

    private String currentUserId        = "";
    private String currentProfileImageUrl = "";

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public FacultyAdminProfileFragment() {
        super(R.layout.fragment_faculty_admin_profile);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null) {
                        Uri imageUri = result.getData().getData();

                        if (imageUri != null) {
                            imageProfile.setImageURI(imageUri);
                            textInitial.setVisibility(View.GONE);
                            uploadProfileImage(imageUri);
                        }
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageProfile       = view.findViewById(R.id.imageFacultyAdminProfile);
        textInitial        = view.findViewById(R.id.textFacultyAdminProfileInitial);
        buttonChangePhoto  = view.findViewById(R.id.buttonFacultyAdminChangePhoto);
        textName           = view.findViewById(R.id.textFacultyAdminName);
        textEmail          = view.findViewById(R.id.textFacultyAdminEmail);
        textFaculty        = view.findViewById(R.id.textFacultyAdminFaculty);
        textRole           = view.findViewById(R.id.textFacultyAdminRole);
        progressBar        = view.findViewById(R.id.progressBarFacultyAdminProfile);
        buttonLogout       = view.findViewById(R.id.buttonFacultyAdminLogout);
        buttonOpenCalendar = view.findViewById(R.id.buttonFacultyAdminOpenCalendar);  // NEW

        buttonLogout.setOnClickListener(v -> logout());
        imageProfile.setOnClickListener(v -> openImagePicker());
        buttonChangePhoto.setOnClickListener(v -> openImagePicker());
        buttonOpenCalendar.setOnClickListener(v -> openCalendar());  // NEW

        loadProfile();
        checkIfAlsoInstructor();  // NEW — show calendar button only when relevant
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NEW: check whether this faculty admin is also assigned as an instructor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calls GET_INSTRUCTOR_COURSES with the current user's ID.
     * If the server returns at least one course, this admin also teaches,
     * so we reveal the calendar button. Otherwise it stays hidden.
     *
     * This is a lightweight fire-and-forget check — no loading spinner,
     * no error toast. If it fails, the button simply stays hidden.
     */
    private void checkIfAlsoInstructor() {
        DatabaseHelper db = new DatabaseHelper(requireContext());
        String userId = safeRaw(db.getLoggedInUserId());

        if (TextUtils.isEmpty(userId)) return;

        String url = ApiConfig.GET_INSTRUCTOR_COURSES + "?instructor_id=" + userId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    if (!isAdded()) return;

                    try {
                        JSONObject obj  = new JSONObject(cleanJson(response));
                        boolean success = obj.optBoolean("success", false);

                        if (!success) return;

                        JSONArray data = obj.optJSONArray("data");

                        // Show the calendar button only if at least one course exists
                        if (data != null && data.length() > 0) {
                            buttonOpenCalendar.setVisibility(View.VISIBLE);
                        }

                    } catch (Exception ignored) {
                        // silent — button stays hidden
                    }
                },
                error -> {
                    // silent — button stays hidden
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NEW: open the shared calendar fragment
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Opens CourseCalendarFragment exactly like the instructor profile does.
     * CourseCalendarFragment reads `role` from SQLite; since role != "STUDENT"
     * it will call GET_INSTRUCTOR_COURSES — which is exactly what we want.
     */
    private void openCalendar() {
        if (!isAdded()) return;

        View parent = (View) requireView().getParent();

        getParentFragmentManager()
                .beginTransaction()
                .replace(parent.getId(), new CourseCalendarFragment())
                .addToBackStack(null)
                .commit();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Existing profile logic — unchanged
    // ─────────────────────────────────────────────────────────────────────────

    private void loadProfile() {
        showLoading(true);

        DatabaseHelper db = new DatabaseHelper(requireContext());
        currentUserId = safeRaw(db.getLoggedInUserId());

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

                        if (!obj.optBoolean("success", false)) {
                            loadProfileFromSession();
                            return;
                        }

                        JSONObject data = obj.optJSONObject("data");

                        if (data == null) {
                            loadProfileFromSession();
                            return;
                        }

                        String fullName    = display(data.optString("full_name", ""));
                        String email       = display(data.optString("email", ""));
                        String facultyName = display(data.optString("faculty_name", ""));
                        String role        = display(data.optString("role", "FACULTY_ADMIN"));
                        String imageUrl    = safeRaw(data.optString("profile_image_url", ""));

                        bindProfile(fullName, email, facultyName, role);

                        currentProfileImageUrl = imageUrl;

                        if (!TextUtils.isEmpty(imageUrl)) {
                            loadProfileImage(imageUrl);
                        } else {
                            textInitial.setVisibility(View.VISIBLE);
                        }

                    } catch (Exception e) {
                        loadProfileFromSession();
                    }
                },
                error -> {
                    if (!isAdded()) return;

                    showLoading(false);
                    loadProfileFromSession();
                    Toast.makeText(requireContext(),
                            getString(R.string.error_loading_profile),
                            Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void loadProfileFromSession() {
        DatabaseHelper db = new DatabaseHelper(requireContext());

        String fullName = display(db.getLoggedInFullName());
        String email    = display(db.getLoggedInEmail());
        String role     = display(db.getLoggedInRole());

        bindProfile(fullName, email, "-", role.isEmpty() ? "FACULTY_ADMIN" : role);
    }

    private void bindProfile(String fullName, String email,
                             String facultyName, String role) {
        textInitial.setText(getInitial(fullName));
        textName.setText(fullName.isEmpty() ? "-" : fullName);
        textEmail.setText(email.isEmpty() ? "-" : email);
        textFaculty.setText(getString(R.string.faculty) + ": " +
                (facultyName.isEmpty() ? "-" : facultyName));
        textRole.setText(role.isEmpty() ? "FACULTY_ADMIN" : role);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(requireContext(),
                    getString(R.string.user_data_not_found),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] imageBytes = getBytesFromUri(imageUri);

        if (imageBytes == null) {
            Toast.makeText(requireContext(),
                    getString(R.string.failed_to_upload_image),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(requireContext(),
                getString(R.string.uploading_image), Toast.LENGTH_SHORT).show();
        showLoading(true);

        VolleyMultipartRequest request = new VolleyMultipartRequest(
                Request.Method.POST,
                ApiConfig.UPLOAD_PROFILE_IMAGE,
                this::handleUploadImageResponse,
                error -> {
                    if (!isAdded()) return;
                    showLoading(false);
                    Toast.makeText(requireContext(),
                            getString(R.string.failed_to_upload_image),
                            Toast.LENGTH_SHORT).show();
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
                params.put("profile_image",
                        new DataPart("profile_image.jpg", imageBytes, "image/jpeg"));
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
            String message  = obj.optString("message", success
                    ? getString(R.string.profile_image_updated)
                    : getString(R.string.failed_to_upload_image));

            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

            if (success) {
                String imageUrl = safeRaw(obj.optString("profile_image_url", ""));
                if (!TextUtils.isEmpty(imageUrl)) {
                    currentProfileImageUrl = imageUrl;
                    loadProfileImage(imageUrl);
                }
            }

        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    getString(R.string.failed_to_upload_image),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfileImage(String imageUrl) {
        if (!isAdded()) return;

        String cleanUrl = ImageLoaderUtil.normalizeImageUrl(imageUrl);

        if (TextUtils.isEmpty(cleanUrl)) {
            imageProfile.setImageResource(R.drawable.baseline_person_24);
            if (textInitial != null) textInitial.setVisibility(View.VISIBLE);
            return;
        }

        if (textInitial != null) textInitial.setVisibility(View.GONE);
        ImageLoaderUtil.loadImage(requireContext(), imageProfile, cleanUrl, false);
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

    private byte[] getBytesFromUri(Uri uri) {
        try {
            InputStream inputStream =
                    requireContext().getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;

            while (inputStream != null && (len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }

            if (inputStream != null) inputStream.close();

            return byteBuffer.toByteArray();

        } catch (Exception e) {
            return null;
        }
    }

    private String getInitial(String value) {
        if (value == null || value.trim().isEmpty() || value.equals("-")) {
            return getString(R.string.participant_default_initial);
        }
        return value.trim().substring(0, 1).toUpperCase();
    }

    private String display(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return "-";
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
        if (response == null) return "{}";
        String clean = response.trim();
        int start = clean.indexOf("{");
        int end   = clean.lastIndexOf("}");
        if (start != -1 && end != -1 && end > start) {
            clean = clean.substring(start, end + 1);
        }
        return clean;
    }
}