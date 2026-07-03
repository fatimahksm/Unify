package com.university.unify.fragments.faculty_admin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.activities.AddCourseActivity;
import com.university.unify.adapter.CourseAdapter;
import com.university.unify.model.CourseModel;
import com.university.unify.model.MajorModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FacultyAdminCoursesFragment extends Fragment {

    private static final String TAG = "COURSES_DEBUG";
    private static final int REQUEST_ADD_COURSE = 2001;

    private RecyclerView recyclerCourses;
    private ProgressBar progressBar;
    private TextView textEmpty;
    private FloatingActionButton fabAddCourse;
    private TextInputEditText editCourseSearch;

    private CourseAdapter adapter;
    private RequestQueue queue;

    private final List<CourseModel> allCourses = new ArrayList<CourseModel>();
    private final List<CourseModel> courseList = new ArrayList<CourseModel>();

    private final List<MajorModel> majorList = new ArrayList<MajorModel>();
    private final List<String> majorNames = new ArrayList<String>();

    private String currentFacultyId = "";

    public FacultyAdminCoursesFragment() {
        super(R.layout.fragment_faculty_admin_courses);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerCourses = view.findViewById(R.id.recyclerCourses);
        progressBar = view.findViewById(R.id.progressBarCourses);
        textEmpty = view.findViewById(R.id.textEmptyCourses);
        fabAddCourse = view.findViewById(R.id.fabAddCourse);
        editCourseSearch = view.findViewById(R.id.editCourseSearch);

        queue = Volley.newRequestQueue(requireContext());

        adapter = new CourseAdapter(requireContext(), courseList);
        recyclerCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCourses.setAdapter(adapter);

        setupSearch();
        setupAddCourseButton();

        loadFacultyFromSessionThenData();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!TextUtils.isEmpty(currentFacultyId) && !currentFacultyId.equals("null")) {
            loadCourses();
        }
    }

    private void setupAddCourseButton() {
        fabAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddCourseActivity();
            }
        });
    }

    private void openAddCourseActivity() {
        if (TextUtils.isEmpty(currentFacultyId) || currentFacultyId.equals("null")) {
            Toast.makeText(
                    requireContext(),
                    getString(R.string.error_faculty_not_assigned),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (majorList.isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    getString(R.string.add_major_first),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Intent intent = new Intent(requireContext(), AddCourseActivity.class);
        startActivityForResult(intent, REQUEST_ADD_COURSE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_COURSE && resultCode == Activity.RESULT_OK) {
            loadCourses();
        }
    }

    private void setupSearch() {
        if (editCourseSearch == null) {
            return;
        }

        editCourseSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String value = "";

                if (s != null) {
                    value = s.toString();
                }

                filterCourses(value);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed.
            }
        });
    }

    private void filterCourses(String query) {
        String cleanQuery = "";

        if (query != null) {
            cleanQuery = query.trim().toLowerCase(Locale.ROOT);
        }

        courseList.clear();

        if (cleanQuery.isEmpty()) {
            courseList.addAll(allCourses);
        } else {
            for (int i = 0; i < allCourses.size(); i++) {
                CourseModel course = allCourses.get(i);

                if (courseMatches(course, cleanQuery)) {
                    courseList.add(course);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (courseList.isEmpty()) {
            recyclerCourses.setVisibility(View.GONE);
            textEmpty.setVisibility(View.VISIBLE);
            textEmpty.setText(getString(R.string.no_courses_found));
        } else {
            recyclerCourses.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.GONE);
        }
    }

    private boolean courseMatches(CourseModel course, String query) {
        String title = safe(course.getTitle()).toLowerCase(Locale.ROOT);
        String code = safe(course.getCode()).toLowerCase(Locale.ROOT);
        String major = safe(course.getMajorName()).toLowerCase(Locale.ROOT);
        String instructor = safe(course.getInstructorName()).toLowerCase(Locale.ROOT);
        String semester = safe(course.getSemester()).toLowerCase(Locale.ROOT);
        String year = safe(course.getAcademicYear()).toLowerCase(Locale.ROOT);
        String schedule = safe(course.getScheduleText()).toLowerCase(Locale.ROOT);
        String status = safe(course.getBestStatus()).toLowerCase(Locale.ROOT);

        return title.contains(query)
                || code.contains(query)
                || major.contains(query)
                || instructor.contains(query)
                || semester.contains(query)
                || year.contains(query)
                || schedule.contains(query)
                || status.contains(query);
    }

    private void loadFacultyFromSessionThenData() {
        showLoading(true);

        DatabaseHelper db = new DatabaseHelper(requireContext());
        currentFacultyId = safe(db.getLoggedInFacultyId());

        Log.d(TAG, "faculty_id from SQLite = " + currentFacultyId);

        if (TextUtils.isEmpty(currentFacultyId) || currentFacultyId.equals("null")) {
            showLoading(false);
            showEmpty(getString(R.string.error_faculty_not_assigned));
            return;
        }

        loadMajorsThenCourses();
    }

    private void loadMajorsThenCourses() {
        String url = ApiConfig.GET_MAJORS_BY_FACULTY + "?faculty_id=" + currentFacultyId;

        Log.d(TAG, "LOAD MAJORS URL = " + url);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) {
                            return;
                        }

                        Log.d(TAG, "MAJORS RESPONSE = " + response);

                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);

                            majorList.clear();
                            majorNames.clear();

                            if (!success) {
                                showLoading(false);
                                showEmpty(obj.optString(
                                        "message",
                                        getString(R.string.error_loading_courses)
                                ));
                                return;
                            }

                            JSONArray data = obj.optJSONArray("data");

                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject item = data.getJSONObject(i);

                                    MajorModel major = new MajorModel();
                                    major.setMajorId(item.optString("major_id", ""));
                                    major.setFacultyId(item.optString("faculty_id", ""));
                                    major.setFacultyName(item.optString("faculty_name", ""));
                                    major.setName(item.optString("name", ""));
                                    major.setCode(item.optString("code", ""));
                                    major.setActive("1".equals(item.optString("is_active", "1")));

                                    majorList.add(major);
                                    majorNames.add(major.getName());
                                }
                            }

                            loadCourses();

                        } catch (Exception e) {
                            Log.e(TAG, "MAJORS PARSE ERROR", e);
                            showLoading(false);
                            showEmpty(getString(R.string.error_loading_courses));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded()) {
                            return;
                        }

                        Log.e(TAG, "MAJORS VOLLEY ERROR = " + getVolleyErrorMessage(error));
                        showLoading(false);
                        showEmpty(getString(R.string.error_loading_courses));
                    }
                }
        );

        queue.add(request);
    }

    private void loadCourses() {
        String url = ApiConfig.GET_COURSES + "?faculty_id=" + currentFacultyId;

        Log.d(TAG, "LOAD COURSES URL = " + url);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) {
                            return;
                        }

                        Log.d(TAG, "COURSES RESPONSE = " + response);

                        try {
                            allCourses.clear();
                            courseList.clear();

                            JSONArray data;
                            String cleanResponse = "";

                            if (response != null) {
                                cleanResponse = response.trim();
                            }

                            if (cleanResponse.startsWith("[")) {
                                data = new JSONArray(cleanResponse);
                            } else {
                                JSONObject obj = new JSONObject(cleanResponse);
                                boolean success = obj.optBoolean("success", true);

                                if (!success) {
                                    showLoading(false);
                                    showEmpty(obj.optString(
                                            "message",
                                            getString(R.string.error_loading_courses)
                                    ));
                                    return;
                                }

                                data = obj.optJSONArray("data");
                            }

                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject item = data.getJSONObject(i);
                                    CourseModel course = mapCourse(item);
                                    allCourses.add(course);
                                }
                            }

                            courseList.addAll(allCourses);
                            adapter.notifyDataSetChanged();

                            showLoading(false);

                            if (courseList.isEmpty()) {
                                showEmpty(getString(R.string.no_courses_found));
                            } else {
                                recyclerCourses.setVisibility(View.VISIBLE);
                                textEmpty.setVisibility(View.GONE);
                            }

                            if (editCourseSearch != null) {
                                String currentQuery = "";

                                if (editCourseSearch.getText() != null) {
                                    currentQuery = editCourseSearch.getText().toString();
                                }

                                filterCourses(currentQuery);
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "COURSES PARSE ERROR", e);
                            showLoading(false);
                            showEmpty(getString(R.string.error_loading_courses));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded()) {
                            return;
                        }

                        Log.e(TAG, "COURSES VOLLEY ERROR = " + getVolleyErrorMessage(error));
                        showLoading(false);
                        showEmpty(getString(R.string.error_loading_courses));
                    }
                }
        );

        queue.add(request);
    }

    private CourseModel mapCourse(JSONObject item) {
        CourseModel course = new CourseModel();

        course.setCourseId(item.optString("course_id", ""));
        course.setTitle(item.optString("title", ""));
        course.setCode(item.optString("code", ""));
        course.setDescription(item.optString("description", ""));
        course.setSection(item.optString("section", ""));
        course.setDepartment(item.optString("department", ""));
        course.setFacultyId(item.optString("faculty_id", ""));
        course.setFacultyName(item.optString("faculty_name", ""));
        course.setMajorId(item.optString("major_id", ""));
        course.setMajorName(item.optString("major_name", ""));
        course.setStudyYear(item.optString("study_year", ""));
        course.setCredits(item.optInt("credits", 3));
        course.setInstructorId(item.optString("instructor_id", ""));
        course.setInstructorName(item.optString("instructor_name", ""));
        course.setSemester(item.optString("semester", ""));
        course.setAcademicYear(item.optString("academic_year", ""));
        course.setScheduleText(item.optString("schedule_text", ""));

        // Course lifecycle fields
        course.setEnrollmentStartAt(item.optString("enrollment_start_at", ""));
        course.setEnrollmentEndAt(item.optString("enrollment_end_at", ""));
        course.setCourseStartAt(item.optString("course_start_at", ""));
        course.setCourseEndAt(item.optString("course_end_at", ""));
        course.setStatus(item.optString("status", ""));
        course.setCourseStatus(item.optString("course_status", ""));
        course.setCalculatedStatus(item.optString("calculated_status", ""));

        return course;
    }

    private void showLoading(boolean loading) {
        if (progressBar != null) {
            if (loading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        }

        if (loading) {
            if (recyclerCourses != null) {
                recyclerCourses.setVisibility(View.GONE);
            }

            if (textEmpty != null) {
                textEmpty.setVisibility(View.GONE);
            }
        }
    }

    private void showEmpty(String message) {
        if (recyclerCourses != null) {
            recyclerCourses.setVisibility(View.GONE);
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (textEmpty != null) {
            textEmpty.setVisibility(View.VISIBLE);
            textEmpty.setText(message);
        }
    }

    private String safe(String value) {
        if (value == null || value.equals("null")) {
            return "";
        }

        return value.trim();
    }

    private String getVolleyErrorMessage(VolleyError error) {
        if (error == null) {
            return getString(R.string.error_network);
        }

        if (error.networkResponse != null && error.networkResponse.data != null) {
            return new String(error.networkResponse.data);
        }

        if (error.getMessage() != null) {
            return error.getMessage();
        }

        return getString(R.string.error_network);
    }
}