package com.university.unify.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.university.unify.R;
import com.university.unify.adapter.CourseMaterialAdapter;
import com.university.unify.model.CourseMaterialModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StudentCourseMaterialsActivity extends AppCompatActivity
        implements CourseMaterialAdapter.OnMaterialActionListener {

    private TextView textTitle;
    private TextView textSubtitle;
    private TextView textEmpty;
    private ProgressBar progressBar;
    private RecyclerView recyclerMaterials;

    private CourseMaterialAdapter adapter;
    private final List<CourseMaterialModel> materialList = new ArrayList<>();

    private String courseId = "";
    private String courseTitle = "";
    private String courseCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_course_materials);

        courseId = safe(getIntent().getStringExtra("course_id"));
        courseTitle = safe(getIntent().getStringExtra("course_title"));
        courseCode = safe(getIntent().getStringExtra("course_code"));

        initViews();
        setupRecycler();

        loadMaterials();
    }

    private void initViews() {
        textTitle = findViewById(R.id.textMaterialsTitle);
        textSubtitle = findViewById(R.id.textMaterialsSubtitle);
        textEmpty = findViewById(R.id.textEmptyMaterials);
        progressBar = findViewById(R.id.progressMaterials);
        recyclerMaterials = findViewById(R.id.recyclerMaterials);

        if (!TextUtils.isEmpty(courseTitle)) {
            textTitle.setText(courseTitle);
        } else {
            textTitle.setText(getString(R.string.course_materials));
        }

        if (!TextUtils.isEmpty(courseCode)) {
            textSubtitle.setText(courseCode + " • " + getString(R.string.materials));
        } else {
            textSubtitle.setText(getString(R.string.materials));
        }
    }

    private void setupRecycler() {
        adapter = new CourseMaterialAdapter(
                this,
                materialList,
                false,
                this
        );

        recyclerMaterials.setLayoutManager(new LinearLayoutManager(this));
        recyclerMaterials.setAdapter(adapter);
    }

    private void loadMaterials() {
        if (TextUtils.isEmpty(courseId)) {
            showEmpty(getString(R.string.course_not_found));
            return;
        }

        showLoading(true);

        String url = ApiConfig.GET_COURSE_MATERIALS + "?course_id=" + courseId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> handleLoadMaterialsResponse(response),
                error -> showEmpty(getString(R.string.failed_to_load_materials))
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void handleLoadMaterialsResponse(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            boolean success = obj.optBoolean("success", false);

            materialList.clear();

            if (!success) {
                String message = obj.optString(
                        "message",
                        getString(R.string.failed_to_load_materials)
                );

                showEmpty(message);
                adapter.notifyDataSetChanged();
                return;
            }

            JSONArray data = obj.optJSONArray("data");

            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject item = data.getJSONObject(i);

                    CourseMaterialModel material = new CourseMaterialModel();

                    material.setMaterialId(item.optString("material_id", ""));
                    material.setCourseId(item.optString("course_id", ""));
                    material.setInstructorId(item.optString("instructor_id", ""));
                    material.setTitle(item.optString("title", ""));
                    material.setDescription(item.optString("description", ""));
                    material.setMaterialType(item.optString("material_type", ""));
                    material.setContentText(item.optString("content_text", ""));
                    material.setFileUrl(item.optString("file_url", ""));
                    material.setLinkUrl(item.optString("link_url", ""));
                    material.setCreatedAt(item.optString("created_at", ""));

                    materialList.add(material);
                }
            }

            adapter.notifyDataSetChanged();
            showLoading(false);

            if (materialList.isEmpty()) {
                showEmpty(getString(R.string.no_materials_found));
            } else {
                recyclerMaterials.setVisibility(View.VISIBLE);
                textEmpty.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            showEmpty(getString(R.string.failed_to_read_materials));
        }
    }

    @Override
    public void onOpenClicked(CourseMaterialModel material) {
        if (material == null) {
            Toast.makeText(this, getString(R.string.material_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        String type = safe(material.getMaterialType());

        if ("NOTE".equalsIgnoreCase(type)) {
            showNoteDialog(material);
            return;
        }

        String url;

        if ("PDF".equalsIgnoreCase(type)) {
            url = safe(material.getFileUrl());
        } else {
            url = safe(material.getLinkUrl());
        }

        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, getString(R.string.no_link_found), Toast.LENGTH_SHORT).show();
            return;
        }

        openUrl(url);
    }

    @Override
    public void onDeleteClicked(int position, CourseMaterialModel material) {
        // Student cannot delete materials.
    }

    private void showNoteDialog(CourseMaterialModel material) {
        String title = safe(material.getTitle());
        String content = safe(material.getContentText());

        if (TextUtils.isEmpty(title)) {
            title = getString(R.string.note);
        }

        if (TextUtils.isEmpty(content)) {
            content = "-";
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(getString(R.string.ok), null)
                .show();
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.no_link_found), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean loading) {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerMaterials.setVisibility(View.GONE);
            textEmpty.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerMaterials.setVisibility(View.VISIBLE);
        }
    }

    private void showEmpty(String message) {
        recyclerMaterials.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        textEmpty.setVisibility(View.VISIBLE);

        if (TextUtils.isEmpty(message)) {
            textEmpty.setText(getString(R.string.no_materials_found));
        } else {
            textEmpty.setText(message);
        }
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }

        if (value.equalsIgnoreCase("null")) {
            return "";
        }

        return value.trim();
    }
}