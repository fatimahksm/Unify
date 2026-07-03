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
import com.university.unify.model.MajorModel;

import java.util.List;

public class MajorAdapter extends RecyclerView.Adapter<MajorAdapter.MajorViewHolder> {

    public interface MajorActionListener {
        void onDeleteClicked(int position, String docId);
    }

    private final Context context;
    private final List<MajorModel> items;
    private final List<String> docIds;
    private final MajorActionListener listener;

    public MajorAdapter(Context context, List<MajorModel> items, List<String> docIds, MajorActionListener listener) {
        this.context = context;
        this.items = items;
        this.docIds = docIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MajorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_major, parent, false);
        return new MajorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MajorViewHolder holder, int position) {
        MajorModel item = items.get(position);

        holder.textMajorName.setText(safe(item.getName()));
        holder.textMajorCode.setText(context.getString(R.string.major_code_value, safe(item.getCode())));
        holder.textMajorFaculty.setText(context.getString(R.string.major_faculty_value, safe(item.getFacultyName())));
        holder.textMajorId.setText(context.getString(R.string.major_id_value, safe(item.getMajorId())));

        holder.buttonDeleteMajor.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION && adapterPosition < docIds.size()) {
                listener.onDeleteClicked(adapterPosition, docIds.get(adapterPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MajorViewHolder extends RecyclerView.ViewHolder {
        TextView textMajorName, textMajorCode, textMajorFaculty, textMajorId;
        MaterialButton buttonDeleteMajor;

        public MajorViewHolder(@NonNull View itemView) {
            super(itemView);
            textMajorName = itemView.findViewById(R.id.textMajorName);
            textMajorCode = itemView.findViewById(R.id.textMajorCode);
            textMajorFaculty = itemView.findViewById(R.id.textMajorFaculty);
            textMajorId = itemView.findViewById(R.id.textMajorId);
            buttonDeleteMajor = itemView.findViewById(R.id.buttonDeleteMajor);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}