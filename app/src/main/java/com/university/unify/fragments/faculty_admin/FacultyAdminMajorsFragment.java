package com.university.unify.fragments.faculty_admin;

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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.adapter.MajorAdapter;
import com.university.unify.model.MajorModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FacultyAdminMajorsFragment extends Fragment implements MajorAdapter.MajorActionListener {

    private RecyclerView recyclerMajors;
    private ProgressBar progressBar;
    private TextView textEmpty;

    private MajorAdapter adapter;
    private final List<MajorModel> majorList = new ArrayList<MajorModel>();
    private final List<String> majorIds = new ArrayList<String>();

    private String currentFacultyId = "";
    private String currentFacultyName = "";

    public FacultyAdminMajorsFragment() {
        super(R.layout.fragment_majors);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerMajors = view.findViewById(R.id.recyclerMajors);
        progressBar = view.findViewById(R.id.progressBarMajors);
        textEmpty = view.findViewById(R.id.textEmptyMajors);

        View fabAddMajor = view.findViewById(R.id.fabAddMajor);

        adapter = new MajorAdapter(requireContext(), majorList, majorIds, this);
        recyclerMajors.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerMajors.setAdapter(adapter);

        fabAddMajor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddMajorDialog();
            }
        });

        loadFacultyFromSession();
    }

    private void loadFacultyFromSession() {
        DatabaseHelper db = new DatabaseHelper(requireContext());

        currentFacultyId = safe(db.getLoggedInFacultyId());

        if (TextUtils.isEmpty(currentFacultyId)) {
            showEmpty(getString(R.string.error_faculty_not_assigned));
            return;
        }

        loadMajors();
    }

    private void loadMajors() {
        showLoading(true);

        String url = ApiConfig.GET_MAJORS_BY_FACULTY + "?faculty_id=" + currentFacultyId;

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

                            majorList.clear();
                            majorIds.clear();

                            if (!success) {
                                showEmpty(obj.optString("message", getString(R.string.error_loading_majors)));
                                return;
                            }

                            JSONArray data = obj.optJSONArray("data");

                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject item = data.getJSONObject(i);

                                    MajorModel major = new MajorModel();
                                    major.setMajorId(item.optString("major_id", ""));
                                    major.setFacultyId(item.optString("faculty_id", ""));
                                    major.setFacultyName(item.optString("faculty_name", ""));
                                    major.setName(item.optString("name", ""));
                                    major.setCode(item.optString("code", ""));
                                    major.setActive("1".equals(item.optString("is_active", "1")));

                                    majorList.add(major);
                                    majorIds.add(item.optString("major_id", ""));
                                }
                            }

                            adapter.notifyDataSetChanged();
                            showLoading(false);

                            if (majorList.isEmpty()) {
                                showEmpty(getString(R.string.no_majors_found));
                            } else {
                                recyclerMajors.setVisibility(View.VISIBLE);
                                textEmpty.setVisibility(View.GONE);
                            }

                        } catch (Exception e) {
                            showLoading(false);
                            showEmpty(getString(R.string.error_loading_majors));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded()) return;

                        showLoading(false);
                        showEmpty(getString(R.string.error_loading_majors));
                    }
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void showAddMajorDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_major, null, false);

        EditText editMajorName = dialogView.findViewById(R.id.editMajorName);
        EditText editMajorCode = dialogView.findViewById(R.id.editMajorCode);

        TextView textSelectedFaculty = dialogView.findViewById(R.id.textSelectedFacultyName);
        if (textSelectedFaculty != null) {
            if (TextUtils.isEmpty(currentFacultyName)) {
                textSelectedFaculty.setText(getString(R.string.current_faculty));
            } else {
                textSelectedFaculty.setText(currentFacultyName);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_major)
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
                String majorName = getText(editMajorName);
                String majorCode = getText(editMajorCode).toUpperCase(Locale.ROOT);

                boolean valid = true;

                if (TextUtils.isEmpty(majorName)) {
                    editMajorName.setError(getString(R.string.error_required));
                    valid = false;
                }

                if (TextUtils.isEmpty(majorCode)) {
                    editMajorCode.setError(getString(R.string.error_required));
                    valid = false;
                }

                if (!valid) return;

                saveMajor(majorName, majorCode, dialog);
            }
        });
    }

    private void saveMajor(final String majorName, final String majorCode, final AlertDialog dialog) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADD_MAJOR,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) return;

                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);
                            String message = obj.optString("message", getString(R.string.error_adding_major));

                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                            if (success) {
                                dialog.dismiss();
                                loadMajors();
                            }

                        } catch (Exception e) {
                            Toast.makeText(requireContext(), getString(R.string.error_adding_major), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), getString(R.string.error_adding_major), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("faculty_id", currentFacultyId);
                params.put("name", majorName);
                params.put("code", majorCode);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }



    private void showEditMajorDialog(final int position, final MajorModel major) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_major, null, false);

        EditText editMajorName = dialogView.findViewById(R.id.editMajorName);
        EditText editMajorCode = dialogView.findViewById(R.id.editMajorCode);

        editMajorName.setText(safe(major.getName()));
        editMajorCode.setText(safe(major.getCode()));

        TextView textSelectedFaculty = dialogView.findViewById(R.id.textSelectedFacultyName);
        if (textSelectedFaculty != null) {
            if (TextUtils.isEmpty(currentFacultyName)) {
                textSelectedFaculty.setText(getString(R.string.current_faculty));
            } else {
                textSelectedFaculty.setText(currentFacultyName);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.edit_major)
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
                String name = getText(editMajorName);
                String code = getText(editMajorCode).toUpperCase(Locale.ROOT);

                boolean valid = true;

                if (TextUtils.isEmpty(name)) {
                    editMajorName.setError(getString(R.string.error_required));
                    valid = false;
                }

                if (TextUtils.isEmpty(code)) {
                    editMajorCode.setError(getString(R.string.error_required));
                    valid = false;
                }

                if (!valid) return;

                updateMajor(position, safe(major.getMajorId()), name, code, dialog);
            }
        });
    }

    private void updateMajor(final int position,
                             final String majorId,
                             final String name,
                             final String code,
                             final AlertDialog dialog) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.UPDATE_MAJOR,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) return;

                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);
                            String message = obj.optString("message", getString(R.string.error_updating_major));

                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                            if (success) {
                                dialog.dismiss();

                                if (position >= 0 && position < majorList.size()) {
                                    majorList.get(position).setName(name);
                                    majorList.get(position).setCode(code);
                                    adapter.notifyItemChanged(position);
                                }
                            }

                        } catch (Exception e) {
                            Toast.makeText(requireContext(), getString(R.string.error_updating_major), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), getString(R.string.error_updating_major), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("major_id", majorId);
                params.put("faculty_id", currentFacultyId);
                params.put("name", name);
                params.put("code", code);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    @Override
    public void onDeleteClicked(final int position, final String majorId) {
        if (position < 0 || position >= majorList.size()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(R.string.confirm_delete_major)
                .setPositiveButton(R.string.delete, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialogInterface, int which) {
                        deleteMajor(position, majorId);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteMajor(final int position, final String majorId) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.DELETE_MAJOR,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) return;

                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);
                            String message = obj.optString("message", getString(R.string.error_deleting_major));

                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                            if (success) {
                                if (position >= 0 && position < majorList.size()) {
                                    majorList.remove(position);
                                    majorIds.remove(position);
                                    adapter.notifyItemRemoved(position);
                                }

                                if (majorList.isEmpty()) {
                                    showEmpty(getString(R.string.no_majors_found));
                                }
                            }

                        } catch (Exception e) {
                            Toast.makeText(requireContext(), getString(R.string.error_deleting_major), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), getString(R.string.error_deleting_major), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("major_id", majorId);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerMajors.setVisibility(loading ? View.GONE : View.VISIBLE);
        textEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        recyclerMajors.setVisibility(View.GONE);
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
        return value == null ? "" : value.trim();
    }
}