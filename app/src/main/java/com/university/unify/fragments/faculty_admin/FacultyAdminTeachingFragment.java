package com.university.unify.fragments.faculty_admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.activities.InstructorCourseDetailsActivity;
import com.university.unify.model.CourseModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FacultyAdminTeachingFragment extends Fragment {

    private RecyclerView recyclerTeachingCourses;
    private ProgressBar progressTeaching;
    private TextView textEmptyTeaching;

    private RequestQueue queue;
    private TeachingCourseAdapter adapter;

    private final List<CourseModel> courses = new ArrayList<CourseModel>();

    private String currentUserId = "";

    public FacultyAdminTeachingFragment() {
        super(R.layout.fragment_faculty_admin_teaching);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerTeachingCourses = view.findViewById(R.id.recyclerTeachingCourses);
        progressTeaching = view.findViewById(R.id.progressTeaching);
        textEmptyTeaching = view.findViewById(R.id.textEmptyTeaching);

        queue = Volley.newRequestQueue(requireContext());

        adapter = new TeachingCourseAdapter(requireContext(), courses, new TeachingCourseAdapter.OnCourseClickListener() {
            @Override
            public void onCourseClicked(CourseModel course) {
                openInstructorCourseDetails(course);
            }
        });

        recyclerTeachingCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTeachingCourses.setAdapter(adapter);

        loadCurrentUserThenCourses();
    }

    private void loadCurrentUserThenCourses() {
        DatabaseHelper db = new DatabaseHelper(requireContext());
        currentUserId = safe(db.getLoggedInUserId());

        if (TextUtils.isEmpty(currentUserId)) {
            showLoading(false);
            showEmpty(getString(R.string.error_loading_courses));
            return;
        }

        loadTeachingCourses();
    }

    private void loadTeachingCourses() {
        showLoading(true);

        String url = ApiConfig.GET_INSTRUCTOR_COURSES + "?instructor_id=" + currentUserId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) {
                            return;
                        }

                        try {
                            courses.clear();

                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);

                            if (!success) {
                                showLoading(false);
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
                                    course.setStudyYear(item.optString("study_year", ""));
                                    course.setCredits(item.optInt("credits", 3));
                                    course.setInstructorId(item.optString("instructor_id", ""));
                                    course.setInstructorName(item.optString("instructor_name", ""));
                                    course.setSemester(item.optString("semester", ""));
                                    course.setAcademicYear(item.optString("academic_year", ""));
                                    course.setScheduleText(item.optString("schedule_text", ""));

                                    courses.add(course);
                                }
                            }

                            adapter.notifyDataSetChanged();
                            showLoading(false);

                            if (courses.isEmpty()) {
                                showEmpty(getString(R.string.no_teaching_courses));
                            } else {
                                recyclerTeachingCourses.setVisibility(View.VISIBLE);
                                textEmptyTeaching.setVisibility(View.GONE);
                            }

                        } catch (Exception e) {
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

                        showLoading(false);
                        Toast.makeText(requireContext(), getVolleyErrorMessage(error), Toast.LENGTH_LONG).show();
                        showEmpty(getString(R.string.error_loading_courses));
                    }
                }
        );

        queue.add(request);
    }

    private void openInstructorCourseDetails(CourseModel course) {
        Intent intent = new Intent(requireContext(), InstructorCourseDetailsActivity.class);
        intent.putExtra("course_id", safe(course.getCourseId()));
        intent.putExtra("course_title", safe(course.getTitle()));
        intent.putExtra("course_code", safe(course.getCode()));
        startActivity(intent);
    }

    private void showLoading(boolean loading) {
        progressTeaching.setVisibility(loading ? View.VISIBLE : View.GONE);

        if (loading) {
            recyclerTeachingCourses.setVisibility(View.GONE);
            textEmptyTeaching.setVisibility(View.GONE);
        }
    }

    private void showEmpty(String message) {
        recyclerTeachingCourses.setVisibility(View.GONE);
        progressTeaching.setVisibility(View.GONE);
        textEmptyTeaching.setVisibility(View.VISIBLE);
        textEmptyTeaching.setText(message);
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

    private static class TeachingCourseAdapter extends RecyclerView.Adapter<TeachingCourseAdapter.CourseViewHolder> {

        interface OnCourseClickListener {
            void onCourseClicked(CourseModel course);
        }

        private final Context context;
        private final List<CourseModel> items;
        private final OnCourseClickListener listener;

        TeachingCourseAdapter(Context context, List<CourseModel> items, OnCourseClickListener listener) {
            this.context = context;
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(
                    R.layout.item_teaching_course,
                    parent,
                    false
            );

            return new CourseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
            final CourseModel course = items.get(position);

            String code = safeStatic(course.getCode());
            String title = safeStatic(course.getTitle());
            String semester = safeStatic(course.getSemester());
            String academicYear = safeStatic(course.getAcademicYear());
            String major = safeStatic(course.getMajorName());
            String studyYear = safeStatic(course.getStudyYear());
            String credits = String.valueOf(course.getCredits());
            String schedule = safeStatic(course.getScheduleText());

            holder.textCourseCode.setText(TextUtils.isEmpty(code) ? "-" : code);
            holder.textCourseTitle.setText(TextUtils.isEmpty(title) ? "-" : title);

            String semesterText = semester;

            if (!TextUtils.isEmpty(academicYear)) {
                semesterText = semester + " " + academicYear;
            }

            holder.textCourseSemester.setText(TextUtils.isEmpty(semesterText) ? "-" : semesterText);

            String meta = "";

            if (!TextUtils.isEmpty(major)) {
                meta += major;
            }

            if (!TextUtils.isEmpty(studyYear)) {
                if (!TextUtils.isEmpty(meta)) {
                    meta += " • ";
                }

                meta += "Year " + studyYear;
            }

            if (!TextUtils.isEmpty(credits)) {
                if (!TextUtils.isEmpty(meta)) {
                    meta += " • ";
                }

                meta += credits + " credits";
            }

            holder.textCourseMeta.setText(TextUtils.isEmpty(meta) ? "-" : meta);

            if (TextUtils.isEmpty(schedule)) {
                holder.textCourseSchedule.setText("-");
            } else {
                holder.textCourseSchedule.setText(schedule);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onCourseClicked(course);
                    }
                }
            });

            holder.textOpenCourse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onCourseClicked(course);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class CourseViewHolder extends RecyclerView.ViewHolder {

            TextView textCourseCode;
            TextView textCourseSemester;
            TextView textCourseTitle;
            TextView textCourseMeta;
            TextView textCourseSchedule;
            TextView textOpenCourse;

            CourseViewHolder(@NonNull View itemView) {
                super(itemView);

                textCourseCode = itemView.findViewById(R.id.textCourseCode);
                textCourseSemester = itemView.findViewById(R.id.textCourseSemester);
                textCourseTitle = itemView.findViewById(R.id.textCourseTitle);
                textCourseMeta = itemView.findViewById(R.id.textCourseMeta);
                textCourseSchedule = itemView.findViewById(R.id.textCourseSchedule);
                textOpenCourse = itemView.findViewById(R.id.textOpenCourse);
            }
        }

        private static String safeStatic(String value) {
            if (value == null || value.equals("null")) {
                return "";
            }

            return value.trim();
        }
    }
}