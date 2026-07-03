package com.university.unify.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.firebase.auth.FirebaseUser;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.auth.AuthHelper;

import com.university.unify.network.ApiConfig;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "unify_prefs";
    private static final String KEY_LANGUAGE = "app_language";

    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private TextInputEditText editEmail;
    private TextInputEditText editPassword;
    private MaterialButton buttonLogin;
    private MaterialButton buttonLanguage;
    private TextView textSignUp;
    private TextView textForgotPassword;

    private final AuthHelper authHelper = new AuthHelper();
    private boolean isLoading = false;

    private RequestQueue queue;

    DatabaseHelper db;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);



        queue = Volley.newRequestQueue(this);
        initViews();
        updateLanguageButtonText();
        setupListeners();
    }

    private void initViews() {
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonLanguage = findViewById(R.id.buttonLanguage);
        textSignUp = findViewById(R.id.textSignUp);
        textForgotPassword = findViewById(R.id.textForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        buttonLogin.setOnClickListener(v -> {
            if (!isLoading) {
                validateAndLogin();
            }
        });

        buttonLanguage.setOnClickListener(v -> {
            if (!isLoading) {
                toggleLanguage();
            }
        });

        textSignUp.setOnClickListener(v -> {
            if (isLoading) return;
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        textForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.forgot_password_coming_soon), Toast.LENGTH_SHORT).show()
        );
    }

    private void validateAndLogin() {
        layoutEmail.setError(null);
        layoutPassword.setError(null);

        String email = editEmail.getText() != null ? editEmail.getText().toString().trim() : "";
        String password = editPassword.getText() != null ? editPassword.getText().toString().trim() : "";

        boolean isValid = true;

        if (email.isEmpty()) {
            layoutEmail.setError(getString(R.string.error_empty_email));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }

        if (password.isEmpty()) {
            layoutPassword.setError(getString(R.string.error_empty_password));
            isValid = false;
        }

        if (!isValid) return;

        setLoading(true);

        authHelper.loginUser(email, password, new AuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                if (firebaseUser == null) {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this, getString(R.string.error_general), Toast.LENGTH_LONG).show();
                    return;
                }

                loginUserFromMySql(firebaseUser);
            }


            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);

                Toast.makeText(
                        LoginActivity.this,
                        errorMessage != null ? errorMessage : getString(R.string.error_login_failed),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void loginUserFromMySql(final FirebaseUser firebaseUser) {
        final String email = firebaseUser.getEmail();
        final String firebaseUid = firebaseUser.getUid();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.LOGIN_USER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        setLoading(false);

                        try {
                            org.json.JSONObject obj = new org.json.JSONObject(response);

                            boolean success = obj.optBoolean("success", false);
                            String status = obj.optString("status", "");
                            String role = obj.optString("role", "");
                            String mustChangePassword = obj.optString("must_change_password", "0");
                            String profileCompleted = obj.optString("profile_completed", "1");
                            String userId = obj.optString("user_id", "");
                            String savedFirebaseUid = obj.optString("firebase_uid", firebaseUid);
                            String fullName = obj.optString("full_name", "");
                            String facultyId = obj.optString("faculty_id", "");
                            String majorId = obj.optString("major_id", "");
                            String studyYear = obj.optString("study_year", "");
                            String isApproved = obj.optString("is_approved", "1");

                            if (!success) {
                                if (status.equals("NOT_FOUND")) {
                                    authHelper.logout();
                                    Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (status.equals("INACTIVE")) {
                                    authHelper.logout();
                                    Toast.makeText(LoginActivity.this, "Account inactive", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (status.equals("PENDING")) {
                                    authHelper.logout();

                                    db.saveLoggedInUser(
                                            obj.optString("user_id", ""),
                                            firebaseUid,
                                            obj.optString("full_name", ""),
                                            email,
                                            obj.optString("role", "STUDENT"),
                                            obj.optString("faculty_id", ""),
                                            obj.optString("major_id", ""),
                                            obj.optString("study_year", ""),
                                            "0"
                                    );

                                    Intent intent = new Intent(LoginActivity.this, PendingApprovalActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                    return;
                                }

                                authHelper.logout();
                                Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_LONG).show();
                                return;
                            }

                            db.saveLoggedInUser(
                                    userId,
                                    savedFirebaseUid,
                                    fullName,
                                    email,
                                    role,
                                    facultyId,
                                    majorId,
                                    studyYear,
                                    isApproved
                            );

                            if ((role.equals("FACULTY_ADMIN") || role.equals("INSTRUCTOR"))
                                    && (mustChangePassword.equals("1") || profileCompleted.equals("0"))) {

                                Intent intent = new Intent(LoginActivity.this, CompleteFacultyAdminProfileActivity.class);
                                intent.putExtra("user_id", userId);
                                intent.putExtra("email", email);
                                intent.putExtra("role", role);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                                return;
                            }
                            openScreenByRole(role);

                        } catch (Exception e) {
                            authHelper.logout();
                            Toast.makeText(LoginActivity.this, "Invalid login response", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        setLoading(false);
                        authHelper.logout();
                        Toast.makeText(LoginActivity.this, "Login error", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email == null ? "" : email);
                params.put("firebase_uid", firebaseUid == null ? "" : firebaseUid);
                return params;
            }
        };

        queue.add(request);
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        buttonLogin.setEnabled(!loading);
        buttonLanguage.setEnabled(!loading);
        textSignUp.setEnabled(!loading);
        textForgotPassword.setEnabled(!loading);
        editEmail.setEnabled(!loading);
        editPassword.setEnabled(!loading);

        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        buttonLogin.setText(loading ? "" : getString(R.string.sign_in));
    }



    private void toggleLanguage() {
        String currentLang = getSavedLanguage();
        String newLang = currentLang.equals("en") ? "ar" : "en";

        saveLanguage(newLang);
        setAppLocale(newLang);
        recreate();
    }

    private void updateLanguageButtonText() {
        String currentLang = getSavedLanguage();
        buttonLanguage.setText(currentLang.equals("en")
                ? getString(R.string.language_ar)
                : getString(R.string.language_en));
    }

    private void applySavedLanguage() {
        String lang = getSavedLanguage();
        setAppLocale(lang);
    }

    private String getSavedLanguage() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getString(KEY_LANGUAGE, "en");
    }

    private void saveLanguage(String languageCode) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    private void setAppLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
    }



    private void openScreenByRole(String role) {
        Intent intent;

        switch (role) {
            case "SUPER_ADMIN":
                intent = new Intent(this, AdminMainActivity.class);
                break;

            case "FACULTY_ADMIN":
                intent = new Intent(this, FacultyAdminMainActivity.class);
                break;

            case "INSTRUCTOR":
                intent = new Intent(this, InstructorMainActivity.class);
                break;

            default:
                intent = new Intent(this, StudentMainActivity.class);
                break;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}