package com.university.unify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

import com.university.unify.R;
import com.university.unify.adapter.RegisterStepsPagerAdapter;
import com.university.unify.auth.AuthHelper;

import com.university.unify.model.FacultyModel;
import com.university.unify.model.MajorModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterDebug";
    private static final long REGISTER_TIMEOUT_MS = 15000L;

    private ViewPager2 registerViewPager;
    private MaterialButton buttonBack, buttonNext;
    private TextView textSignIn;
    private TextView textStep1Circle, textStep2Circle, textStep3Circle;

    private RegisterStepsPagerAdapter pagerAdapter;
    private final AuthHelper authHelper = new AuthHelper();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRegistering = false;
    private Runnable registerTimeoutRunnable;

    private RequestQueue queue;

    // Step 1
    private TextInputLayout layoutFirstName, layoutLastName, layoutStudentId;
    private TextInputEditText editFirstName, editLastName, editStudentId;

    // Step 2
    private TextInputLayout layoutFaculty, layoutMajor, layoutStudyYear, layoutPhone;
    private AutoCompleteTextView autoFaculty, autoMajor, autoStudyYear;
    private TextInputEditText editPhone;

    // Step 3
    private TextInputLayout layoutEmail, layoutPassword, layoutConfirmPassword;
    private TextInputEditText editEmail, editPassword, editConfirmPassword;

    private final List<FacultyModel> facultyList = new ArrayList<>();
    private final List<String> facultyNames = new ArrayList<>();

    private final List<MajorModel> allMajors = new ArrayList<>();
    private final List<MajorModel> filteredMajors = new ArrayList<>();
    private final List<String> majorNames = new ArrayList<>();

    private String selectedFacultyId = "";
    private String selectedFacultyName = "";

    private String selectedMajorId = "";
    private String selectedMajorName = "";

    private String selectedStudyYear = "";

    private ProgressBar progressBar;

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);
        queue = Volley.newRequestQueue(this);

        initViews();
        setupPager();
        setupListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearRegisterTimeout();
    }

    private void initViews() {
        registerViewPager = findViewById(R.id.registerViewPager);
        buttonBack = findViewById(R.id.buttonBack);
        buttonNext = findViewById(R.id.buttonNext);
        textSignIn = findViewById(R.id.textSignIn);

        textStep1Circle = findViewById(R.id.textStep1Circle);
        textStep2Circle = findViewById(R.id.textStep2Circle);
        textStep3Circle = findViewById(R.id.textStep3Circle);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupPager() {
        pagerAdapter = new RegisterStepsPagerAdapter(this);
        registerViewPager.setAdapter(pagerAdapter);
        registerViewPager.setOffscreenPageLimit(3);
        registerViewPager.setUserInputEnabled(false);

        registerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateStepUi(position);
            }
        });

        registerViewPager.post(this::bindStepViewsWhenReady);
    }

    private void bindStepViewsWhenReady() {
        View step1 = pagerAdapter.getStepView(0);
        View step2 = pagerAdapter.getStepView(1);
        View step3 = pagerAdapter.getStepView(2);

        if (step1 == null || step2 == null || step3 == null) {
            registerViewPager.post(this::bindStepViewsWhenReady);
            return;
        }

        layoutFirstName = step1.findViewById(R.id.layoutFirstName);
        layoutLastName = step1.findViewById(R.id.layoutLastName);
        layoutStudentId = step1.findViewById(R.id.layoutStudentId);

        editFirstName = step1.findViewById(R.id.editFirstName);
        editLastName = step1.findViewById(R.id.editLastName);
        editStudentId = step1.findViewById(R.id.editStudentId);

        layoutFaculty = step2.findViewById(R.id.layoutFaculty);
        layoutMajor = step2.findViewById(R.id.layoutMajor);
        layoutPhone = step2.findViewById(R.id.layoutPhone);

        autoFaculty = step2.findViewById(R.id.autoFaculty);
        autoMajor = step2.findViewById(R.id.autoMajor);

        editPhone = step2.findViewById(R.id.editPhone);

        layoutEmail = step3.findViewById(R.id.layoutEmail);
        layoutPassword = step3.findViewById(R.id.layoutPassword);
        layoutConfirmPassword = step3.findViewById(R.id.layoutConfirmPassword);

        editEmail = step3.findViewById(R.id.editEmail);
        editPassword = step3.findViewById(R.id.editPassword);
        editConfirmPassword = step3.findViewById(R.id.editConfirmPassword);

        setupFacultyDropdown();
        setupMajorDropdown();
        loadFaculties();
    }

    private void setupListeners() {
        buttonBack.setOnClickListener(v -> {
            if (isRegistering) return;

            int current = registerViewPager.getCurrentItem();
            if (current > 0) {
                registerViewPager.setCurrentItem(current - 1, true);
            }
        });

        buttonNext.setOnClickListener(v -> {
            if (isRegistering) return;

            int current = registerViewPager.getCurrentItem();

            if (!validateCurrentStep(current)) {
                Log.d(TAG, "Validation failed on step = " + current);
                return;
            }

            if (current < 2) {
                registerViewPager.setCurrentItem(current + 1, true);
            } else {
                registerStudent();
            }
        });

        textSignIn.setOnClickListener(v -> {
            if (!isRegistering) {
                finish();
            }
        });
    }

    private void setupFacultyDropdown() {
        autoFaculty.setOnItemClickListener((parent, view, position, id) -> {
            String facultyName = (String) parent.getItemAtPosition(position);

            for (FacultyModel faculty : facultyList) {
                if (faculty.getName().equals(facultyName)) {
                    selectedFacultyId = faculty.getFacultyId();
                    selectedFacultyName = faculty.getName();
                    break;
                }
            }

            clearSelectedMajor();
            loadMajors();
        });
    }

    private void setupMajorDropdown() {
        autoMajor.setOnItemClickListener((parent, view, position, id) -> {
            String majorName = (String) parent.getItemAtPosition(position);

            for (MajorModel major : allMajors) {
                if (major.getName().equals(majorName)) {
                    selectedMajorId = major.getMajorId();
                    selectedMajorName = major.getName();
                    break;
                }
            }
        });
    }


    private void updateStepUi(int position) {
        buttonBack.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);

        if (position == 2) {
            buttonNext.setText(isRegistering
                    ? getString(R.string.loading)
                    : getString(R.string.register_student));
        } else {
            buttonNext.setText(getString(R.string.next));
        }

        updateStepCircle(textStep1Circle, position == 0);
        updateStepCircle(textStep2Circle, position == 1);
        updateStepCircle(textStep3Circle, position == 2);
    }

    private void updateStepCircle(TextView textView, boolean active) {
        textView.setBackgroundResource(active ? R.drawable.bg_step_active : R.drawable.bg_step_inactive);
        textView.setTextColor(ContextCompat.getColor(
                this,
                active ? R.color.white : R.color.unify_text_secondary
        ));
    }

    private void loadFaculties() {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                ApiConfig.GET_FACULTIES,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        facultyList.clear();
                        facultyNames.clear();

                        for (int i=0; i<jsonArray.length();i++){
                            JSONObject obj = null;
                            try {
                                obj = jsonArray.getJSONObject(i);


                            FacultyModel f = new FacultyModel();
                            f.setFacultyId(obj.optString("faculty_id"));
                            f.setName(obj.optString("name"));
                            f.setCode(obj.optString("code"));
                            f.setActive(true);

                            facultyList.add(f);
                            facultyNames.add(f.getName());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                RegisterActivity.this,
                                android.R.layout.simple_list_item_1,
                                facultyNames
                        );

                        autoFaculty.setAdapter(adapter);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(RegisterActivity.this, volleyError.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        );
        queue.add(request);
    }

    private void loadMajors() {
        if (selectedFacultyId == null || selectedFacultyId.trim().isEmpty()) {
            Toast.makeText(this, "Please select a faculty first", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.GET_MAJORS_BY_FACULTY + "?faculty_id=" + selectedFacultyId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        allMajors.clear();
                        majorNames.clear();

                        boolean success = response.optBoolean("success", false);
                        String message = response.optString("message", "Failed to load majors");

                        if (!success) {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                            autoMajor.setAdapter(null);
                            return;
                        }

                        JSONArray data = response.optJSONArray("data");

                        if (data == null || data.length() == 0) {
                            Toast.makeText(RegisterActivity.this, "No majors found for this faculty", Toast.LENGTH_SHORT).show();
                            autoMajor.setAdapter(null);
                            return;
                        }

                        for (int i = 0; i < data.length(); i++) {
                            try {
                                JSONObject obj = data.getJSONObject(i);

                                MajorModel major = new MajorModel();
                                major.setMajorId(obj.optString("major_id"));
                                major.setFacultyId(obj.optString("faculty_id"));
                                major.setFacultyName(obj.optString("faculty_name"));
                                major.setName(obj.optString("name"));
                                major.setCode(obj.optString("code"));
                                major.setActive("1".equals(obj.optString("is_active")));

                                allMajors.add(major);
                                majorNames.add(major.getName());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                RegisterActivity.this,
                                android.R.layout.simple_dropdown_item_1line,
                                majorNames
                        );

                        autoMajor.setAdapter(adapter);
                        autoMajor.showDropDown();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(
                                RegisterActivity.this,
                                "Failed to load majors",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        queue.add(request);
    }
    private void filterMajorsByFaculty(String facultyId) {
        filteredMajors.clear();
        majorNames.clear();

        for (MajorModel major : allMajors) {
            if (facultyId.equals(safe(major.getFacultyId()))) {
                filteredMajors.add(major);
                majorNames.add(safe(major.getName()));
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                majorNames
        );
        autoMajor.setAdapter(adapter);

        Log.d(TAG, "Filtered majors count = " + majorNames.size() + " for facultyId = " + facultyId);
    }

    private void clearSelectedMajor() {
        selectedMajorId = "";
        selectedMajorName = "";
        autoMajor.setText("", false);
        layoutMajor.setError(null);
        majorNames.clear();
        allMajors.clear();
        autoMajor.setAdapter(null);
    }

    private boolean validateCurrentStep(int step) {
        switch (step) {
            case 0:
                return validateStepOne();
            case 1:
                return validateStepTwo();
            case 2:
                return validateStepThree();
            default:
                return false;
        }
    }

    private boolean validateStepOne() {
        layoutFirstName.setError(null);
        layoutLastName.setError(null);
        layoutStudentId.setError(null);

        String firstName = getText(editFirstName);
        String lastName = getText(editLastName);
        String studentId = getText(editStudentId);

        boolean valid = true;

        if (firstName.isEmpty()) {
            layoutFirstName.setError(getString(R.string.error_empty_first_name));
            valid = false;
        }

        if (lastName.isEmpty()) {
            layoutLastName.setError(getString(R.string.error_empty_last_name));
            valid = false;
        }

        if (studentId.isEmpty()) {
            layoutStudentId.setError(getString(R.string.error_empty_student_id));
            valid = false;
        }

        return valid;
    }

    private boolean validateStepTwo() {
        layoutFaculty.setError(null);
        layoutMajor.setError(null);
        layoutPhone.setError(null);

        String phone = getText(editPhone);

        boolean valid = true;

        if (selectedFacultyId.isEmpty()) {
            layoutFaculty.setError(getString(R.string.error_empty_faculty));
            valid = false;
        }

        if (selectedMajorId.isEmpty() || selectedMajorName.isEmpty()) {
            layoutMajor.setError(getString(R.string.error_empty_major));
            valid = false;
        }

        if (phone.isEmpty()) {
            layoutPhone.setError(getString(R.string.error_empty_phone));
            valid = false;
        }

        return valid;
    }

    private boolean validateStepThree() {
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);

        String email = getText(editEmail);
        String password = getText(editPassword);
        String confirmPassword = getText(editConfirmPassword);

        boolean valid = true;

        if (email.isEmpty()) {
            layoutEmail.setError(getString(R.string.error_empty_email));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        }

        if (password.isEmpty()) {
            layoutPassword.setError(getString(R.string.error_empty_password));
            valid = false;
        } else if (password.length() < 6) {
            layoutPassword.setError(getString(R.string.error_password_short));
            valid = false;
        }

        if (confirmPassword.isEmpty()) {
            layoutConfirmPassword.setError(getString(R.string.error_confirm_password));
            valid = false;
        } else if (!confirmPassword.equals(password)) {
            layoutConfirmPassword.setError(getString(R.string.error_password_mismatch));
            valid = false;
        }

        return valid;
    }

    private void registerStudent() {
        if (isRegistering) return;

        String firstName = getText(editFirstName);
        String lastName = getText(editLastName);
        String studentId = getText(editStudentId);
        String phone = getText(editPhone);
        String email = getText(editEmail);
        String password = getText(editPassword);

        Log.d(TAG, "Starting register for email = " + email);

        isRegistering = true;

        setLoading(true);

        registerTimeoutRunnable = () -> {
            if (isRegistering) {
                Log.e(TAG, "Register timeout reached");
                isRegistering = false;
                setLoading(false);
                Toast.makeText(
                        RegisterActivity.this,
                        getString(R.string.error_registration_timeout),
                        Toast.LENGTH_LONG
                ).show();
            }
        };
        handler.postDelayed(registerTimeoutRunnable, REGISTER_TIMEOUT_MS);

        authHelper.registerUser(email, password, new AuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                clearRegisterTimeout();

                if (firebaseUser == null) {
                    isRegistering = false;
                    setLoading(false);
                    Toast.makeText(RegisterActivity.this, getString(R.string.error_general), Toast.LENGTH_SHORT).show();
                    return;
                }

                saveUserToMySql(firebaseUser);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Auth failed: " + errorMessage);

                clearRegisterTimeout();

                isRegistering = false;
                setLoading(false);

                Toast.makeText(
                        RegisterActivity.this,
                        errorMessage != null ? errorMessage : getString(R.string.error_registration_failed),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        buttonBack.setEnabled(!loading);
        buttonNext.setEnabled(!loading);
        textSignIn.setEnabled(!loading);
        registerViewPager.setUserInputEnabled(!loading);

        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);

        if (loading) {
            buttonNext.setText("");
        } else {
            int current = registerViewPager.getCurrentItem();
            buttonNext.setText(current == 2
                    ? getString(R.string.register_student)
                    : getString(R.string.next));
        }
    }

    private void clearRegisterTimeout() {
        if (registerTimeoutRunnable != null) {
            handler.removeCallbacks(registerTimeoutRunnable);
            registerTimeoutRunnable = null;
        }
    }

    private void saveUserToMySql(FirebaseUser firebaseUser) {
        String firstName = getText(editFirstName);
        String lastName = getText(editLastName);
        String fullName = firstName + " " + lastName;
        String studentId = getText(editStudentId);
        String phone = getText(editPhone);
        String email = getText(editEmail);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ApiConfig.REGISTER_USER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        isRegistering = false;
                        setLoading(false);

                        Toast.makeText(RegisterActivity.this, response, Toast.LENGTH_LONG).show();

                        if (response.contains("User registered")) {
                            authHelper.logout();


                            db.saveLoggedInUser(email, "STUDENT", "0");

                            Intent intent = new Intent(RegisterActivity.this, PendingApprovalActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            firebaseUser.delete();
                        }
                    }},
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        isRegistering = false;
                        setLoading(false);

                        Toast.makeText(RegisterActivity.this, "Failed to save user in MySQL", Toast.LENGTH_LONG).show();

                        firebaseUser.delete();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("firebase_uid", firebaseUser.getUid());
                params.put("full_name", fullName);
                params.put("email", email);
                params.put("role", "STUDENT");
                params.put("study_year","1");
                params.put("allowed_credits", "18");
                params.put("student_number", studentId);
                params.put("faculty_id", selectedFacultyId);
                params.put("major_id", selectedMajorId);
                params.put("phone_number", phone);

                return params;
            }
        };

        queue.add(stringRequest);
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}