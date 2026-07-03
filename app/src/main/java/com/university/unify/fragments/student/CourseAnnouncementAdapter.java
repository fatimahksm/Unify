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
import com.university.unify.model.CourseAnnouncementModel;

import java.util.List;

public class CourseAnnouncementAdapter extends RecyclerView.Adapter<CourseAnnouncementAdapter.CourseAnnouncementViewHolder> {

    private final Context context;
    private final List<CourseAnnouncementModel> announcements;

    public CourseAnnouncementAdapter(Context context, List<CourseAnnouncementModel> announcements) {
        this.context = context;
        this.announcements = announcements;
    }

    @NonNull
    @Override
    public CourseAnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course_announcement, parent, false);
        return new CourseAnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseAnnouncementViewHolder holder, int position) {
        CourseAnnouncementModel item = announcements.get(position);

        String title = display(item.getTitle());
        String body = display(item.getBody());
        String createdBy = display(item.getCreatedByName());
        String createdAt = display(item.getCreatedAt());

        holder.textTitle.setText(title);
        holder.textBody.setText(body);
        holder.textMeta.setText(context.getString(
                R.string.announcement_by_date_value,
                createdBy,
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

    static class CourseAnnouncementViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle;
        TextView textBody;
        TextView textMeta;
        TextView textPinned;

        public CourseAnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);

            textTitle = itemView.findViewById(R.id.textCourseAnnouncementTitle);
            textBody = itemView.findViewById(R.id.textCourseAnnouncementBody);
            textMeta = itemView.findViewById(R.id.textCourseAnnouncementMeta);
            textPinned = itemView.findViewById(R.id.textCourseAnnouncementPinned);
        }
    }
}