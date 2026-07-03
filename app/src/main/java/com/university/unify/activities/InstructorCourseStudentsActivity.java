package com.university.unify.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.adapter.InstructorStudentAdapter;
import com.university.unify.constants.ChatConstants;
import com.university.unify.model.InstructorStudentModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class InstructorCourseStudentsActivity extends AppCompatActivity {

    private TextView textTitle;
    private TextView textSubtitle;
    private TextView textEmpty;
    private ProgressBar progressBar;
    private RecyclerView recyclerStudents;

    private InstructorStudentAdapter adapter;
    private final java.util.List<InstructorStudentModel> studentList =
            new java.util.ArrayList<InstructorStudentModel>();

    private String courseId = "";
    private String courseTitle = "";
    private String courseCode = "";
    private String instructorId = "";
    private String instructorName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_course_students);

        courseId = safe(getIntent().getStringExtra("course_id"));
        courseTitle = safe(getIntent().getStringExtra("course_title"));
        courseCode = safe(getIntent().getStringExtra("course_code"));

        loadInstructorFromSession();

        initViews();
        setupRecycler();
        loadStudents();
    }

    private void loadInstructorFromSession() {
        DatabaseHelper db = new DatabaseHelper(this);

        instructorId = safe(db.getLoggedInUserId());
        instructorName = safe(db.getLoggedInFullName());

        if (instructorName.isEmpty()) {
            instructorName = safe(db.getLoggedInEmail());
        }

        if (instructorName.isEmpty()) {
            instructorName = getString(R.string.instructor);
        }
    }

    private void initViews() {
        textTitle = findViewById(R.id.textCourseStudentsTitle);
        textSubtitle = findViewById(R.id.textCourseStudentsSubtitle);
        textEmpty = findViewById(R.id.textEmptyCourseStudents);
        progressBar = findViewById(R.id.progressBarCourseStudents);
        recyclerStudents = findViewById(R.id.recyclerCourseStudents);

        if (!TextUtils.isEmpty(courseTitle)) {
            textTitle.setText(courseTitle);
        }

        if (!TextUtils.isEmpty(courseCode)) {
            textSubtitle.setText(courseCode + " • " + getString(R.string.enrolled_students));
        }
    }

    private void setupRecycler() {
        adapter = new InstructorStudentAdapter(
                this,
                studentList,
                new InstructorStudentAdapter.OnStudentActionListener() {
                    @Override
                    public void onSetGradeClicked(InstructorStudentModel student) {
                        showSetGradeDialog(student);
                    }

                    @Override
                    public void onChatClicked(InstructorStudentModel student) {
                        openPrivateChatWithStudent(student);
                    }
                }
        );

        recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        recyclerStudents.setAdapter(adapter);
    }

    private void openPrivateChatWithStudent(InstructorStudentModel student) {
        if (student == null) {
            Toast.makeText(this, getString(R.string.unknown_student), Toast.LENGTH_SHORT).show();
            return;
        }

        String studentId = safe(student.getUserId());
        String studentName = safe(student.getFullName());

        if (TextUtils.isEmpty(instructorId)) {
            Toast.makeText(this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(studentId)) {
            Toast.makeText(this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        if (studentName.isEmpty()) {
            studentName = getString(R.string.unknown_student);
        }

        String chatId = ChatConstants.privateChatId(instructorId, studentId);

        Intent intent = new Intent(this, SingleChatActivity.class);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("chat_type", ChatConstants.TYPE_PRIVATE);
        intent.putExtra("other_user_id", studentId);
        intent.putExtra("other_user_name", studentName);
        intent.putExtra("chat_title", studentName);

        startActivity(intent);
    }

    private void loadStudents() {
        if (TextUtils.isEmpty(courseId)) {
            showEmpty(getString(R.string.error_course_not_found));
            return;
        }

        showLoading(true);

        String url = ApiConfig.GET_COURSE_STUDENTS + "?course_id=" + courseId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        handleLoadStudentsResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showEmpty(getString(R.string.error_loading_students));
                    }
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void handleLoadStudentsResponse(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            boolean success = obj.optBoolean("success", false);

            studentList.clear();

            if (!success) {
                showEmpty(obj.optString("message", getString(R.string.error_loading_students)));
                adapter.notifyDataSetChanged();
                return;
            }

            JSONArray data = obj.optJSONArray("data");

            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject item = data.getJSONObject(i);

                    InstructorStudentModel student = new InstructorStudentModel();

                    student.setUserId(item.optString("user_id", ""));
                    student.setFullName(item.optString("full_name", ""));
                    student.setEmail(item.optString("email", ""));
                    student.setPhoneNumber(item.optString("phone_number", ""));
                    student.setStudentNumber(item.optString("student_number", ""));
                    student.setStudyYear(item.optString("study_year", ""));
                    student.setProfileImageUrl(item.optString("profile_image_url", ""));
                    student.setEnrollmentId(item.optString("enrollment_id", ""));
                    student.setStatus(item.optString("status", ""));
                    student.setEnrolledAt(item.optString("enrolled_at", ""));

                    student.setFinalGrade(item.optString("final_grade", ""));
                    student.setResult(item.optString("result", ""));

                    studentList.add(student);
                }
            }

            adapter.notifyDataSetChanged();
            showLoading(false);

            if (studentList.isEmpty()) {
                showEmpty(getString(R.string.no_students_found));
            } else {
                recyclerStudents.setVisibility(View.VISIBLE);
                textEmpty.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            showEmpty(getString(R.string.error_loading_students));
        }
    }

    private void showSetGradeDialog(final InstructorStudentModel student) {
        if (student == null || TextUtils.isEmpty(student.getEnrollmentId())) {
            Toast.makeText(this, getString(R.string.error_enrollment_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        int padding = dp(20);
        layout.setPadding(padding, padding, padding, 0);

        final EditText editGrade = new EditText(this);
        editGrade.setHint(getString(R.string.grade_hint));
        editGrade.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        String oldGrade = safe(student.getFinalGrade());

        if (!TextUtils.isEmpty(oldGrade)) {
            editGrade.setText(oldGrade);
            editGrade.setSelection(oldGrade.length());
        }

        final AutoCompleteTextView autoResult = new AutoCompleteTextView(this);
        autoResult.setHint(getString(R.string.result));
        autoResult.setInputType(0);
        autoResult.setFocusable(false);

        String[] results = new String[]{"PASSED", "FAILED", "IN_PROGRESS"};

        ArrayAdapter<String> resultAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                results
        );

        autoResult.setAdapter(resultAdapter);
        autoResult.setThreshold(0);

        String oldResult = safe(student.getResult());

        if (!TextUtils.isEmpty(oldResult)) {
            autoResult.setText(oldResult, false);
        }

        autoResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoResult.showDropDown();
            }
        });

        layout.addView(editGrade);
        layout.addView(autoResult);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.set_grade_for, safe(student.getFullName())))
                .setView(layout)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String grade = editGrade.getText() == null
                        ? ""
                        : editGrade.getText().toString().trim();

                String result = autoResult.getText() == null
                        ? ""
                        : autoResult.getText().toString().trim();

                if (TextUtils.isEmpty(grade)) {
                    editGrade.setError(getString(R.string.error_required));
                    return;
                }

                double gradeValue;

                try {
                    gradeValue = Double.parseDouble(grade);
                } catch (Exception e) {
                    editGrade.setError(getString(R.string.invalid_grade));
                    return;
                }

                if (gradeValue < 0 || gradeValue > 100) {
                    editGrade.setError(getString(R.string.invalid_grade_range));
                    return;
                }

                if (TextUtils.isEmpty(result)) {
                    result = gradeValue >= 50 ? "PASSED" : "FAILED";
                }

                updateStudentGrade(student, grade, result, dialog);
            }
        });
    }

    private void updateStudentGrade(final InstructorStudentModel student,
                                    final String grade,
                                    final String result,
                                    final AlertDialog dialog) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.UPDATE_STUDENT_GRADE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        handleUpdateGradeResponse(response, dialog);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(
                                InstructorCourseStudentsActivity.this,
                                getString(R.string.error_updating_grade),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("enrollment_id", safe(student.getEnrollmentId()));
                params.put("grade", grade);
                params.put("result", result);

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void handleUpdateGradeResponse(String response, AlertDialog dialog) {
        try {
            JSONObject obj = new JSONObject(response);
            boolean success = obj.optBoolean("success", false);
            String message = obj.optString("message", getString(R.string.error_updating_grade));

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            if (success) {
                dialog.dismiss();
                loadStudents();
            }

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    getString(R.string.error_updating_grade),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerStudents.setVisibility(loading ? View.GONE : View.VISIBLE);
        textEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        recyclerStudents.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        textEmpty.setVisibility(View.VISIBLE);
        textEmpty.setText(message);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String safe(String value) {
        if (value == null || value.equalsIgnoreCase("null")) {
            return "";
        }

        return value.trim();
    }
}