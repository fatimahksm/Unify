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

public class FacultyAdminCourseAdapter extends RecyclerView.Adapter<FacultyAdminCourseAdapter.CourseViewHolder> {

    public interface CourseActionListener {
        void onCourseClicked(int position, String courseId, CourseModel course);
    }

    private final Context context;
    private final List<CourseModel> items;
    private final CourseActionListener listener;

    public FacultyAdminCourseAdapter(Context context,
                                     List<CourseModel> items,
                                     CourseActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        CourseModel item = items.get(position);

        String title = display(item.getTitle());
        String code = display(item.getCode());
        String major = display(item.getMajorName());
        String instructor = display(item.getInstructorName());
        String semester = display(item.getSemester());
        String credits = String.valueOf(item.getCredits());

        String schedule = display(item.getScheduleText());
        if (schedule.equals(context.getString(R.string.not_available_short))) {
            schedule = context.getString(R.string.no_schedule_available);
        }

        holder.textInitial.setText(getInitial(title));
        holder.textCode.setText(code);
        holder.textName.setText(title);

        holder.textMajor.setText(context.getString(R.string.major_value, major));
        holder.textInstructor.setText(context.getString(R.string.instructor_value, instructor));
        holder.textSchedule.setText(context.getString(R.string.schedule_value, schedule));
        holder.textSemester.setText(context.getString(R.string.semester_value, semester));
        holder.textCredits.setText(context.getString(R.string.credits_value, credits));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCourseClicked(
                        holder.getAdapterPosition(),
                        display(item.getCourseId()),
                        item
                );
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
            return context.getString(R.string.course_default_initial);
        }

        return value.trim().substring(0, 1).toUpperCase();
    }

    private String display(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return context.getString(R.string.not_available_short);
        }

        return value.trim();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView textInitial;
        TextView textCode;
        TextView textName;
        TextView textMajor;
        TextView textInstructor;
        TextView textSchedule;
        TextView textSemester;
        TextView textCredits;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);

            textInitial = itemView.findViewById(R.id.textCourseInitial);
            textCode = itemView.findViewById(R.id.textCourseCode);
            textName = itemView.findViewById(R.id.textCourseName);
            textMajor = itemView.findViewById(R.id.textCourseMajor);
            textInstructor = itemView.findViewById(R.id.textCourseInstructor);
            textSchedule = itemView.findViewById(R.id.textCourseSchedule);
            textSemester = itemView.findViewById(R.id.textCourseSemester);
            textCredits = itemView.findViewById(R.id.textCourseCredits);
        }
    }
}