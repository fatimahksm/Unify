package com.university.unify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.university.unify.R;
import com.university.unify.model.UserModel;

import java.util.List;

public class InstructorAdapter extends RecyclerView.Adapter<InstructorAdapter.InstructorViewHolder> {

    public interface InstructorActionListener {
        void onDeleteClicked(int position, String docId);
    }

    private final Context context;
    private final List<UserModel> items;
    private final List<String> docIds;
    private final InstructorActionListener listener;

    public InstructorAdapter(Context context,
                             List<UserModel> items,
                             List<String> docIds,
                             InstructorActionListener listener) {
        this.context = context;
        this.items = items;
        this.docIds = docIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InstructorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_instructor, parent, false);
        return new InstructorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstructorViewHolder holder, int position) {
        UserModel item = items.get(position);

        String fullName = (safe(item.getFirstName()) + " " + safe(item.getLastName())).trim();
        holder.textName.setText(fullName.isEmpty() ? "-" : fullName);
        holder.textEmail.setText(safe(item.getEmail()).isEmpty() ? "-" : safe(item.getEmail()));
        holder.textEmployeeId.setText(
                context.getString(R.string.employee_id_value,
                        safe(item.getEmployeeId()).isEmpty()
                                ? context.getString(R.string.not_available_short)
                                : safe(item.getEmployeeId()))
        );

        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null && position >= 0 && position < docIds.size()) {
                listener.onDeleteClicked(position, docIds.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class InstructorViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textEmail, textEmployeeId;
        ImageButton buttonDelete;

        public InstructorViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textInstructorName);
            textEmail = itemView.findViewById(R.id.textInstructorEmail);
            textEmployeeId = itemView.findViewById(R.id.textInstructorEmployeeId);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteInstructor);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}