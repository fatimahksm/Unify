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
import com.university.unify.model.FacultyAdminModel;

import java.util.List;

public class FacultyAdminAdapter extends RecyclerView.Adapter<FacultyAdminAdapter.FacultyAdminViewHolder> {

    public interface FacultyAdminActionListener {
        void onUpdateClicked(int position, String docId);
        void onDeleteClicked(int position, String docId);
    }

    private final Context context;
    private final List<FacultyAdminModel> items;
    private final List<String> docIds;
    private final FacultyAdminActionListener listener;

    public FacultyAdminAdapter(
            Context context,
            List<FacultyAdminModel> items,
            List<String> docIds,
            FacultyAdminActionListener listener
    ) {
        this.context = context;
        this.items = items;
        this.docIds = docIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FacultyAdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_faculty_admin, parent, false);
        return new FacultyAdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FacultyAdminViewHolder holder, int position) {
        FacultyAdminModel item = items.get(position);

        String fullName = (safe(item.getFirstName()) + " " + safe(item.getLastName())).trim();

        holder.textName.setText(fullName.isEmpty() ? context.getString(R.string.not_available_short) : fullName);
        holder.textEmail.setText(context.getString(R.string.profile_email_value, safe(item.getEmail())));
        holder.textEmployeeId.setText(context.getString(R.string.employee_id_value, safe(item.getEmployeeId())));
        holder.textFaculty.setText(context.getString(R.string.profile_faculty_value, safe(item.getFacultyName())));

        holder.buttonUpdate.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();

            if (listener != null
                    && adapterPosition != RecyclerView.NO_POSITION
                    && adapterPosition < docIds.size()) {
                listener.onUpdateClicked(adapterPosition, docIds.get(adapterPosition));
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();

            if (listener != null
                    && adapterPosition != RecyclerView.NO_POSITION
                    && adapterPosition < docIds.size()) {
                listener.onDeleteClicked(adapterPosition, docIds.get(adapterPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FacultyAdminViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textEmail, textEmployeeId, textFaculty;
        MaterialButton buttonUpdate, buttonDelete;

        public FacultyAdminViewHolder(@NonNull View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.textFacultyAdminName);
            textEmail = itemView.findViewById(R.id.textFacultyAdminEmail);
            textEmployeeId = itemView.findViewById(R.id.textFacultyAdminEmployeeId);
            textFaculty = itemView.findViewById(R.id.textFacultyAdminFaculty);

            buttonUpdate = itemView.findViewById(R.id.buttonUpdateFacultyAdmin);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteFacultyAdmin);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}