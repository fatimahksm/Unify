package com.university.unify.fragments.faculty_admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.adapter.PendingUsersAdapter;
import com.university.unify.model.UserModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacultyAdminPendingStudentsFragment extends Fragment implements PendingUsersAdapter.PendingUserActionListener {

    private RecyclerView recyclerPendingUsers;
    private ProgressBar progressBar;
    private TextView textEmpty;

    private PendingUsersAdapter adapter;
    private final List<UserModel> pendingUsers = new ArrayList<UserModel>();
    private final List<String> pendingUserIds = new ArrayList<String>();

    private String currentFacultyId = "";

    public FacultyAdminPendingStudentsFragment() {
        super(R.layout.fragment_pending_users);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerPendingUsers = view.findViewById(R.id.recyclerPendingUsers);
        progressBar = view.findViewById(R.id.progressBarPendingUsers);
        textEmpty = view.findViewById(R.id.textEmptyPendingUsers);

        adapter = new PendingUsersAdapter(requireContext(), pendingUsers, pendingUserIds, this);
        recyclerPendingUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPendingUsers.setAdapter(adapter);

        loadFacultyFromSession();
    }

    private void loadFacultyFromSession() {
        DatabaseHelper db = new DatabaseHelper(requireContext());
        currentFacultyId = safe(db.getLoggedInFacultyId());

        if (TextUtils.isEmpty(currentFacultyId)) {
            showEmpty(getString(R.string.error_faculty_not_assigned));
            return;
        }

        loadPendingStudents();
    }

    private void loadPendingStudents() {
        showLoading(true);

        String url = ApiConfig.GET_PENDING_STUDENTS_BY_FACULTY + "?faculty_id=" + currentFacultyId;

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

                            pendingUsers.clear();
                            pendingUserIds.clear();

                            if (!success) {
                                showEmpty(obj.optString("message", getString(R.string.error_loading_pending_users)));
                                return;
                            }

                            JSONArray data = obj.optJSONArray("data");

                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject item = data.getJSONObject(i);

                                    UserModel user = new UserModel();

                                    String fullName = item.optString("full_name", "");
                                    String[] parts = splitName(fullName);

                                    user.setFirstName(parts[0]);
                                    user.setLastName(parts[1]);

                                    user.setEmail(item.optString("email", ""));

                                    user.setFacultyId(item.optString("faculty_id", ""));
                                    user.setFacultyName(item.optString("faculty_name", ""));

                                    user.setMajorId(item.optString("major_id", ""));
                                    user.setMajorName(item.optString("major_name", ""));

                                    user.setStudyYear(item.optString("study_year", ""));
                                    user.setStudentNumber(item.optString("student_number", ""));
                                    user.setPhone(item.optString("phone_number", ""));
                                    user.setProfileImageUrl(item.optString("profile_image_url", ""));

                                    user.setApproved("1".equals(item.optString("is_approved", "0")));
                                    user.setActive("1".equals(item.optString("is_active", "1")));

                                    pendingUsers.add(user);

                                    // this is the real users.user_id from MySQL, used for approve/reject
                                    pendingUserIds.add(item.optString("user_id", ""));
                                }
                            }

                            adapter.notifyDataSetChanged();
                            showLoading(false);

                            if (pendingUsers.isEmpty()) {
                                showEmpty(getString(R.string.no_pending_students_found));
                            } else {
                                recyclerPendingUsers.setVisibility(View.VISIBLE);
                                textEmpty.setVisibility(View.GONE);
                            }

                        } catch (Exception e) {
                            showEmpty(getString(R.string.error_loading_pending_users));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded()) return;
                        showEmpty(getString(R.string.error_loading_pending_users));
                    }
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    @Override
    public void onApproveClicked(final int position, final String userId) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.APPROVE_STUDENT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) return;

                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);

                            Toast.makeText(requireContext(), obj.optString("message"), Toast.LENGTH_SHORT).show();

                            if (success) {
                                removeItem(position);
                            }

                        } catch (Exception ignored) {}
                    }
                },
                null
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", userId);
                params.put("faculty_id", currentFacultyId);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    @Override
    public void onRejectClicked(final int position, final String userId) {

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.reject_user)
                .setMessage(R.string.reject_user_confirmation)
                .setPositiveButton(R.string.reject, (dialog, which) -> rejectStudent(position, userId))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void rejectStudent(final int position, final String userId) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.REJECT_STUDENT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) return;

                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);

                            Toast.makeText(requireContext(), obj.optString("message"), Toast.LENGTH_SHORT).show();

                            if (success) {
                                removeItem(position);
                            }

                        } catch (Exception ignored) {}
                    }
                },
                null
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", userId);
                params.put("faculty_id", currentFacultyId);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void removeItem(int position) {
        if (position >= 0 && position < pendingUsers.size()) {
            pendingUsers.remove(position);
            pendingUserIds.remove(position);
            adapter.notifyItemRemoved(position);
        }

        if (pendingUsers.isEmpty()) {
            showEmpty(getString(R.string.no_pending_students_found));
        }
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerPendingUsers.setVisibility(loading ? View.GONE : View.VISIBLE);
        textEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        recyclerPendingUsers.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        textEmpty.setVisibility(View.VISIBLE);
        textEmpty.setText(message);
    }

    private String[] splitName(String fullName) {
        String[] result = new String[]{"", ""};
        if (TextUtils.isEmpty(fullName)) return result;

        String[] parts = fullName.split(" ", 2);
        result[0] = parts[0];
        if (parts.length > 1) result[1] = parts[1];

        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}