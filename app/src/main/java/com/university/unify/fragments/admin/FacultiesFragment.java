package com.university.unify.fragments.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.university.unify.R;
import com.university.unify.adapter.FacultyAdapter;
import com.university.unify.model.FacultyModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FacultiesFragment extends Fragment implements FacultyAdapter.FacultyActionListener {

    private RecyclerView recyclerFaculties;
    private ProgressBar progressBar;
    private TextView textEmpty;
    private FloatingActionButton fabAddFaculty;

    private FacultyAdapter adapter;

    private final List<FacultyModel> facultyList = new ArrayList<>();
    private final List<String> facultyDocIds = new ArrayList<>();

    private RequestQueue queue;

    public FacultiesFragment() {
        super(R.layout.fragment_faculties);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queue = Volley.newRequestQueue(requireContext());

        recyclerFaculties = view.findViewById(R.id.recyclerFaculties);
        progressBar = view.findViewById(R.id.progressBarFaculties);
        textEmpty = view.findViewById(R.id.textEmptyFaculties);
        fabAddFaculty = view.findViewById(R.id.fabAddFaculty);

        adapter = new FacultyAdapter(requireContext(), facultyList, facultyDocIds, this);

        recyclerFaculties.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerFaculties.setAdapter(adapter);

        fabAddFaculty.setOnClickListener(v -> showAddFacultyDialog());

        loadFaculties();
    }

    private void loadFaculties() {
        showLoading(true);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                ApiConfig.GET_FACULTIES,
                null,
                jsonArray -> {
                    if (!isAdded()) return;

                    facultyList.clear();
                    facultyDocIds.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject obj = jsonArray.getJSONObject(i);

                            FacultyModel faculty = new FacultyModel();
                            faculty.setFacultyId(obj.optString("faculty_id"));
                            faculty.setName(obj.optString("name"));
                            faculty.setCode(obj.optString("code"));
                            faculty.setActive(true);

                            facultyList.add(faculty);
                            facultyDocIds.add(obj.optString("faculty_id"));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    adapter.notifyDataSetChanged();
                    showLoading(false);

                    if (facultyList.isEmpty()) {
                        showEmpty(getString(R.string.no_faculties_found));
                    } else {
                        recyclerFaculties.setVisibility(View.VISIBLE);
                        textEmpty.setVisibility(View.GONE);
                    }
                },
                error -> {
                    if (!isAdded()) return;

                    showLoading(false);
                    showEmpty(getString(R.string.error_loading_faculties));

                    Toast.makeText(
                            requireContext(),
                            getString(R.string.error_loading_faculties),
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        queue.add(request);
    }

    private void showAddFacultyDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_faculty, null, false);

        EditText editFacultyName = dialogView.findViewById(R.id.editFacultyName);
        EditText editFacultyCode = dialogView.findViewById(R.id.editFacultyCode);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_faculty)
                .setView(dialogView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String facultyName = getText(editFacultyName);
            String facultyCode = getText(editFacultyCode).toUpperCase(Locale.ROOT);

            boolean valid = true;

            if (TextUtils.isEmpty(facultyName)) {
                editFacultyName.setError(getString(R.string.error_required));
                valid = false;
            }

            if (TextUtils.isEmpty(facultyCode)) {
                editFacultyCode.setError(getString(R.string.error_required));
                valid = false;
            }

            if (!valid) return;

            validateThenSaveFaculty(facultyName, facultyCode, dialog);
        });
    }

    private void validateThenSaveFaculty(String facultyName, String facultyCode, AlertDialog dialog) {
        String normalizedName = facultyName.trim().toLowerCase(Locale.ROOT);
        String normalizedCode = facultyCode.trim().toUpperCase(Locale.ROOT);

        /*
         * Important:
         * Faculties are now loaded from online MySQL/PHP.
         * Do NOT check Firebase here.
         * We check duplicates from the already loaded online DB list.
         */
        for (FacultyModel existingFaculty : facultyList) {
            if (existingFaculty == null) continue;

            String existingName = safe(existingFaculty.getName()).toLowerCase(Locale.ROOT);
            String existingCode = safe(existingFaculty.getCode()).toUpperCase(Locale.ROOT);

            if (existingName.equals(normalizedName)) {
                Toast.makeText(
                        requireContext(),
                        getString(R.string.error_faculty_name_already_exists),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (existingCode.equals(normalizedCode)) {
                Toast.makeText(
                        requireContext(),
                        getString(R.string.error_faculty_code_already_exists),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
        }

        saveFaculty(facultyName, facultyCode, dialog);
    }

    private void saveFaculty(String facultyName, String facultyCode, AlertDialog dialog) {
        showLoading(true);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADD_FACULTY,
                response -> {
                    if (!isAdded()) return;

                    showLoading(false);

                    String cleanResponse = response == null ? "" : response.trim();
                    Toast.makeText(requireContext(), cleanResponse, Toast.LENGTH_SHORT).show();

                    if (cleanResponse.toLowerCase(Locale.ROOT).contains("added")
                            || cleanResponse.toLowerCase(Locale.ROOT).contains("success")) {
                        dialog.dismiss();
                        loadFaculties();
                    }
                },
                error -> {
                    if (!isAdded()) return;

                    showLoading(false);

                    Toast.makeText(
                            requireContext(),
                            "Error adding faculty",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();


                params.put("fname", facultyName);
                params.put("fcode", facultyCode);
                params.put("fkey", "2026");

                return params;
            }
        };

        queue.add(request);
    }

    @Override
    public void onDeleteClicked(int position, String docId) {
        if (position < 0 || position >= facultyList.size()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(R.string.confirm_delete_faculty)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteFaculty(position, docId))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onEditClicked(int position, String docId) {
        if (position < 0 || position >= facultyList.size()) return;

        FacultyModel faculty = facultyList.get(position);
        showUpdateFacultyDialog(faculty, docId);
    }

    private void showUpdateFacultyDialog(FacultyModel faculty, String facultyId) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_faculty, null, false);

        EditText editFacultyName = dialogView.findViewById(R.id.editFacultyName);
        EditText editFacultyCode = dialogView.findViewById(R.id.editFacultyCode);

        editFacultyName.setText(safe(faculty.getName()));
        editFacultyCode.setText(safe(faculty.getCode()));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.edit)
                .setView(dialogView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String facultyName = getText(editFacultyName);
            String facultyCode = getText(editFacultyCode).toUpperCase(Locale.ROOT);

            if (TextUtils.isEmpty(facultyName)) {
                editFacultyName.setError(getString(R.string.error_required));
                return;
            }

            if (TextUtils.isEmpty(facultyCode)) {
                editFacultyCode.setError(getString(R.string.error_required));
                return;
            }

            validateThenUpdateFaculty(facultyId, facultyName, facultyCode, dialog);
        });
    }

    private void validateThenUpdateFaculty(String facultyId,
                                           String facultyName,
                                           String facultyCode,
                                           AlertDialog dialog) {
        String normalizedName = facultyName.trim().toLowerCase(Locale.ROOT);
        String normalizedCode = facultyCode.trim().toUpperCase(Locale.ROOT);

        for (FacultyModel existingFaculty : facultyList) {
            if (existingFaculty == null) continue;

            String existingId = safe(existingFaculty.getFacultyId());

            if (existingId.equals(safe(facultyId))) {
                continue;
            }

            String existingName = safe(existingFaculty.getName()).toLowerCase(Locale.ROOT);
            String existingCode = safe(existingFaculty.getCode()).toUpperCase(Locale.ROOT);

            if (existingName.equals(normalizedName)) {
                Toast.makeText(
                        requireContext(),
                        getString(R.string.error_faculty_name_already_exists),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (existingCode.equals(normalizedCode)) {
                Toast.makeText(
                        requireContext(),
                        getString(R.string.error_faculty_code_already_exists),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
        }

        updateFaculty(facultyId, facultyName, facultyCode, dialog);
    }

    private void updateFaculty(String facultyId,
                               String facultyName,
                               String facultyCode,
                               AlertDialog dialog) {
        showLoading(true);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.UPDATE_FACULTY,
                response -> {
                    if (!isAdded()) return;

                    showLoading(false);

                    String cleanResponse = response == null ? "" : response.trim();
                    Toast.makeText(requireContext(), cleanResponse, Toast.LENGTH_SHORT).show();

                    if (cleanResponse.toLowerCase(Locale.ROOT).contains("updated")
                            || cleanResponse.toLowerCase(Locale.ROOT).contains("success")) {
                        dialog.dismiss();
                        loadFaculties();
                    }
                },
                error -> {
                    if (!isAdded()) return;

                    showLoading(false);

                    Toast.makeText(
                            requireContext(),
                            "Error updating faculty",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("fid", facultyId);
                params.put("fname", facultyName);
                params.put("fcode", facultyCode);
                params.put("fkey", "2026");

                return params;
            }
        };

        queue.add(request);
    }

    private void deleteFaculty(int position, String docId) {
        showLoading(true);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.DELETE_FACULTY,
                response -> {
                    if (!isAdded()) return;

                    showLoading(false);

                    String cleanResponse = response == null ? "" : response.trim();
                    Toast.makeText(requireContext(), cleanResponse, Toast.LENGTH_SHORT).show();

                    if (cleanResponse.toLowerCase(Locale.ROOT).contains("deleted")
                            || cleanResponse.toLowerCase(Locale.ROOT).contains("success")) {
                        facultyList.remove(position);
                        facultyDocIds.remove(position);
                        adapter.notifyItemRemoved(position);

                        if (facultyList.isEmpty()) {
                            showEmpty(getString(R.string.no_faculties_found));
                        } else {
                            recyclerFaculties.setVisibility(View.VISIBLE);
                            textEmpty.setVisibility(View.GONE);
                        }
                    }
                },
                error -> {
                    if (!isAdded()) return;

                    showLoading(false);

                    Toast.makeText(
                            requireContext(),
                            "Error deleting faculty",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("fid", docId);
                params.put("fkey", "2026");

                return params;
            }
        };

        queue.add(request);
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerFaculties.setVisibility(loading ? View.GONE : View.VISIBLE);
        textEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        recyclerFaculties.setVisibility(View.GONE);
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
        if (value == null || value.equalsIgnoreCase("null")) {
            return "";
        }

        return value.trim();
    }
}