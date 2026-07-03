package com.university.unify.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.university.unify.R;
import com.university.unify.activities.CourseDetailsActivity;
import com.university.unify.model.CourseModel;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private final Context context;
    private final List<CourseModel> items;

    public CourseAdapter(Context context, List<CourseModel> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        final CourseModel item = items.get(position);

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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCourseDetails(item);
            }
        });
    }

    private void openCourseDetails(CourseModel item) {
        Intent intent = new Intent(context, CourseDetailsActivity.class);

        intent.putExtra(CourseDetailsActivity.EXTRA_COURSE_ID, safe(item.getCourseId()));
        intent.putExtra(CourseDetailsActivity.EXTRA_COURSE_CODE, safe(item.getCode()));
        intent.putExtra(CourseDetailsActivity.EXTRA_COURSE_TITLE, safe(item.getTitle()));
        intent.putExtra(CourseDetailsActivity.EXTRA_COURSE_DESCRIPTION, safe(item.getDescription()));
        intent.putExtra(CourseDetailsActivity.EXTRA_SECTION, safe(item.getSection()));
        intent.putExtra(CourseDetailsActivity.EXTRA_DEPARTMENT, safe(item.getDepartment()));
        intent.putExtra(CourseDetailsActivity.EXTRA_FACULTY_NAME, safe(item.getFacultyName()));
        intent.putExtra(CourseDetailsActivity.EXTRA_FACULTY_ID, safe(item.getFacultyId()));
        intent.putExtra(CourseDetailsActivity.EXTRA_INSTRUCTOR_ID, safe(item.getInstructorId()));
        intent.putExtra(CourseDetailsActivity.EXTRA_STUDY_YEAR, safe(item.getStudyYear()));
        intent.putExtra(CourseDetailsActivity.EXTRA_SEMESTER, safe(item.getSemester()));
        intent.putExtra(CourseDetailsActivity.EXTRA_ACADEMIC_YEAR, safe(item.getAcademicYear()));

        intent.putExtra(CourseDetailsActivity.EXTRA_MAJOR_NAME, safe(item.getMajorName()));
        intent.putExtra(CourseDetailsActivity.EXTRA_INSTRUCTOR_NAME, safe(item.getInstructorName()));
        intent.putExtra(CourseDetailsActivity.EXTRA_CREDITS, String.valueOf(item.getCredits()));
        intent.putExtra(CourseDetailsActivity.EXTRA_SCHEDULE, safe(item.getScheduleText()));

        // Course lifecycle dates
        intent.putExtra(CourseDetailsActivity.EXTRA_ENROLLMENT_START, safe(item.getEnrollmentStartAt()));
        intent.putExtra(CourseDetailsActivity.EXTRA_ENROLLMENT_END, safe(item.getEnrollmentEndAt()));
        intent.putExtra(CourseDetailsActivity.EXTRA_COURSE_START, safe(item.getCourseStartAt()));
        intent.putExtra(CourseDetailsActivity.EXTRA_COURSE_END, safe(item.getCourseEndAt()));

        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }

        return items.size();
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

    private String safe(String value) {
        if (value == null || value.equalsIgnoreCase("null")) {
            return "";
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