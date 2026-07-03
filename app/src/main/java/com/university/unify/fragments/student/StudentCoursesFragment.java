package com.university.unify.fragments.student;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.adapter.AvailableCourseAdapter;
import com.university.unify.adapter.StudentCourseAdapter;
import com.university.unify.model.StudentCourseModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class StudentCoursesFragment extends Fragment {

    private static final String TAB_MY_COURSES = "MY_COURSES";
    private static final String TAB_AVAILABLE_COURSES = "AVAILABLE_COURSES";

    private TextView textWelcome;
    private TextView textEmpty;

    private TextView tabMyCourses;
    private TextView tabAvailableCourses;
    private View underlineMyCourses;
    private View underlineAvailableCourses;

    private TextInputEditText editSearch;
    private RecyclerView recyclerCourses;
    private RecyclerView recyclerAvailableCourses;
    private ProgressBar progressBar;

    private DatabaseHelper db;
    private RequestQueue queue;

    private StudentCourseAdapter myCoursesAdapter;
    private AvailableCourseAdapter availableCourseAdapter;

    private final List<StudentCourseModel> allMyCourses = new ArrayList<>();
    private final List<StudentCourseModel> visibleMyCourses = new ArrayList<>();

    private final List<StudentCourseModel> allAvailableCourses = new ArrayList<>();
    private final List<StudentCourseModel> visibleAvailableCourses = new ArrayList<>();

    private String selectedTab = TAB_MY_COURSES;
    private boolean availableCoursesLoaded = false;

    public StudentCoursesFragment() {
        super(R.layout.fragment_student_courses);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DatabaseHelper(requireContext());
        queue = Volley.newRequestQueue(requireContext());

        initViews(view);
        setupRecyclerViews();
        setupSearch();
        setupTabs();

        bindWelcome();
        selectTab(TAB_MY_COURSES);
        loadStudentCourses();
    }

    private void initViews(View view) {
        textWelcome = view.findViewById(R.id.textWelcome);
        textEmpty = view.findViewById(R.id.textEmpty);

        tabMyCourses = view.findViewById(R.id.tabMyCourses);
        tabAvailableCourses = view.findViewById(R.id.tabAvailableCourses);
        underlineMyCourses = view.findViewById(R.id.underlineMyCourses);
        underlineAvailableCourses = view.findViewById(R.id.underlineAvailableCourses);

        editSearch = view.findViewById(R.id.editSearch);
        recyclerCourses = view.findViewById(R.id.recyclerCourses);
        recyclerAvailableCourses = view.findViewById(R.id.recyclerAvailableCourses);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerViews() {
        myCoursesAdapter = new StudentCourseAdapter(requireContext(), visibleMyCourses);
        recyclerCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCourses.setAdapter(myCoursesAdapter);

        availableCourseAdapter = new AvailableCourseAdapter(
                requireContext(),
                visibleAvailableCourses,
                this::enrollCourse
        );

        recyclerAvailableCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAvailableCourses.setAdapter(availableCourseAdapter);
    }

    private void setupTabs() {
        tabMyCourses.setOnClickListener(v -> selectTab(TAB_MY_COURSES));
        tabAvailableCourses.setOnClickListener(v -> selectTab(TAB_AVAILABLE_COURSES));
    }

    private void setupSearch() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCurrentTab(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void bindWelcome() {
        String fullName = db.getLoggedInFullName();

        if (fullName == null || fullName.trim().isEmpty()) {
            textWelcome.setText(getString(R.string.welcome));
        } else {
            textWelcome.setText(getString(R.string.welcome_user, fullName.trim()));
        }
    }

    private void selectTab(String tab) {
        selectedTab = tab;

        boolean myCoursesSelected = TAB_MY_COURSES.equals(tab);
        boolean availableSelected = TAB_AVAILABLE_COURSES.equals(tab);

        setTabState(tabMyCourses, underlineMyCourses, myCoursesSelected);
        setTabState(tabAvailableCourses, underlineAvailableCourses, availableSelected);

        recyclerCourses.setVisibility(myCoursesSelected ? View.VISIBLE : View.GONE);
        recyclerAvailableCourses.setVisibility(availableSelected ? View.VISIBLE : View.GONE);

        editSearch.setText("");

        if (availableSelected && !availableCoursesLoaded) {
            loadAvailableCourses();
        } else {
            updateEmptyState();
        }
    }

    private void setTabState(TextView tab, View underline, boolean active) {
        tab.setTextColor(ContextCompat.getColor(
                requireContext(),
                active ? R.color.unify_blue : R.color.unify_text_secondary
        ));

        tab.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);

        underline.setBackgroundColor(ContextCompat.getColor(
                requireContext(),
                active ? R.color.unify_gold : android.R.color.transparent
        ));
    }

    private void loadStudentCourses() {
        String studentId = db.getLoggedInUserId();

        if (studentId == null || studentId.trim().isEmpty()) {
            showEmpty(getString(R.string.student_session_not_found));
            return;
        }

        showLoading(true);

        String url = ApiConfig.GET_STUDENT_COURSES + "?student_id=" + studentId.trim();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    showLoading(false);

                    try {
                        boolean success = response.optBoolean("success", false);
                        String message = response.optString(
                                "message",
                                getString(R.string.failed_to_load_courses)
                        );

                        allMyCourses.clear();
                        visibleMyCourses.clear();

                        if (!success) {
                            myCoursesAdapter.notifyDataSetChanged();
                            showEmpty(message);
                            return;
                        }

                        JSONArray data = response.optJSONArray("data");

                        if (data == null || data.length() == 0) {
                            myCoursesAdapter.notifyDataSetChanged();
                            showEmpty(getString(R.string.no_courses_found_yet));
                            return;
                        }

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.optJSONObject(i);

                            if (obj == null) {
                                continue;
                            }

                            allMyCourses.add(mapCourse(obj));
                        }

                        visibleMyCourses.addAll(allMyCourses);
                        myCoursesAdapter.notifyDataSetChanged();
                        updateEmptyState();

                    } catch (Exception e) {
                        showEmpty(getString(R.string.courses_parse_error));
                    }
                },
                error -> {
                    showLoading(false);
                    Toast.makeText(
                            requireContext(),
                            getString(R.string.failed_to_load_courses),
                            Toast.LENGTH_SHORT
                    ).show();
                    showEmpty(getString(R.string.failed_to_load_courses));
                }
        );

        queue.add(request);
    }

    private void loadAvailableCourses() {
        String studentId = db.getLoggedInUserId();

        if (studentId == null || studentId.trim().isEmpty()) {
            showEmpty(getString(R.string.student_session_not_found));
            return;
        }

        showLoading(true);

        String url = ApiConfig.GET_AVAILABLE_COURSES + "?student_id=" + studentId.trim();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    showLoading(false);
                    availableCoursesLoaded = true;

                    try {
                        boolean success = response.optBoolean("success", false);
                        String message = response.optString(
                                "message",
                                getString(R.string.failed_to_load_available_courses)
                        );

                        allAvailableCourses.clear();
                        visibleAvailableCourses.clear();

                        if (!success) {
                            availableCourseAdapter.notifyDataSetChanged();
                            showEmpty(message);
                            return;
                        }

                        JSONArray data = response.optJSONArray("data");

                        if (data == null || data.length() == 0) {
                            availableCourseAdapter.notifyDataSetChanged();
                            showEmpty(getString(R.string.available_courses_empty));
                            return;
                        }

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.optJSONObject(i);

                            if (obj == null) {
                                continue;
                            }

                            allAvailableCourses.add(mapCourse(obj));
                        }

                        visibleAvailableCourses.addAll(allAvailableCourses);
                        availableCourseAdapter.notifyDataSetChanged();
                        updateEmptyState();

                    } catch (Exception e) {
                        showEmpty(getString(R.string.available_courses_parse_error));
                    }
                },
                error -> {
                    showLoading(false);
                    Toast.makeText(
                            requireContext(),
                            getString(R.string.failed_to_load_available_courses),
                            Toast.LENGTH_SHORT
                    ).show();
                    showEmpty(getString(R.string.failed_to_load_available_courses));
                }
        );

        queue.add(request);
    }

    private StudentCourseModel mapCourse(JSONObject obj) {
        StudentCourseModel course = new StudentCourseModel();

        course.setCourseId(obj.optString("course_id"));
        course.setTitle(obj.optString("title"));
        course.setCode(obj.optString("code"));
        course.setDescription(obj.optString("description"));
        course.setSection(obj.optString("section"));
        course.setDepartment(obj.optString("department"));

        course.setInstructorId(obj.optString("instructor_id"));
        course.setInstructorName(obj.optString("instructor_name"));

        course.setSemester(obj.optString("semester"));
        course.setAcademicYear(obj.optString("academic_year"));

        course.setEnrollmentId(obj.optString("enrollment_id"));
        course.setEnrollmentStatus(obj.optString("enrollment_status"));
        course.setEnrolledAt(obj.optString("enrolled_at"));

        course.setCredits(obj.optString("credits"));
        course.setScheduleText(obj.optString("schedule_text"));

        // New course lifecycle fields
        course.setEnrollmentStartAt(obj.optString("enrollment_start_at"));
        course.setEnrollmentEndAt(obj.optString("enrollment_end_at"));
        course.setCourseStartAt(obj.optString("course_start_at"));
        course.setCourseEndAt(obj.optString("course_end_at"));
        course.setCourseStatus(obj.optString("course_status"));
        course.setCalculatedStatus(obj.optString("calculated_status"));

        // Result fields
        course.setResult(obj.optString("result", "IN_PROGRESS"));
        course.setFinalGrade(obj.optString("final_grade"));
        course.setResultPublished(obj.optBoolean("is_result_published", false));

        return course;
    }

    private void enrollCourse(StudentCourseModel course, int position) {
        String studentId = db.getLoggedInUserId();

        if (studentId == null || studentId.trim().isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    getString(R.string.student_session_not_found),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (course == null || course.getCourseId() == null || course.getCourseId().trim().isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    getString(R.string.error_enrollment_failed),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        showLoading(true);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ENROLL_COURSE,
                response -> {
                    showLoading(false);

                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean success = obj.optBoolean("success", false);
                        String code = obj.optString("code", "");

                        String message = getEnrollMessageFromCode(code, success);
                        message = appendConflictDetails(message, code, obj);

                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

                        if (success) {
                            removeAvailableCourse(position);
                            reloadMyCoursesAfterEnrollment();
                        }

                    } catch (Exception e) {
                        Toast.makeText(
                                requireContext(),
                                getString(R.string.error_enrollment_failed),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                },
                error -> {
                    showLoading(false);
                    Toast.makeText(
                            requireContext(),
                            getString(R.string.error_enrollment_failed),
                            Toast.LENGTH_SHORT
                    ).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("student_id", studentId.trim());
                params.put("course_id", course.getCourseId().trim());
                return params;
            }
        };

        queue.add(request);
    }

    private String getEnrollMessageFromCode(String code, boolean success) {
        if (success) {
            return getString(R.string.enroll_success);
        }

        if (code == null) {
            return getString(R.string.error_enrollment_failed);
        }

        switch (code) {
            case "MISSING_FIELDS":
                return getString(R.string.error_missing_fields);

            case "INVALID_FIELDS":
                return getString(R.string.error_invalid_fields);

            case "STUDENT_NOT_FOUND":
                return getString(R.string.error_student_not_found);

            case "COURSE_NOT_FOUND":
                return getString(R.string.error_course_not_found);

            case "COURSE_NOT_ACTIVE":
                return getString(R.string.error_course_not_active);

            case "ENROLLMENT_NOT_OPEN":
                return getString(R.string.error_enrollment_not_open);

            case "ENROLLMENT_NOT_STARTED":
                return getString(R.string.error_enrollment_not_started);

            case "ENROLLMENT_CLOSED":
                return getString(R.string.error_enrollment_closed);

            case "ENROLLMENT_WINDOW_NOT_SET":
                return getString(R.string.error_enrollment_window_not_set);

            case "COURSE_NOT_FOR_FACULTY":
                return getString(R.string.error_course_not_for_faculty);

            case "COURSE_NOT_FOR_MAJOR":
                return getString(R.string.error_course_not_for_major);

            case "COURSE_NOT_FOR_STUDY_YEAR":
                return getString(R.string.error_course_not_for_study_year);

            case "ALREADY_ENROLLED":
                return getString(R.string.error_already_enrolled);

            case "CREDIT_LIMIT_EXCEEDED":
                return getString(R.string.error_credit_limit_exceeded);

            case "TIME_CONFLICT":
                return getString(R.string.error_time_conflict);

            case "ENROLL_INSERT_FAILED":
            default:
                return getString(R.string.error_enrollment_failed);
        }
    }

    private String appendConflictDetails(String message, String code, JSONObject obj) {
        if (!"TIME_CONFLICT".equals(code)) {
            return message;
        }

        String conflictTitle = obj.optString("conflict_course_title", "");
        String conflictCode = obj.optString("conflict_course_code", "");

        if (conflictTitle.trim().isEmpty()) {
            return message;
        }

        String result = message + " " + conflictTitle.trim();

        if (!conflictCode.trim().isEmpty()) {
            result = result + " (" + conflictCode.trim() + ")";
        }

        return result;
    }

    private void removeAvailableCourse(int position) {
        if (position >= 0 && position < visibleAvailableCourses.size()) {
            StudentCourseModel removed = visibleAvailableCourses.remove(position);
            allAvailableCourses.remove(removed);
            availableCourseAdapter.notifyItemRemoved(position);
        } else {
            availableCoursesLoaded = false;
            loadAvailableCourses();
        }

        updateEmptyState();
    }

    private void reloadMyCoursesAfterEnrollment() {
        allMyCourses.clear();
        visibleMyCourses.clear();
        myCoursesAdapter.notifyDataSetChanged();
        loadStudentCourses();
    }

    private void filterCurrentTab(String query) {
        if (TAB_MY_COURSES.equals(selectedTab)) {
            filterMyCourses(query);
        } else {
            filterAvailableCourses(query);
        }
    }

    private void filterMyCourses(String query) {
        String cleanQuery = query == null ? "" : query.trim().toLowerCase();

        visibleMyCourses.clear();

        if (cleanQuery.isEmpty()) {
            visibleMyCourses.addAll(allMyCourses);
        } else {
            for (StudentCourseModel course : allMyCourses) {
                if (courseMatches(course, cleanQuery)) {
                    visibleMyCourses.add(course);
                }
            }
        }

        myCoursesAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void filterAvailableCourses(String query) {
        String cleanQuery = query == null ? "" : query.trim().toLowerCase();

        visibleAvailableCourses.clear();

        if (cleanQuery.isEmpty()) {
            visibleAvailableCourses.addAll(allAvailableCourses);
        } else {
            for (StudentCourseModel course : allAvailableCourses) {
                if (courseMatches(course, cleanQuery)) {
                    visibleAvailableCourses.add(course);
                }
            }
        }

        availableCourseAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private boolean courseMatches(StudentCourseModel course, String query) {
        String title = safe(course.getTitle()).toLowerCase();
        String code = safe(course.getCode()).toLowerCase();
        String instructor = safe(course.getInstructorName()).toLowerCase();
        String schedule = safe(course.getScheduleText()).toLowerCase();
        String semester = safe(course.getSemester()).toLowerCase();
        String academicYear = safe(course.getAcademicYear()).toLowerCase();
        String status = safe(course.getBestCourseStatus()).toLowerCase();
        String result = safe(course.getResult()).toLowerCase();

        return title.contains(query)
                || code.contains(query)
                || instructor.contains(query)
                || schedule.contains(query)
                || semester.contains(query)
                || academicYear.contains(query)
                || status.contains(query)
                || result.contains(query);
    }
    private void showLoading(boolean loading) {
        if (!isAdded() || getView() == null) {
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }

        if (recyclerCourses != null) {
            recyclerCourses.setVisibility(loading ? View.GONE : View.VISIBLE);
        }

        if (!loading) {
            updateEmptyState();
        } else if (textEmpty != null) {
            textEmpty.setVisibility(View.GONE);
        }
    }

    private void showEmpty(String message) {
        recyclerCourses.setVisibility(View.GONE);
        recyclerAvailableCourses.setVisibility(View.GONE);
        textEmpty.setVisibility(View.VISIBLE);
        textEmpty.setText(message);
    }

    private void updateEmptyState() {
        if (TAB_MY_COURSES.equals(selectedTab)) {
            if (visibleMyCourses.isEmpty()) {
                recyclerCourses.setVisibility(View.GONE);
                recyclerAvailableCourses.setVisibility(View.GONE);
                textEmpty.setVisibility(View.VISIBLE);
                textEmpty.setText(getString(R.string.no_courses_found));
            } else {
                recyclerCourses.setVisibility(View.VISIBLE);
                recyclerAvailableCourses.setVisibility(View.GONE);
                textEmpty.setVisibility(View.GONE);
            }
            return;
        }

        if (visibleAvailableCourses.isEmpty()) {
            recyclerCourses.setVisibility(View.GONE);
            recyclerAvailableCourses.setVisibility(View.GONE);
            textEmpty.setVisibility(View.VISIBLE);
            textEmpty.setText(getString(R.string.available_courses_empty));
        } else {
            recyclerCourses.setVisibility(View.GONE);
            recyclerAvailableCourses.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.GONE);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}