package com.university.unify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.university.unify.R;
import com.university.unify.model.CourseInstructorModel;

import java.util.List;

public class CourseInstructorAdapter extends RecyclerView.Adapter<CourseInstructorAdapter.InstructorViewHolder> {

    public interface OnInstructorActionListener {
        void onRemoveClicked(int position, CourseInstructorModel instructor);
    }

    private final Context context;
    private final List<CourseInstructorModel> items;
    private final OnInstructorActionListener listener;

    public CourseInstructorAdapter(Context context,
                                   List<CourseInstructorModel> items,
                                   OnInstructorActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InstructorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course_instructor, parent, false);
        return new InstructorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstructorViewHolder holder, int position) {
        CourseInstructorModel item = items.get(position);

        String name = display(item.getFullName());
        String email = display(item.getEmail());

        holder.textInitial.setText(getInitial(name));
        holder.textName.setText(name);
        holder.textEmail.setText(email);

        holder.buttonRemove.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();

            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onRemoveClicked(adapterPosition, item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    private String getInitial(String value) {
        if (value == null
                || value.trim().isEmpty()
                || value.equals(context.getString(R.string.not_available_short))) {
            return "-";
        }

        return value.trim().substring(0, 1).toUpperCase();
    }

    private String display(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return context.getString(R.string.not_available_short);
        }

        return value.trim();
    }

    static class InstructorViewHolder extends RecyclerView.ViewHolder {

        TextView textInitial;
        TextView textName;
        TextView textEmail;
        TextView buttonRemove;

        public InstructorViewHolder(@NonNull View itemView) {
            super(itemView);

            textInitial = itemView.findViewById(R.id.textInstructorInitial);
            textName = itemView.findViewById(R.id.textInstructorName);
            textEmail = itemView.findViewById(R.id.textInstructorEmail);
            buttonRemove = itemView.findViewById(R.id.buttonRemoveInstructor);
        }
    }
}