package com.university.unify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.university.unify.R;
import com.university.unify.model.UserModel;

import java.util.List;

public class PendingUsersAdapter extends RecyclerView.Adapter<PendingUsersAdapter.PendingUserViewHolder> {

    public interface PendingUserActionListener {
        void onApproveClicked(int position, String docId);
        void onRejectClicked(int position, String docId);
    }

    private final Context context;
    private final List<UserModel> items;
    private final List<String> docIds;
    private final PendingUserActionListener listener;

    public PendingUsersAdapter(Context context, List<UserModel> items, List<String> docIds, PendingUserActionListener listener) {
        this.context = context;
        this.items = items;
        this.docIds = docIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PendingUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pending_user, parent, false);
        return new PendingUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingUserViewHolder holder, int position) {
        UserModel item = items.get(position);

        String fullName = (safe(item.getFirstName()) + " " + safe(item.getLastName())).trim();

        holder.textName.setText(fullName.isEmpty() ? context.getString(R.string.not_available_short) : fullName);
        holder.textEmail.setText(context.getString(R.string.profile_email_value, safe(item.getEmail())));
        holder.textStudentId.setText(context.getString(R.string.profile_student_id_value, safe(item.getStudentNumber())));
        holder.textFaculty.setText(context.getString(R.string.profile_faculty_value, safe(item.getFacultyName())));
        holder.textMajor.setText(context.getString(R.string.profile_major_value, safe(item.getMajorName())));

        holder.buttonApprove.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION && adapterPosition < docIds.size()) {
                listener.onApproveClicked(adapterPosition, docIds.get(adapterPosition));
            }
        });

        holder.buttonReject.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION && adapterPosition < docIds.size()) {
                listener.onRejectClicked(adapterPosition, docIds.get(adapterPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PendingUserViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textEmail, textStudentId, textFaculty, textMajor;
        MaterialButton buttonApprove, buttonReject;

        public PendingUserViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textPendingUserName);
            textEmail = itemView.findViewById(R.id.textPendingUserEmail);
            textStudentId = itemView.findViewById(R.id.textPendingUserStudentId);
            textFaculty = itemView.findViewById(R.id.textPendingUserFaculty);
            textMajor = itemView.findViewById(R.id.textPendingUserMajor);
            buttonApprove = itemView.findViewById(R.id.buttonApproveUser);
            buttonReject = itemView.findViewById(R.id.buttonRejectUser);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}