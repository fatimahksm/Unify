package com.university.unify.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.adapter.CourseMaterialAdapter;
import com.university.unify.firebase.NotificationHelper;
import com.university.unify.model.CourseMaterialModel;
import com.university.unify.network.ApiConfig;
import com.university.unify.network.VolleyMultipartRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstructorCourseMaterialsActivity extends AppCompatActivity
        implements CourseMaterialAdapter.OnMaterialActionListener {

    private static final int PDF_PICK_REQUEST_CODE = 2001;

    private TextView textTitle;
    private TextView textSubtitle;
    private TextView textEmpty;
    private ProgressBar progressBar;
    private RecyclerView recyclerMaterials;
    private FloatingActionButton fabAddMaterial;

    private CourseMaterialAdapter adapter;
    private final List<CourseMaterialModel> materialList = new ArrayList<>();

    private String courseId = "";
    private String courseTitle = "";
    private String courseCode = "";
    private String instructorId = "";

    private Uri selectedPdfUri;
    private TextView textSelectedPdf;
    private AlertDialog currentAddDialog;

    private String pendingTitle = "";
    private String pendingDescription = "";
    private String pendingType = "";
    private String pendingContent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_course_materials);

        courseId = safe(getIntent().getStringExtra("course_id"));
        courseTitle = safe(getIntent().getStringExtra("course_title"));
        courseCode = safe(getIntent().getStringExtra("course_code"));

        DatabaseHelper db = new DatabaseHelper(this);
        instructorId = safe(db.getLoggedInUserId());

        initViews();
        setupRecycler();
        setupListeners();

        loadMaterials();
    }

    private void initViews() {
        textTitle = findViewById(R.id.textMaterialsTitle);
        textSubtitle = findViewById(R.id.textMaterialsSubtitle);
        textEmpty = findViewById(R.id.textEmptyMaterials);
        progressBar = findViewById(R.id.progressMaterials);
        recyclerMaterials = findViewById(R.id.recyclerMaterials);
        fabAddMaterial = findViewById(R.id.fabAddMaterial);

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
        adapter = new CourseMaterialAdapter(this, materialList, true, this);
        recyclerMaterials.setLayoutManager(new LinearLayoutManager(this));
        recyclerMaterials.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAddMaterial.setOnClickListener(view -> showAddMaterialDialog());
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
                this::handleLoadMaterialsResponse,
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
                showEmpty(obj.optString("message", getString(R.string.failed_to_load_materials)));
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

    private void showAddMaterialDialog() {
        selectedPdfUri = null;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        EditText editTitle = new EditText(this);
        editTitle.setHint(getString(R.string.material_title));
        layout.addView(editTitle);

        EditText editDescription = new EditText(this);
        editDescription.setHint(getString(R.string.material_description));
        layout.addView(editDescription);

        Spinner spinnerType = new Spinner(this);
        String[] types = {"NOTE", "VIDEO", "LINK", "PDF"};

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                types
        );

        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        layout.addView(spinnerType);

        EditText editContent = new EditText(this);
        editContent.setHint(getString(R.string.material_content_hint));
        editContent.setMinLines(3);
        editContent.setSingleLine(false);
        layout.addView(editContent);

        Button buttonChoosePdf = new Button(this);
        buttonChoosePdf.setText(getString(R.string.choose_pdf));
        buttonChoosePdf.setVisibility(View.GONE);
        layout.addView(buttonChoosePdf);

        textSelectedPdf = new TextView(this);
        textSelectedPdf.setText(getString(R.string.no_pdf_selected));
        textSelectedPdf.setVisibility(View.GONE);
        layout.addView(textSelectedPdf);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = spinnerType.getSelectedItem().toString();

                if ("PDF".equals(selectedType)) {
                    editContent.setVisibility(View.GONE);
                    buttonChoosePdf.setVisibility(View.VISIBLE);
                    textSelectedPdf.setVisibility(View.VISIBLE);
                } else {
                    editContent.setVisibility(View.VISIBLE);
                    buttonChoosePdf.setVisibility(View.GONE);
                    textSelectedPdf.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        buttonChoosePdf.setOnClickListener(view -> openPdfPicker());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.add_material))
                .setView(layout)
                .setPositiveButton(getString(R.string.save), null)
                .setNegativeButton(getString(R.string.cancel), null)
                .create();

        currentAddDialog = dialog;
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String title = getEditTextValue(editTitle);
            String description = getEditTextValue(editDescription);
            String type = spinnerType.getSelectedItem().toString();
            String content = getEditTextValue(editContent);

            boolean valid = true;

            if (TextUtils.isEmpty(title)) {
                editTitle.setError(getString(R.string.required));
                valid = false;
            }

            if (!"PDF".equals(type) && TextUtils.isEmpty(content)) {
                editContent.setError(getString(R.string.required));
                valid = false;
            }

            if ("PDF".equals(type) && selectedPdfUri == null) {
                Toast.makeText(this, getString(R.string.upload_pdf_first), Toast.LENGTH_SHORT).show();
                valid = false;
            }

            if (!valid) return;

            pendingTitle = title;
            pendingDescription = description;
            pendingType = type;
            pendingContent = content;

            if ("PDF".equals(type)) {
                uploadPdfToPhpServer();
            } else {
                createMaterial(title, description, type, content, "", dialog);
            }
        });
    }

    private void openPdfPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PDF_PICK_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PDF_PICK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedPdfUri = data.getData();

            if (selectedPdfUri != null && textSelectedPdf != null) {
                textSelectedPdf.setText(getString(R.string.pdf_selected));
            }
        }
    }

    private void uploadPdfToPhpServer() {
        if (selectedPdfUri == null) {
            Toast.makeText(this, getString(R.string.upload_pdf_first), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, getString(R.string.uploading_pdf), Toast.LENGTH_SHORT).show();

        byte[] pdfBytes = getBytesFromUri(selectedPdfUri);

        if (pdfBytes == null) {
            Toast.makeText(this, getString(R.string.failed_to_upload_pdf), Toast.LENGTH_SHORT).show();
            return;
        }

        VolleyMultipartRequest request = new VolleyMultipartRequest(
                Request.Method.POST,
                ApiConfig.UPLOAD_MATERIAL_PDF,
                response -> handlePdfUploadResponse(response),
                error -> Toast.makeText(
                        this,
                        getString(R.string.failed_to_upload_pdf),
                        Toast.LENGTH_SHORT
                ).show()
        ) {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put(
                        "pdf_file",
                        new DataPart(
                                "course_material.pdf",
                                pdfBytes,
                                "application/pdf"
                        )
                );
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void handlePdfUploadResponse(NetworkResponse response) {
        try {
            String responseText = new String(response.data, "UTF-8").trim();

            android.util.Log.d("PDF_UPLOAD_RESPONSE", responseText);

            int start = responseText.indexOf("{");
            int end = responseText.lastIndexOf("}");

            if (start == -1 || end == -1 || end <= start) {
                Toast.makeText(
                        this,
                        getString(R.string.failed_to_upload_pdf),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            String jsonText = responseText.substring(start, end + 1);

            JSONObject obj = new JSONObject(jsonText);

            boolean success = obj.optBoolean("success", false);

            if (!success) {
                Toast.makeText(
                        this,
                        obj.optString("message", getString(R.string.failed_to_upload_pdf)),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            String fileUrl = obj.optString("file_url", "");

            android.util.Log.d("PDF_FILE_URL", fileUrl);

            if (TextUtils.isEmpty(fileUrl)) {
                Toast.makeText(
                        this,
                        getString(R.string.failed_to_upload_pdf),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            createMaterial(
                    pendingTitle,
                    pendingDescription,
                    pendingType,
                    "",
                    fileUrl,
                    currentAddDialog
            );

        } catch (Exception e) {
            android.util.Log.e("PDF_UPLOAD_ERROR", "Parse error", e);

            Toast.makeText(
                    this,
                    getString(R.string.failed_to_upload_pdf),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private byte[] getBytesFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;

            while (inputStream != null && (len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }

            if (inputStream != null) {
                inputStream.close();
            }

            return byteBuffer.toByteArray();

        } catch (Exception e) {
            return null;
        }
    }

    private void createMaterial(final String title,
                                final String description,
                                final String type,
                                final String content,
                                final String fileUrl,
                                final AlertDialog dialog) {

        if (TextUtils.isEmpty(courseId)) {
            Toast.makeText(this, getString(R.string.course_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(instructorId)) {
            Toast.makeText(this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADD_COURSE_MATERIAL,
                response -> handleCreateMaterialResponse(response, dialog),
                error -> Toast.makeText(
                        this,
                        getString(R.string.failed_to_add_material),
                        Toast.LENGTH_SHORT
                ).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("course_id", courseId);
                params.put("instructor_id", instructorId);
                params.put("title", title);
                params.put("description", description);
                params.put("material_type", type);

                if ("NOTE".equals(type)) {
                    params.put("content_text", content);
                    params.put("link_url", "");
                    params.put("file_url", "");
                } else if ("PDF".equals(type)) {
                    params.put("content_text", "");
                    params.put("link_url", "");
                    params.put("file_url", fileUrl);
                } else {
                    params.put("content_text", "");
                    params.put("link_url", content);
                    params.put("file_url", "");
                }

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void handleCreateMaterialResponse(String response, AlertDialog dialog) {
        try {
            android.util.Log.d("ADD_MATERIAL_RESPONSE", response);

            String cleanResponse = response.trim();

            int start = cleanResponse.indexOf("{");
            int end = cleanResponse.lastIndexOf("}");

            if (start != -1 && end != -1 && end > start) {
                cleanResponse = cleanResponse.substring(start, end + 1);
            }

            JSONObject obj = new JSONObject(cleanResponse);

            boolean success = obj.optBoolean("success", false);
            String message = obj.optString(
                    "message",
                    success
                            ? getString(R.string.material_added_successfully)
                            : getString(R.string.failed_to_add_material)
            );

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            if (success) {
                if (dialog != null) {
                    dialog.dismiss();
                }

                selectedPdfUri = null;
                currentAddDialog = null;
                loadMaterials();

                NotificationHelper.sendCourseMaterialNotificationToStudents(
                        this,
                        courseId,
                        courseTitle,
                        courseCode,
                        pendingTitle,
                        instructorId
                );
            }

        } catch (Exception e) {
            android.util.Log.e("ADD_MATERIAL_ERROR", "Parse error: " + response, e);

            Toast.makeText(
                    this,
                    getString(R.string.failed_to_add_material),
                    Toast.LENGTH_SHORT
            ).show();
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

    @Override
    public void onDeleteClicked(final int position, final CourseMaterialModel material) {
        if (material == null) {
            Toast.makeText(this, getString(R.string.material_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete))
                .setMessage(getString(R.string.delete_material_question))
                .setPositiveButton(getString(R.string.delete), (dialogInterface, which) ->
                        deleteMaterial(position, material.getMaterialId())
                )
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void deleteMaterial(final int position, final String materialId) {
        if (TextUtils.isEmpty(materialId)) {
            Toast.makeText(this, getString(R.string.material_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(instructorId)) {
            Toast.makeText(this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.DELETE_COURSE_MATERIAL,
                response -> handleDeleteMaterialResponse(response, position),
                error -> Toast.makeText(
                        this,
                        getString(R.string.failed_to_delete_material),
                        Toast.LENGTH_SHORT
                ).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("material_id", materialId);
                params.put("instructor_id", instructorId);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void handleDeleteMaterialResponse(String response, int position) {
        try {
            JSONObject obj = new JSONObject(response);

            boolean success = obj.optBoolean("success", false);
            String message = obj.optString(
                    "message",
                    success
                            ? getString(R.string.material_deleted_successfully)
                            : getString(R.string.failed_to_delete_material)
            );

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            if (success) {
                if (position >= 0 && position < materialList.size()) {
                    materialList.remove(position);
                    adapter.notifyItemRemoved(position);
                } else {
                    loadMaterials();
                }

                if (materialList.isEmpty()) {
                    showEmpty(getString(R.string.no_materials_found));
                }
            }

        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.failed_to_delete_material), Toast.LENGTH_SHORT).show();
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

    private String getEditTextValue(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }

    private String safe(String value) {
        if (value == null || value.equalsIgnoreCase("null")) {
            return "";
        }

        return value.trim();
    }
}