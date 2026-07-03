package com.university.unify.fragments.student;

import android.os.Bundle;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.adapter.StudentAnnouncementAdapter;
import com.university.unify.model.StudentAnnouncementModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StudentAnnouncementsFragment extends Fragment {

    private RecyclerView recyclerAnnouncements;
    private ProgressBar progressAnnouncements;
    private TextView textAnnouncementsEmpty;

    private DatabaseHelper db;
    private RequestQueue queue;

    private StudentAnnouncementAdapter adapter;
    private final List<StudentAnnouncementModel> announcements = new ArrayList<>();

    public StudentAnnouncementsFragment() {
        super(R.layout.fragment_student_announcements);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DatabaseHelper(requireContext());
        queue = Volley.newRequestQueue(requireContext());

        initViews(view);
        setupRecycler();
        loadAnnouncements();
    }

    private void initViews(View view) {
        recyclerAnnouncements = view.findViewById(R.id.recyclerAnnouncements);
        progressAnnouncements = view.findViewById(R.id.progressAnnouncements);
        textAnnouncementsEmpty = view.findViewById(R.id.textAnnouncementsEmpty);
    }

    private void setupRecycler() {
        adapter = new StudentAnnouncementAdapter(requireContext(), announcements);

        recyclerAnnouncements.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAnnouncements.setAdapter(adapter);
    }

    private void loadAnnouncements() {
        String studentId = db.getLoggedInUserId();

        if (studentId == null || studentId.trim().isEmpty()) {
            showEmpty(getString(R.string.student_session_not_found));
            return;
        }

        showLoading(true);

        String url = ApiConfig.GET_STUDENT_ANNOUNCEMENTS + "?student_id=" + studentId.trim();

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
                                getString(R.string.failed_to_load_announcements)
                        );

                        announcements.clear();

                        if (!success) {
                            adapter.notifyDataSetChanged();
                            showEmpty(message);
                            return;
                        }

                        JSONArray data = response.optJSONArray("data");

                        if (data == null || data.length() == 0) {
                            adapter.notifyDataSetChanged();
                            showEmpty(getString(R.string.no_announcements_found));
                            return;
                        }

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.optJSONObject(i);

                            if (obj == null) {
                                continue;
                            }

                            StudentAnnouncementModel item = new StudentAnnouncementModel();

                            item.setAnnouncementId(obj.optString("announcement_id"));
                            item.setCourseId(obj.optString("course_id"));
                            item.setTitle(obj.optString("title"));
                            item.setBody(obj.optString("body"));
                            item.setCreatedBy(obj.optString("created_by"));
                            item.setCreatedByName(obj.optString("created_by_name"));
                            item.setIsPinned(obj.optString("is_pinned"));
                            item.setCreatedAt(obj.optString("created_at"));
                            item.setCourseTitle(obj.optString("course_title"));
                            item.setCourseCode(obj.optString("course_code"));
                            item.setSemester(obj.optString("semester"));

                            announcements.add(item);
                        }

                        adapter.notifyDataSetChanged();
                        updateEmptyState();

                    } catch (Exception e) {
                        showEmpty(getString(R.string.announcements_parse_error));
                    }
                },
                error -> {
                    showLoading(false);
                    Toast.makeText(
                            requireContext(),
                            getString(R.string.failed_to_load_announcements),
                            Toast.LENGTH_SHORT
                    ).show();

                    showEmpty(getString(R.string.failed_to_load_announcements));
                }
        );

        queue.add(request);
    }

    private void showLoading(boolean loading) {
        progressAnnouncements.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerAnnouncements.setVisibility(loading ? View.GONE : View.VISIBLE);
        textAnnouncementsEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        recyclerAnnouncements.setVisibility(View.GONE);
        textAnnouncementsEmpty.setVisibility(View.VISIBLE);
        textAnnouncementsEmpty.setText(message);
    }

    private void updateEmptyState() {
        if (announcements.isEmpty()) {
            showEmpty(getString(R.string.no_announcements_found));
        } else {
            recyclerAnnouncements.setVisibility(View.VISIBLE);
            textAnnouncementsEmpty.setVisibility(View.GONE);
        }
    }
}