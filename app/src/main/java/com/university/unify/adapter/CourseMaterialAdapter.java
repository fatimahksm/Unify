package com.university.unify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.university.unify.R;
import com.university.unify.model.CourseMaterialModel;

import java.util.List;

public class CourseMaterialAdapter extends RecyclerView.Adapter<CourseMaterialAdapter.MaterialViewHolder> {

    public interface OnMaterialActionListener {
        void onOpenClicked(CourseMaterialModel material);
        void onDeleteClicked(int position, CourseMaterialModel material);
    }

    private final Context context;
    private final List<CourseMaterialModel> materials;
    private final boolean showDeleteButton;
    private final OnMaterialActionListener listener;

    public CourseMaterialAdapter(Context context,
                                 List<CourseMaterialModel> materials,
                                 boolean showDeleteButton,
                                 OnMaterialActionListener listener) {
        this.context = context;
        this.materials = materials;
        this.showDeleteButton = showDeleteButton;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course_material, parent, false);
        return new MaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        CourseMaterialModel material = materials.get(position);

        holder.textType.setText(display(material.getMaterialType()));
        holder.textTitle.setText(display(material.getTitle()));

        String description = display(material.getDescription());
        holder.textDescription.setText(description);
        holder.textDate.setText(display(material.getCreatedAt()));

        holder.buttonDelete.setVisibility(showDeleteButton ? View.VISIBLE : View.GONE);

        holder.buttonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onOpenClicked(material);
            }
        });

        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onDeleteClicked(holder.getAdapterPosition(), material);
            }
        });
    }

    @Override
    public int getItemCount() {
        return materials == null ? 0 : materials.size();
    }

    private String display(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return "-";
        }

        return value.trim();
    }

    static class MaterialViewHolder extends RecyclerView.ViewHolder {

        TextView textType;
        TextView textTitle;
        TextView textDescription;
        TextView textDate;
        Button buttonOpen;
        Button buttonDelete;

        public MaterialViewHolder(@NonNull View itemView) {
            super(itemView);

            textType = itemView.findViewById(R.id.textMaterialType);
            textTitle = itemView.findViewById(R.id.textMaterialTitle);
            textDescription = itemView.findViewById(R.id.textMaterialDescription);
            textDate = itemView.findViewById(R.id.textMaterialDate);
            buttonOpen = itemView.findViewById(R.id.buttonOpenMaterial);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteMaterial);
        }
    }
}