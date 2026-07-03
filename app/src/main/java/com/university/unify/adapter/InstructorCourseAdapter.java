package com.university.unify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.university.unify.R;
import com.university.unify.model.CourseModel;

import java.util.List;

public class InstructorCourseAdapter extends RecyclerView.Adapter<InstructorCourseAdapter.CourseViewHolder> {

    private final Context context;
    private final List<CourseModel> courses;

    private final OnCourseClickListener listener;

    public InstructorCourseAdapter(Context context, List<CourseModel> courses, OnCourseClickListener listener) {
        this.context = context;
        this.courses = courses;
        this.listener = listener;
    }
    public interface OnCourseClickListener {
        void onCourseClicked(CourseModel course);
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_instructor_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        CourseModel course = courses.get(position);

        String code = safe(course.getCode());
        String title = safe(course.getTitle());
        String major = safe(course.getMajorName());
        String studyYear = safe(course.getStudyYear());
        String semester = safe(course.getSemester());
        String academicYear = safe(course.getAcademicYear());
        String credits = String.valueOf(course.getCredits());
        String description = safe(course.getDescription());

        holder.textCourseCode.setText(code.isEmpty() ? "-" : code);
        holder.textCourseTitle.setText(title.isEmpty() ? "-" : title);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onCourseClicked(course);
                }
            }
        });

        if (major.isEmpty()) {
            holder.textCourseMajor.setText(context.getString(R.string.major_not_available));
        } else {
            holder.textCourseMajor.setText(major);
        }

        holder.textCourseMeta.setText(
                studyYear + " • " + semester + " • " + academicYear + " • " +
                        context.getString(R.string.credits_value, credits)
        );

        if (description.isEmpty()) {
            holder.textCourseDescription.setVisibility(View.GONE);
        } else {
            holder.textCourseDescription.setVisibility(View.VISIBLE);
            holder.textCourseDescription.setText(description);
        }

    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView textCourseCode;
        TextView textCourseTitle;
        TextView textCourseMajor;
        TextView textCourseMeta;
        TextView textCourseDescription;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);

            textCourseCode = itemView.findViewById(R.id.textCourseCode);
            textCourseTitle = itemView.findViewById(R.id.textCourseTitle);
            textCourseMajor = itemView.findViewById(R.id.textCourseMajor);
            textCourseMeta = itemView.findViewById(R.id.textCourseMeta);
            textCourseDescription = itemView.findViewById(R.id.textCourseDescription);
        }
    }

    private String safe(String value) {
        return value == null || value.equals("null") ? "" : value.trim();
    }
}