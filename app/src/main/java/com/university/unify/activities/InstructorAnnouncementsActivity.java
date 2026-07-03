package com.university.unify.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.adapter.AnnouncementAdapter;
import com.university.unify.firebase.NotificationHelper;
import com.university.unify.model.AnnouncementModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class InstructorAnnouncementsActivity extends AppCompatActivity implements AnnouncementAdapter.AnnouncementActionListener {

    private TextView textTitle;
    private TextView textSubtitle;
    private TextView textEmpty;
    private ProgressBar progressBar;
    private RecyclerView recyclerAnnouncements;
    private FloatingActionButton fabAddAnnouncement;

    private AnnouncementAdapter adapter;
    private final List<AnnouncementModel> announcementList = new ArrayList<AnnouncementModel>();

    private String courseId = "";
    private String courseTitle = "";
    private String courseCode = "";
    private String instructorId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_announcements);

        courseId = getIntent().getStringExtra("course_id");
        courseTitle = getIntent().getStringExtra("course_title");
        courseCode = getIntent().getStringExtra("course_code");

        if (courseId == null) courseId = "";
        if (courseTitle == null) courseTitle = "";
        if (courseCode == null) courseCode = "";

        DatabaseHelper db = new DatabaseHelper(this);
        instructorId = safe(db.getLoggedInUserId());

        initViews();
        setupRecycler();
        setupListeners();

        loadAnnouncements();
    }

    private void initViews() {
        textTitle = findViewById(R.id.textAnnouncementsTitle);
        textSubtitle = findViewById(R.id.textAnnouncementsSubtitle);
        textEmpty = findViewById(R.id.textEmptyAnnouncements);
        progressBar = findViewById(R.id.progressBarAnnouncements);
        recyclerAnnouncements = findViewById(R.id.recyclerAnnouncements);
        fabAddAnnouncement = findViewById(R.id.fabAddAnnouncement);

        if (!TextUtils.isEmpty(courseTitle)) {
            textTitle.setText(courseTitle);
        }

        if (!TextUtils.isEmpty(courseCode)) {
            textSubtitle.setText(courseCode + " • " + getString(R.string.announcements));
        }
    }

    private void setupRecycler() {
        adapter = new AnnouncementAdapter(this, announcementList, this);
        recyclerAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnnouncements.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAddAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddAnnouncementDialog();
            }
        });
    }

    private void loadAnnouncements() {
        if (TextUtils.isEmpty(courseId)) {
            showEmpty(getString(R.string.error_course_not_found));
            return;
        }

        showLoading(true);

        String url = ApiConfig.GET_ANNOUNCEMENTS + "?course_id=" + courseId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);

                            announcementList.clear();

                            if (!success) {
                                showEmpty(obj.optString("message", getString(R.string.error_loading_announcements)));
                                return;
                            }

                            JSONArray data = obj.optJSONArray("data");

                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject item = data.getJSONObject(i);

                                    AnnouncementModel announcement = new AnnouncementModel();
                                    announcement.setAnnouncementId(item.optString("announcement_id", ""));
                                    announcement.setTitle(item.optString("title", ""));
                                    announcement.setBody(item.optString("body", ""));
                                    announcement.setCreatedAt(item.optString("created_at", ""));
                                    announcement.setCreatedByName(item.optString("created_by_name", ""));
                                    announcement.setPinned("1".equals(item.optString("is_pinned", "0")));

                                    announcementList.add(announcement);
                                }
                            }

                            adapter.notifyDataSetChanged();
                            showLoading(false);

                            if (announcementList.isEmpty()) {
                                showEmpty(getString(R.string.no_announcements_found));
                            } else {
                                recyclerAnnouncements.setVisibility(View.VISIBLE);
                                textEmpty.setVisibility(View.GONE);
                            }

                        } catch (Exception e) {
                            showEmpty(getString(R.string.error_loading_announcements));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showEmpty(getString(R.string.error_loading_announcements));
                    }
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void showAddAnnouncementDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_announcement, null, false);

        EditText editTitle = dialogView.findViewById(R.id.editAnnouncementTitle);
        EditText editBody = dialogView.findViewById(R.id.editAnnouncementBody);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_announcement)
                .setView(dialogView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = getText(editTitle);
                String body = getText(editBody);

                boolean valid = true;

                if (TextUtils.isEmpty(title)) {
                    editTitle.setError(getString(R.string.error_required));
                    valid = false;
                }

                if (TextUtils.isEmpty(body)) {
                    editBody.setError(getString(R.string.error_required));
                    valid = false;
                }

                if (!valid) return;

                createAnnouncement(title, body, dialog);
            }
        });
    }

    private void createAnnouncement(final String title,
                                    final String body,
                                    final AlertDialog dialog) {

        if (TextUtils.isEmpty(instructorId)) {
            Toast.makeText(this, getString(R.string.error_user_data_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.CREATE_ANNOUNCEMENT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);
                            String message = obj.optString("message", getString(R.string.error_creating_announcement));

                            Toast.makeText(InstructorAnnouncementsActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (success) {
                                dialog.dismiss();
                                loadAnnouncements();

                                NotificationHelper.sendCourseAnnouncementNotificationToStudents(
                                        InstructorAnnouncementsActivity.this,
                                        courseId,
                                        courseTitle,
                                        courseCode,
                                        title,
                                        instructorId
                                );
                            }

                        } catch (Exception e) {
                            Toast.makeText(
                                    InstructorAnnouncementsActivity.this,
                                    getString(R.string.error_creating_announcement),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(
                                InstructorAnnouncementsActivity.this,
                                getString(R.string.error_creating_announcement),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("course_id", courseId);
                params.put("title", title);
                params.put("body", body);
                params.put("created_by", instructorId);

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public void onDeleteClicked(final int position, final String announcementId) {
        if (position < 0 || position >= announcementList.size()) return;

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.confirm_delete_announcement)
                .setPositiveButton(R.string.delete, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialogInterface, int which) {
                        deleteAnnouncement(position, announcementId);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteAnnouncement(final int position, final String announcementId) {
        if (TextUtils.isEmpty(announcementId)) {
            Toast.makeText(this, getString(R.string.error_deleting_announcement), Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.DELETE_ANNOUNCEMENT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);
                            String message = obj.optString("message", getString(R.string.error_deleting_announcement));

                            Toast.makeText(InstructorAnnouncementsActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (success) {
                                if (position >= 0 && position < announcementList.size()) {
                                    announcementList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                }

                                if (announcementList.isEmpty()) {
                                    showEmpty(getString(R.string.no_announcements_found));
                                }
                            }

                        } catch (Exception e) {
                            Toast.makeText(
                                    InstructorAnnouncementsActivity.this,
                                    getString(R.string.error_deleting_announcement),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(
                                InstructorAnnouncementsActivity.this,
                                getString(R.string.error_deleting_announcement),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("announcement_id", announcementId);
                params.put("created_by", instructorId);

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerAnnouncements.setVisibility(loading ? View.GONE : View.VISIBLE);
        textEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        recyclerAnnouncements.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        textEmpty.setVisibility(View.VISIBLE);
        textEmpty.setText(message);
    }

    private String getText(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }

    private String safe(String value) {
        return value == null || value.equals("null") ? "" : value.trim();
    }
}