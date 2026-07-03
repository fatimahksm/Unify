package com.university.unify.fragments.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.university.unify.R;
import com.university.unify.adapter.FacultyAdminAdapter;
import com.university.unify.helpers.EmailJsSender;
import com.university.unify.model.FacultyAdminModel;
import com.university.unify.model.FacultyModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.firebase.auth.AuthResult;

public class FacultyAdminsFragment extends Fragment implements FacultyAdminAdapter.FacultyAdminActionListener {

    private RecyclerView recyclerFacultyAdmins;
    private ProgressBar progressBar;
    private TextView textEmpty;
    private FloatingActionButton fabAddFacultyAdmin;

    private FacultyAdminAdapter adapter;

    private final List<FacultyAdminModel> facultyAdminList = new ArrayList<FacultyAdminModel>();
    private final List<String> facultyAdminDocIds = new ArrayList<String>();

    private final List<FacultyModel> facultyList = new ArrayList<FacultyModel>();
    private final List<String> facultyNames = new ArrayList<String>();

    private RequestQueue queue;
    private FirebaseAuth firebaseAuth;

    public FacultyAdminsFragment() {
        super(R.layout.fragment_faculty_admins);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queue = Volley.newRequestQueue(requireContext());

        recyclerFacultyAdmins = view.findViewById(R.id.recyclerFacultyAdmins);
        progressBar = view.findViewById(R.id.progressBarFacultyAdmins);
        textEmpty = view.findViewById(R.id.textEmptyFacultyAdmins);
        fabAddFacultyAdmin = view.findViewById(R.id.fabAddFacultyAdmin);

        adapter = new FacultyAdminAdapter(requireContext(), facultyAdminList, facultyAdminDocIds, this);
        recyclerFacultyAdmins.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerFacultyAdmins.setAdapter(adapter);

        firebaseAuth = FirebaseAuth.getInstance();

        fabAddFacultyAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (facultyList.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.add_faculty_first), Toast.LENGTH_SHORT).show();
                    return;
                }

                showAddFacultyAdminDialog();
            }
        });

        loadFacultiesThenAdmins();
    }

    private void loadFacultiesThenAdmins() {
        showLoading(true);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                ApiConfig.GET_FACULTIES,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        facultyList.clear();
                        facultyNames.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject obj = jsonArray.getJSONObject(i);

                                FacultyModel faculty = new FacultyModel();
                                faculty.setFacultyId(obj.optString("faculty_id"));
                                faculty.setName(obj.optString("name"));
                                faculty.setCode(obj.optString("code"));

                                facultyList.add(faculty);

                                String label = safe(faculty.getName());
                                String code = safe(faculty.getCode());

                                if (code.isEmpty()) {
                                    facultyNames.add(label);
                                } else {
                                    facultyNames.add(label + " • " + code);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        loadFacultyAdmins();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        showLoading(false);
                        showEmpty(getString(R.string.error_loading_faculties));
                        Toast.makeText(requireContext(), "Error loading faculties", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        queue.add(request);
    }

    private void loadFacultyAdmins() {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                ApiConfig.GET_FACULTY_ADMINS,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        facultyAdminList.clear();
                        facultyAdminDocIds.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject obj = jsonArray.getJSONObject(i);

                                String fullName = obj.optString("full_name", "");
                                String firstName = fullName;
                                String lastName = "";

                                if (fullName.contains(" ")) {
                                    firstName = fullName.substring(0, fullName.indexOf(" "));
                                    lastName = fullName.substring(fullName.indexOf(" ") + 1);
                                }

                                FacultyAdminModel admin = new FacultyAdminModel();
                                admin.setAdminId(obj.optString("user_id"));
                                admin.setFirstName(firstName);
                                admin.setLastName(lastName);
                                admin.setEmail(obj.optString("email"));
                                admin.setFacultyId(obj.optString("faculty_id"));
                                admin.setFacultyName(obj.optString("faculty_name"));
                                admin.setActive(obj.optInt("is_active", 1) == 1);

                                facultyAdminList.add(admin);
                                facultyAdminDocIds.add(obj.optString("user_id"));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        adapter.notifyDataSetChanged();
                        showLoading(false);

                        if (facultyAdminList.isEmpty()) {
                            showEmpty(getString(R.string.no_faculty_admins_found));
                        } else {
                            recyclerFacultyAdmins.setVisibility(View.VISIBLE);
                            textEmpty.setVisibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        showLoading(false);
                        showEmpty(getString(R.string.error_loading_faculty_admins));
                        Toast.makeText(requireContext(), "Error loading faculty admins", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        queue.add(request);
    }

    private void showAddFacultyAdminDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_faculty_admin, null, false);

        EditText editEmail = dialogView.findViewById(R.id.editFacultyAdminEmail);
        AutoCompleteTextView autoFaculty = dialogView.findViewById(R.id.autoFacultyForAdmin);

        ArrayAdapter<String> facultyAdapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                facultyNames
        );

        autoFaculty.setAdapter(facultyAdapter);
        autoFaculty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoFaculty.showDropDown();
            }
        });

        final FacultyModel[] selectedFaculty = new FacultyModel[1];

        autoFaculty.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < facultyList.size()) {
                    selectedFaculty[0] = facultyList.get(position);
                    autoFaculty.setError(null);
                }
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_faculty_admin)
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
                String email = getText(editEmail);

                boolean valid = true;

                if (TextUtils.isEmpty(email)) {
                    editEmail.setError(getString(R.string.error_required));
                    valid = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editEmail.setError(getString(R.string.error_invalid_email));
                    valid = false;
                }

                if (selectedFaculty[0] == null) {
                    autoFaculty.setError(getString(R.string.error_select_faculty));
                    valid = false;
                }

                if (!valid) return;

                createFacultyAdmin(email, selectedFaculty[0], dialog);
            }
        });
    }

    private void createFacultyAdmin(final String email,
                                    final FacultyModel faculty,
                                    final AlertDialog dialog) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.CREATE_FACULTY_ADMIN,
                response -> {
                    if (!isAdded()) return;

                    try {
                        JSONObject obj = new JSONObject(cleanJson(response));

                        boolean success = obj.optBoolean("success", false);
                        String message = obj.optString("message", "");

                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                        if (success) {
                            String returnedEmail = obj.optString("email", email);

                            String temporaryPassword = obj.optString("temporary_password", "");
                            if (TextUtils.isEmpty(temporaryPassword)) {
                                temporaryPassword = obj.optString("password", "");
                            }
                            if (TextUtils.isEmpty(temporaryPassword)) {
                                temporaryPassword = obj.optString("temp_password", "");
                            }

                            dialog.dismiss();

                            if (TextUtils.isEmpty(temporaryPassword)) {
                                loadFacultiesThenAdmins();
                                Toast.makeText(requireContext(), "Faculty admin created, but password was not returned.", Toast.LENGTH_LONG).show();
                                return;
                            }

                            showCredentialsDialog(returnedEmail, temporaryPassword);

                            createFirebaseAccountForFacultyAdmin(
                                    returnedEmail,
                                    temporaryPassword
                            );
                        }

                    } catch (Exception e) {
                        Toast.makeText(requireContext(), response, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    if (!isAdded()) return;

                    Toast.makeText(
                            requireContext(),
                            "Error creating faculty admin",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("email", email);
                params.put("faculty_id", safe(faculty.getFacultyId()));

                return params;
            }
        };

        queue.add(request);
    }

    private void createFirebaseAccountForFacultyAdmin(final String email,
                                                      final String temporaryPassword) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(temporaryPassword)) {
            Toast.makeText(requireContext(), "Missing generated credentials", Toast.LENGTH_LONG).show();
            return;
        }

        final FirebaseUser currentSuperAdmin = firebaseAuth.getCurrentUser();
        final String superAdminEmail;

        if (currentSuperAdmin != null && currentSuperAdmin.getEmail() != null) {
            superAdminEmail = currentSuperAdmin.getEmail();
        } else {
            superAdminEmail = "";
        }

        firebaseAuth.createUserWithEmailAndPassword(email, temporaryPassword)
                .addOnCompleteListener(requireActivity(), new com.google.android.gms.tasks.OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String firebaseUid = "";

                            if (task.getResult() != null && task.getResult().getUser() != null) {
                                firebaseUid = task.getResult().getUser().getUid();
                            }

                            updateFirebaseUidInMySql(
                                    email,
                                    firebaseUid,
                                    temporaryPassword,
                                    superAdminEmail
                            );

                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    "MySQL user created, but Firebase account failed. Email will still be sent.",
                                    Toast.LENGTH_LONG
                            ).show();

                            sendFacultyAdminEmail(email, temporaryPassword);
                            loadFacultiesThenAdmins();
                        }
                    }
                });
    }

    private void updateFirebaseUidInMySql(final String email,
                                          final String firebaseUid,
                                          final String temporaryPassword,
                                          final String superAdminEmail) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.UPDATE_FIREBASE_UID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) return;

                        Toast.makeText(requireContext(), response, Toast.LENGTH_SHORT).show();

                        sendFacultyAdminEmail(email, temporaryPassword);

                        loadFacultiesThenAdmins();

                        firebaseAuth.signOut();

                        Toast.makeText(
                                requireContext(),
                                "Faculty admin created. Credentials email is being sent. Please sign in again as Super Admin.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(requireContext(), "Firebase UID not saved. Email will still be sent.", Toast.LENGTH_LONG).show();

                        sendFacultyAdminEmail(email, temporaryPassword);
                        loadFacultiesThenAdmins();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("firebase_uid", firebaseUid);
                return params;
            }
        };

        queue.add(request);
    }

    private void sendFacultyAdminEmail(String email, String temporaryPassword) {
        EmailJsSender.sendInstructorCredentials(
                requireContext(),
                email,
                temporaryPassword,
                "Faculty Admin"
        );
    }

    private void showCredentialsDialog(String email, String temporaryPassword) {
        String message =
                getString(R.string.email) + ": " + email + "\n\n" +
                        getString(R.string.temporary_password) + ": " + temporaryPassword;

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.credentials)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void onUpdateClicked(int position, String docId) {
        if (position < 0 || position >= facultyAdminList.size()) return;

        FacultyAdminModel admin = facultyAdminList.get(position);
        showUpdateFacultyAdminDialog(admin, docId);
    }

    private String cleanJson(String response) {
        if (response == null) {
            return "{}";
        }

        String clean = response.trim();

        int start = clean.indexOf("{");
        int end = clean.lastIndexOf("}");

        if (start != -1 && end != -1 && end > start) {
            return clean.substring(start, end + 1);
        }

        return clean;
    }

    private void showUpdateFacultyAdminDialog(FacultyAdminModel admin, String userId) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_faculty_admin, null, false);

        EditText editEmail = dialogView.findViewById(R.id.editFacultyAdminEmail);
        AutoCompleteTextView autoFaculty = dialogView.findViewById(R.id.autoFacultyForAdmin);

        editEmail.setText(safe(admin.getEmail()));

        ArrayAdapter<String> facultyAdapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                facultyNames
        );

        autoFaculty.setAdapter(facultyAdapter);
        autoFaculty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoFaculty.showDropDown();
            }
        });

        final FacultyModel[] selectedFaculty = new FacultyModel[1];

        for (int i = 0; i < facultyList.size(); i++) {
            FacultyModel faculty = facultyList.get(i);

            if (safe(faculty.getFacultyId()).equals(safe(admin.getFacultyId()))) {
                selectedFaculty[0] = faculty;

                String label = safe(faculty.getName());
                String code = safe(faculty.getCode());

                if (code.isEmpty()) {
                    autoFaculty.setText(label, false);
                } else {
                    autoFaculty.setText(label + " • " + code, false);
                }

                break;
            }
        }

        autoFaculty.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < facultyList.size()) {
                    selectedFaculty[0] = facultyList.get(position);
                    autoFaculty.setError(null);
                }
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.edit)
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
                String email = getText(editEmail);

                if (TextUtils.isEmpty(email)) {
                    editEmail.setError(getString(R.string.error_required));
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editEmail.setError(getString(R.string.error_invalid_email));
                    return;
                }

                if (selectedFaculty[0] == null) {
                    autoFaculty.setError(getString(R.string.error_select_faculty));
                    return;
                }

                updateFacultyAdmin(userId, email, selectedFaculty[0], dialog);
            }
        });
    }

    private void updateFacultyAdmin(final String userId,
                                    final String email,
                                    final FacultyModel faculty,
                                    final AlertDialog dialog) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.UPDATE_FACULTY_ADMIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(requireContext(), response, Toast.LENGTH_SHORT).show();

                        if (response.contains("updated")) {
                            dialog.dismiss();
                            loadFacultiesThenAdmins();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(requireContext(), "Error updating faculty admin", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("user_id", userId);
                params.put("email", email);
                params.put("faculty_id", safe(faculty.getFacultyId()));
                params.put("fkey", "2026");

                return params;
            }
        };

        queue.add(request);
    }

    @Override
    public void onDeleteClicked(int position, String docId) {
        if (position < 0 || position >= facultyAdminList.size()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(R.string.confirm_delete_faculty_admin)
                .setPositiveButton(R.string.delete, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialogInterface, int which) {
                        deleteFacultyAdmin(position, docId);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteFacultyAdmin(final int position, final String userId) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.DELETE_FACULTY_ADMIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(requireContext(), response, Toast.LENGTH_SHORT).show();

                        if (response.contains("deleted")) {
                            facultyAdminList.remove(position);
                            facultyAdminDocIds.remove(position);
                            adapter.notifyItemRemoved(position);

                            if (facultyAdminList.isEmpty()) {
                                showEmpty(getString(R.string.no_faculty_admins_found));
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(requireContext(), "Error deleting faculty admin", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("user_id", userId);
                params.put("fkey", "2026");

                return params;
            }
        };

        queue.add(request);
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerFacultyAdmins.setVisibility(loading ? View.GONE : View.VISIBLE);
        textEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        recyclerFacultyAdmins.setVisibility(View.GONE);
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
        if (value == null) {
            return "";
        }

        return value.trim();
    }
}