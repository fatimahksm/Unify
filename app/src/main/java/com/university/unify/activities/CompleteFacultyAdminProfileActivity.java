package com.university.unify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.university.unify.R;
import com.university.unify.network.ApiConfig;

import java.util.HashMap;
import java.util.Map;

public class CompleteFacultyAdminProfileActivity extends AppCompatActivity {

    private TextInputLayout layoutFirstName;
    private TextInputLayout layoutLastName;
    private TextInputLayout layoutPhone;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutConfirmPassword;

    private TextInputEditText editFirstName;
    private TextInputEditText editLastName;
    private TextInputEditText editPhone;
    private TextInputEditText editPassword;
    private TextInputEditText editConfirmPassword;

    private MaterialButton buttonSave;
    private ProgressBar progressBar;

    private RequestQueue queue;
    private boolean isLoading = false;

    private String userId = "";
    private String email = "";

    private String role = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_faculty_admin_profile);

        queue = Volley.newRequestQueue(this);

        userId = getIntent().getStringExtra("user_id");
        email = getIntent().getStringExtra("email");

        if (userId == null) userId = "";
        if (email == null) email = "";

        initViews();
        setupListeners();


        role = getIntent().getStringExtra("role");
        if (role == null) role = "";
    }

    private void initViews() {
        layoutFirstName = findViewById(R.id.layoutFirstName);
        layoutLastName = findViewById(R.id.layoutLastName);
        layoutPhone = findViewById(R.id.layoutPhone);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword);

        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editPhone = findViewById(R.id.editPhone);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);

        buttonSave = findViewById(R.id.buttonSaveProfile);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isLoading) {
                    validateAndSave();
                }
            }
        });
    }

    private void validateAndSave() {
        layoutFirstName.setError(null);
        layoutLastName.setError(null);
        layoutPhone.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);

        String firstName = getText(editFirstName);
        String lastName = getText(editLastName);
        String phone = getText(editPhone);
        String password = getText(editPassword);
        String confirmPassword = getText(editConfirmPassword);

        boolean valid = true;

        if (TextUtils.isEmpty(firstName)) {
            layoutFirstName.setError(getString(R.string.error_empty_first_name));
            valid = false;
        }

        if (TextUtils.isEmpty(lastName)) {
            layoutLastName.setError(getString(R.string.error_empty_last_name));
            valid = false;
        }

        if (TextUtils.isEmpty(phone)) {
            layoutPhone.setError(getString(R.string.error_empty_phone));
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            layoutPassword.setError(getString(R.string.error_empty_password));
            valid = false;
        } else if (password.length() < 6) {
            layoutPassword.setError(getString(R.string.error_password_short));
            valid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            layoutConfirmPassword.setError(getString(R.string.error_confirm_password));
            valid = false;
        } else if (!confirmPassword.equals(password)) {
            layoutConfirmPassword.setError(getString(R.string.error_password_mismatch));
            valid = false;
        }

        if (!valid) return;

        updateFirebasePassword(firstName, lastName, phone, password);
    }

    private void updateFirebasePassword(final String firstName,
                                        final String lastName,
                                        final String phone,
                                        final String newPassword) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, getString(R.string.error_user_data_not_found), Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        currentUser.updatePassword(newPassword)
                .addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(com.google.android.gms.tasks.Task<Void> task) {
                        if (task.isSuccessful()) {
                            saveProfileToMySql(firstName, lastName, phone, newPassword);
                        } else {
                            setLoading(false);
                            Toast.makeText(
                                    CompleteFacultyAdminProfileActivity.this,
                                    getString(R.string.error_update_password),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                });
    }

    private void saveProfileToMySql(final String firstName,
                                    final String lastName,
                                    final String phone,
                                    final String newPassword){

        String url = role.equals("INSTRUCTOR")
                ? ApiConfig.COMPLETE_INSTRUCTOR_PROFILE
                : ApiConfig.COMPLETE_FACULTY_ADMIN_PROFILE;

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        setLoading(false);

                        if (response.contains("Profile completed")) {
                            Toast.makeText(
                                    CompleteFacultyAdminProfileActivity.this,
                                    getString(R.string.profile_completed_successfully),
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent;

                            if (role.equals("INSTRUCTOR")) {
                                intent = new Intent(
                                        CompleteFacultyAdminProfileActivity.this,
                                        InstructorMainActivity.class
                                );
                            } else {
                                intent = new Intent(
                                        CompleteFacultyAdminProfileActivity.this,
                                        FacultyAdminMainActivity.class
                                );
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(
                                    CompleteFacultyAdminProfileActivity.this,
                                    response,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setLoading(false);
                        Toast.makeText(
                                CompleteFacultyAdminProfileActivity.this,
                                getString(R.string.error_complete_profile),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        ) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                String fullName = firstName + " " + lastName;

                params.put("user_id", userId);
                params.put("full_name", fullName);
                params.put("phone_number", phone);
                params.put("new_password", newPassword);
                params.put("fkey", "2026");

                return params;
            }
        };

        queue.add(request);
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        buttonSave.setEnabled(!loading);
        editFirstName.setEnabled(!loading);
        editLastName.setEnabled(!loading);
        editPhone.setEnabled(!loading);
        editPassword.setEnabled(!loading);
        editConfirmPassword.setEnabled(!loading);

        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        buttonSave.setText(loading ? "" : getString(R.string.save_profile));
    }

    private String getText(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }
}