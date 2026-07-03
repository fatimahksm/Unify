package com.university.unify.fragments.faculty_admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.adapter.InstructorAdapter;
import com.university.unify.constants.UserRoles;
import com.university.unify.helpers.EmailJsSender;
import com.university.unify.model.UserModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class FacultyAdminInstructorsFragment extends Fragment implements InstructorAdapter.InstructorActionListener {

    private RecyclerView recyclerInstructors;
    private ProgressBar progressBar;
    private TextView textEmpty;
    private FloatingActionButton fabAddInstructor;

    private InstructorAdapter adapter;
    private final List<UserModel> instructorList = new ArrayList<UserModel>();
    private final List<String> instructorDocIds = new ArrayList<String>();

    private String currentFacultyId = "";

    public FacultyAdminInstructorsFragment() {
        super(R.layout.fragment_faculty_admin_instructors);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerInstructors = view.findViewById(R.id.recyclerInstructors);
        progressBar = view.findViewById(R.id.progressBarInstructors);
        textEmpty = view.findViewById(R.id.textEmptyInstructors);
        fabAddInstructor = view.findViewById(R.id.fabAddInstructor);

        adapter = new InstructorAdapter(requireContext(), instructorList, instructorDocIds, this);
        recyclerInstructors.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerInstructors.setAdapter(adapter);

        fabAddInstructor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddInstructorDialog();
            }
        });

        loadFacultyFromSessionThenInstructors();
    }

    private void loadFacultyFromSessionThenInstructors() {
        DatabaseHelper db = new DatabaseHelper(requireContext());
        currentFacultyId = safe(db.getLoggedInFacultyId());

        if (TextUtils.isEmpty(currentFacultyId)) {
            showEmpty(getString(R.string.error_faculty_not_assigned));
            return;
        }

        loadInstructors();
    }

    private void loadInstructors() {
        showLoading(true);

        String url = ApiConfig.GET_INSTRUCTORS_BY_FACULTY + "?faculty_id=" + currentFacultyId;

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
                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);

                            instructorList.clear();
                            instructorDocIds.clear();

                            if (!success) {
                                showEmpty(obj.optString(
                                        "message",
                                        getString(R.string.error_loading_instructors)
                                ));
                                return;
                            }

                            JSONArray data = obj.optJSONArray("data");

                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject item = data.getJSONObject(i);

                                    String fullName = item.optString("full_name", "");
                                    String[] parts = splitName(fullName);

                                    UserModel user = new UserModel();
                                    user.setFirstName(parts[0]);
                                    user.setLastName(parts[1]);
                                    user.setEmail(item.optString("email", ""));
                                    user.setRole(UserRoles.INSTRUCTOR);
                                    user.setFacultyId(item.optString("faculty_id", ""));
                                    user.setFacultyName(item.optString("faculty_name", ""));
                                    user.setPhone(item.optString("phone_number", ""));
                                    user.setProfileImageUrl(item.optString("profile_image_url", ""));
                                    user.setApproved("1".equals(item.optString("is_approved", "1")));
                                    user.setActive("1".equals(item.optString("is_active", "1")));
                                    user.setEmployeeId(item.optString("employee_id", ""));

                                    instructorList.add(user);
                                    instructorDocIds.add(item.optString("user_id", ""));
                                }
                            }

                            adapter.notifyDataSetChanged();
                            showLoading(false);

                            if (instructorList.isEmpty()) {
                                showEmpty(getString(R.string.no_instructors_found));
                            } else {
                                recyclerInstructors.setVisibility(View.VISIBLE);
                                textEmpty.setVisibility(View.GONE);
                            }

                        } catch (Exception e) {
                            showEmpty(getString(R.string.error_loading_instructors));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded()) {
                            return;
                        }

                        showEmpty(getString(R.string.error_loading_instructors));
                    }
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void showAddInstructorDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_instructor, null, false);

        EditText editEmail = dialogView.findViewById(R.id.editInstructorEmail);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_instructor)
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

                if (!valid) {
                    return;
                }

                saveInstructor(email, dialog);
            }
        });
    }

    private void saveInstructor(final String email,
                                final AlertDialog dialog) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADD_INSTRUCTOR,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) {
                            return;
                        }

                        try {
                            JSONObject obj = new JSONObject(response);

                            boolean success = obj.optBoolean("success", false);
                            String message = obj.optString(
                                    "message",
                                    getString(R.string.error_adding_instructor)
                            );

                            if (success) {
                                String returnedEmail = obj.optString("email", email);
                                String tempPassword = obj.optString("temporary_password", "");

                                if (!TextUtils.isEmpty(tempPassword)) {
                                    FirebaseAuth.getInstance()
                                            .createUserWithEmailAndPassword(returnedEmail, tempPassword);

                                    EmailJsSender.sendInstructorCredentials(
                                            requireContext(),
                                            returnedEmail,
                                            tempPassword,
                                            "Instructor"
                                    );
                                }

                                dialog.dismiss();
                                loadInstructors();

                                if (TextUtils.isEmpty(tempPassword)) {
                                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(
                                            requireContext(),
                                            "Instructor created. Credentials email is being sent.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    showTemporaryPasswordDialog(tempPassword);
                                }

                            } else {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                            }

                        } catch (Exception e) {
                            Toast.makeText(
                                    requireContext(),
                                    getString(R.string.error_adding_instructor),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                getString(R.string.error_adding_instructor),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("email", email);
                params.put("faculty_id", currentFacultyId);

                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void showTemporaryPasswordDialog(String tempPassword) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.temporary_password)
                .setMessage(getString(R.string.temporary_password_message) + "\n\n" + tempPassword)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    @Override
    public void onDeleteClicked(final int position, final String userId) {
        if (position < 0 || position >= instructorList.size()) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(R.string.confirm_delete_instructor)
                .setPositiveButton(R.string.delete, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        deleteInstructor(position, userId);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteInstructor(final int position, final String userId) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.DELETE_INSTRUCTOR,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) {
                            return;
                        }

                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);
                            String message = obj.optString(
                                    "message",
                                    getString(R.string.error_deleting_instructor)
                            );

                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                            if (success) {
                                instructorList.remove(position);
                                instructorDocIds.remove(position);
                                adapter.notifyItemRemoved(position);

                                if (instructorList.isEmpty()) {
                                    showEmpty(getString(R.string.no_instructors_found));
                                }
                            }

                        } catch (Exception e) {
                            Toast.makeText(
                                    requireContext(),
                                    getString(R.string.error_deleting_instructor),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                getString(R.string.error_deleting_instructor),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", userId);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private String[] splitName(String fullName) {
        String safeName = safe(fullName);
        String[] result = new String[]{"", ""};

        if (safeName.isEmpty()) {
            return result;
        }

        String[] parts = safeName.split(" ", 2);
        result[0] = parts[0];

        if (parts.length > 1) {
            result[1] = parts[1];
        }

        return result;
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerInstructors.setVisibility(loading ? View.GONE : View.VISIBLE);
        textEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        recyclerInstructors.setVisibility(View.GONE);
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