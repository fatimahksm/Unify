package com.university.unify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.university.unify.R;
import com.university.unify.model.StudentAnnouncementModel;

import java.util.List;

public class StudentAnnouncementAdapter extends RecyclerView.Adapter<StudentAnnouncementAdapter.AnnouncementViewHolder> {

    private final Context context;
    private final List<StudentAnnouncementModel> announcements;

    public StudentAnnouncementAdapter(Context context, List<StudentAnnouncementModel> announcements) {
        this.context = context;
        this.announcements = announcements;
    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_announcement, parent, false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
        StudentAnnouncementModel item = announcements.get(position);

        String title = display(item.getTitle());
        String body = display(item.getBody());
        String course = display(item.getCourseTitle());
        String courseCode = display(item.getCourseCode());
        String createdBy = display(item.getCreatedByName());
        String createdAt = display(item.getCreatedAt());

        holder.textAnnouncementTitle.setText(title);
        holder.textAnnouncementBody.setText(body);

        String courseText = course;
        if (!courseCode.equals(context.getString(R.string.not_available_short))) {
            courseText = course + " (" + courseCode + ")";
        }

        holder.textAnnouncementCourse.setText(context.getString(
                R.string.announcement_course_value,
                courseText
        ));

        holder.textAnnouncementBy.setText(context.getString(
                R.string.announcement_by_value,
                createdBy
        ));

        holder.textAnnouncementDate.setText(context.getString(
                R.string.announcement_date_value,
                createdAt
        ));

        holder.textPinned.setVisibility(isPinned(item.getIsPinned()) ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v ->
                Toast.makeText(context, title, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return announcements == null ? 0 : announcements.size();
    }

    private boolean isPinned(String value) {
        if (value == null) {
            return false;
        }

        String clean = value.trim();

        return clean.equals("1")
                || clean.equalsIgnoreCase("true")
                || clean.equalsIgnoreCase("yes");
    }

    private String display(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return context.getString(R.string.not_available_short);
        }

        return value.trim();
    }

    static class AnnouncementViewHolder extends RecyclerView.ViewHolder {

        TextView textAnnouncementTitle;
        TextView textAnnouncementBody;
        TextView textAnnouncementCourse;
        TextView textAnnouncementBy;
        TextView textAnnouncementDate;
        TextView textPinned;

        public AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);

            textAnnouncementTitle = itemView.findViewById(R.id.textAnnouncementTitle);
            textAnnouncementBody = itemView.findViewById(R.id.textAnnouncementBody);
            textAnnouncementCourse = itemView.findViewById(R.id.textAnnouncementCourse);
            textAnnouncementBy = itemView.findViewById(R.id.textAnnouncementBy);
            textAnnouncementDate = itemView.findViewById(R.id.textAnnouncementDate);
            textPinned = itemView.findViewById(R.id.textPinned);
        }
    }
}