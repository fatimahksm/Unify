package com.university.unify.fragments.instructor;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.network.ApiConfig;

import org.json.JSONObject;

public class InstructorHomeFragment extends Fragment {

    private TextView textWelcome;
    private TextView textSubtitle;
    private TextView textCoursesCount;
    private TextView textAttendanceCount;
    private TextView textAnnouncementsCount;

    private String instructorId = "";

    public InstructorHomeFragment() {
        super(R.layout.fragment_instructor_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textWelcome = view.findViewById(R.id.textInstructorWelcome);
        textSubtitle = view.findViewById(R.id.textInstructorSubtitle);
        textCoursesCount = view.findViewById(R.id.textInstructorCoursesCount);
        textAttendanceCount = view.findViewById(R.id.textInstructorAttendanceCount);
        textAnnouncementsCount = view.findViewById(R.id.textInstructorAnnouncementsCount);

        loadHomeData();
    }

    private void loadHomeData() {
        DatabaseHelper db = new DatabaseHelper(requireContext());

        instructorId = safe(db.getLoggedInUserId());

        String fullName = safe(db.getLoggedInFullName());

        if (fullName.isEmpty()) {
            fullName = getString(R.string.instructor);
        }

        textWelcome.setText(getString(R.string.welcome_user, fullName));
        textSubtitle.setText(getString(R.string.instructor_home_subtitle));

        textCoursesCount.setText("0");
        textAttendanceCount.setText("0");
        textAnnouncementsCount.setText("0");

        if (!instructorId.isEmpty()) {
            loadStats();
        }
    }

    private void loadStats() {
        String url = ApiConfig.GET_INSTRUCTOR_HOME_STATS + "?instructor_id=" + instructorId;

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

                            if (!success) {
                                return;
                            }

                            textCoursesCount.setText(String.valueOf(obj.optInt("courses_count", 0)));
                            textAttendanceCount.setText(String.valueOf(obj.optInt("students_count", 0)));
                            textAnnouncementsCount.setText(String.valueOf(obj.optInt("announcements_count", 0)));

                        } catch (Exception ignored) {
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Keep default 0 values
                    }
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private String safe(String value) {
        return value == null || value.equals("null") ? "" : value.trim();
    }
}