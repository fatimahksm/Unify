package com.university.unify.fragments.instructor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.activities.InstructorCourseDetailsActivity;
import com.university.unify.adapter.InstructorCourseAdapter;
import com.university.unify.model.CourseModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InstructorCoursesFragment extends Fragment {

    private RecyclerView recyclerCourses;
    private ProgressBar progressBar;
    private TextView textEmpty;

    private InstructorCourseAdapter adapter;
    private final List<CourseModel> courseList = new ArrayList<CourseModel>();

    private String instructorId = "";

    public InstructorCoursesFragment() {
        super(R.layout.fragment_instructor_courses);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerCourses = view.findViewById(R.id.recyclerInstructorCourses);
        progressBar = view.findViewById(R.id.progressBarInstructorCourses);
        textEmpty = view.findViewById(R.id.textEmptyInstructorCourses);

        adapter = new InstructorCourseAdapter(requireContext(), courseList,
                new InstructorCourseAdapter.OnCourseClickListener() {
                    @Override
                    public void onCourseClicked(CourseModel course) {

                        Intent intent = new Intent(requireContext(), InstructorCourseDetailsActivity.class);

                        intent.putExtra("course_id", course.getCourseId());
                        intent.putExtra("course_title", course.getTitle());
                        intent.putExtra("course_code", course.getCode());

                        startActivity(intent);
                    }
                });
        recyclerCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCourses.setAdapter(adapter);

        loadInstructorFromSession();
    }

    private void loadInstructorFromSession() {
        DatabaseHelper db = new DatabaseHelper(requireContext());
        instructorId = safe(db.getLoggedInUserId());

        if (TextUtils.isEmpty(instructorId)) {
            showEmpty(getString(R.string.error_user_data_not_found));
            return;
        }

        loadCourses();
    }

    private void loadCourses() {
        showLoading(true);

        String url = ApiConfig.GET_INSTRUCTOR_COURSES + "?instructor_id=" + instructorId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) return;

                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);

                            courseList.clear();

                            if (!success) {
                                showEmpty(obj.optString("message", getString(R.string.error_loading_courses)));
                                return;
                            }

                            JSONArray data = obj.optJSONArray("data");

                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject item = data.getJSONObject(i);

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
                                    course.setInstructorId(item.optString("instructor_id", ""));
                                    course.setInstructorName(item.optString("instructor_name", ""));
                                    course.setStudyYear(item.optString("study_year", ""));
                                    course.setSemester(item.optString("semester", ""));
                                    course.setAcademicYear(item.optString("academic_year", ""));
                                    course.setCredits(item.optInt("credits", 3));
                                    course.setScheduleText(item.optString("schedule_text", ""));
                                    courseList.add(course);
                                }
                            }

                            adapter.notifyDataSetChanged();
                            showLoading(false);

                            if (courseList.isEmpty()) {
                                showEmpty(getString(R.string.no_courses_found));
                            } else {
                                recyclerCourses.setVisibility(View.VISIBLE);
                                textEmpty.setVisibility(View.GONE);
                            }

                        } catch (Exception e) {
                            showEmpty(getString(R.string.error_loading_courses));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded()) return;
                        showEmpty(getString(R.string.error_loading_courses));
                    }
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerCourses.setVisibility(loading ? View.GONE : View.VISIBLE);
        textEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        recyclerCourses.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        textEmpty.setVisibility(View.VISIBLE);
        textEmpty.setText(message);
    }

    private String safe(String value) {
        return value == null || value.equals("null") ? "" : value.trim();
    }
}