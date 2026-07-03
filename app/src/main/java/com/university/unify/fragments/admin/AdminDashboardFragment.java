package com.university.unify.fragments.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.university.unify.R;
import com.university.unify.network.ApiConfig;

public class AdminDashboardFragment extends Fragment {

    private TextView textFacultiesCount;
    private TextView textFacultyAdminsCount;
    private TextView textStudentsCount;
    private TextView textInstructorsCount;
    private TextView textDashboardStatus;
    private ProgressBar progressBarDashboard;

    public AdminDashboardFragment() {
        super(R.layout.fragment_admin_dashboard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textFacultiesCount = view.findViewById(R.id.textFacultiesCount);
        textFacultyAdminsCount = view.findViewById(R.id.textFacultyAdminsCount);
        textStudentsCount = view.findViewById(R.id.textStudentsCount);
        textInstructorsCount = view.findViewById(R.id.textInstructorsCount);
        textDashboardStatus = view.findViewById(R.id.textDashboardStatus);
        progressBarDashboard = view.findViewById(R.id.progressBarDashboard);

        loadDashboardCounts();
    }

    private void loadDashboardCounts() {
        showLoading(true);
        textDashboardStatus.setText(getString(R.string.loading_dashboard));

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.GET_ADMIN_DASHBOARD_COUNTS,
                null,
                response -> {
                    textFacultiesCount.setText(String.valueOf(response.optInt("faculties")));
                    textFacultyAdminsCount.setText(String.valueOf(response.optInt("faculty_admins")));
                    textStudentsCount.setText(String.valueOf(response.optInt("students")));
                    textInstructorsCount.setText(String.valueOf(response.optInt("instructors")));

                    showLoading(false);
                    textDashboardStatus.setText(getString(R.string.dashboard_loaded_successfully));
                },
                error -> {
                    showLoading(false);
                    textDashboardStatus.setText(getString(R.string.error_loading_dashboard));
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void showLoading(boolean loading) {
        progressBarDashboard.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}