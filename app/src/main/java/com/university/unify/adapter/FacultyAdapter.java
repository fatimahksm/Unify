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
import com.university.unify.model.FacultyModel;

import java.util.List;

public class FacultyAdapter extends RecyclerView.Adapter<FacultyAdapter.FacultyViewHolder> {

    public interface FacultyActionListener {
        void onDeleteClicked(int position, String docId);
        void onEditClicked(int position, String docId);
    }

    private final Context context;
    private final List<FacultyModel> items;
    private final List<String> docIds;
    private final FacultyActionListener listener;

    public FacultyAdapter(Context context, List<FacultyModel> items, List<String> docIds, FacultyActionListener listener) {
        this.context = context;
        this.items = items;
        this.docIds = docIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FacultyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_faculty, parent, false);
        return new FacultyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FacultyViewHolder holder, int position) {
        FacultyModel item = items.get(position);

        holder.textFacultyName.setText(safe(item.getName()));
        holder.textFacultyCode.setText(context.getString(R.string.faculty_code_value, safe(item.getCode())));
        holder.textFacultyId.setText(context.getString(R.string.faculty_id_value, safe(item.getFacultyId())));

        holder.buttonDeleteFaculty.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION && adapterPosition < docIds.size()) {
                listener.onDeleteClicked(adapterPosition, docIds.get(adapterPosition));
            }
        });
        holder.buttonEditFaculty.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION && adapterPosition < docIds.size()) {
                listener.onEditClicked(adapterPosition, docIds.get(adapterPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FacultyViewHolder extends RecyclerView.ViewHolder {
        TextView textFacultyName, textFacultyCode, textFacultyId;
        MaterialButton buttonDeleteFaculty;
        MaterialButton buttonEditFaculty;

        public FacultyViewHolder(@NonNull View itemView) {
            super(itemView);
            textFacultyName = itemView.findViewById(R.id.textFacultyName);
            textFacultyCode = itemView.findViewById(R.id.textFacultyCode);
            textFacultyId = itemView.findViewById(R.id.textFacultyId);
            buttonDeleteFaculty = itemView.findViewById(R.id.buttonDeleteFaculty);
            buttonEditFaculty = itemView.findViewById(R.id.buttonUpdateFaculty);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}