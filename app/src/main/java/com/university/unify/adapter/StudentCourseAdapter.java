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
import com.university.unify.activities.StudentCourseDetailsActivity;
import com.university.unify.model.StudentCourseModel;

import java.util.List;
import java.util.Locale;

public class StudentCourseAdapter extends RecyclerView.Adapter<StudentCourseAdapter.CourseViewHolder> {

    private static final String STATUS_UPCOMING = "UPCOMING";
    private static final String STATUS_NOT_STARTED = "NOT_STARTED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_ONGOING = "ONGOING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FINISHED = "FINISHED";
    private static final String STATUS_ENDED = "ENDED";

    private final Context context;
    private final List<StudentCourseModel> courses;

    public StudentCourseAdapter(Context context, List<StudentCourseModel> courses) {
        this.context = context;
        this.courses = courses;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        StudentCourseModel course = courses.get(position);

        String title = display(course.getTitle());
        String code = display(course.getCode());
        String instructorName = display(course.getInstructorName());

        holder.textCourseTitle.setText(title);
        holder.textCourseCode.setText(code);
        holder.textCourseInstructor.setText(context.getString(
                R.string.instructor_value,
                instructorName
        ));

        String semester = display(course.getSemester());
        String academicYear = display(course.getAcademicYear());
        holder.textCourseMeta.setText(semester + " • " + academicYear);

        holder.textCourseInitial.setText(getInitial(title));

        applyStatusBadge(holder, course);
        applyGrade(holder, course);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StudentCourseDetailsActivity.class);

            intent.putExtra("course_id", course.getCourseId());
            intent.putExtra("title", course.getTitle());
            intent.putExtra("code", course.getCode());
            intent.putExtra("description", course.getDescription());
            intent.putExtra("section", course.getSection());
            intent.putExtra("department", course.getDepartment());
            intent.putExtra("instructor_id", course.getInstructorId());
            intent.putExtra("instructor_name", course.getInstructorName());
            intent.putExtra("semester", course.getSemester());
            intent.putExtra("academic_year", course.getAcademicYear());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return courses == null ? 0 : courses.size();
    }

    /**
     * Pick the most authoritative status from the model.
     * Priority: calculatedStatus → courseStatus (matches CourseModel.getBestStatus()).
     */
    private String pickStatus(StudentCourseModel course) {
        String calculated = course.getCalculatedStatus();

        if (calculated != null && !calculated.trim().isEmpty()
                && !calculated.equalsIgnoreCase("null")) {
            return calculated.trim().toUpperCase(Locale.ROOT);
        }

        String courseStatus = course.getCourseStatus();

        if (courseStatus != null && !courseStatus.trim().isEmpty()
                && !courseStatus.equalsIgnoreCase("null")) {
            return courseStatus.trim().toUpperCase(Locale.ROOT);
        }

        return "";
    }

    private void applyStatusBadge(CourseViewHolder holder, StudentCourseModel course) {
        String status = pickStatus(course);

        if (status.isEmpty()) {
            holder.textStatusBadge.setVisibility(View.GONE);
            return;
        }

        if (status.equals(STATUS_UPCOMING)
                || status.equals(STATUS_NOT_STARTED)
                || status.equals(STATUS_PENDING)) {

            holder.textStatusBadge.setText(R.string.status_upcoming);
            holder.textStatusBadge.setBackgroundResource(R.drawable.bg_status_upcoming);
            holder.textStatusBadge.setVisibility(View.VISIBLE);
            return;
        }

        if (status.equals(STATUS_IN_PROGRESS)
                || status.equals(STATUS_ACTIVE)
                || status.equals(STATUS_ONGOING)) {

            holder.textStatusBadge.setText(R.string.status_in_progress);
            holder.textStatusBadge.setBackgroundResource(R.drawable.bg_status_active);
            holder.textStatusBadge.setVisibility(View.VISIBLE);
            return;
        }

        if (status.equals(STATUS_COMPLETED)
                || status.equals(STATUS_FINISHED)
                || status.equals(STATUS_ENDED)) {

            holder.textStatusBadge.setText(R.string.status_completed);
            holder.textStatusBadge.setBackgroundResource(R.drawable.bg_status_completed);
            holder.textStatusBadge.setVisibility(View.VISIBLE);
            return;
        }

        holder.textStatusBadge.setVisibility(View.GONE);
    }

    /**
     * Show the final grade only when:
     *   1. course is completed, AND
     *   2. result is published, AND
     *   3. a grade value exists.
     */
    private void applyGrade(CourseViewHolder holder, StudentCourseModel course) {
        String status = pickStatus(course);

        boolean isCompleted = status.equals(STATUS_COMPLETED)
                || status.equals(STATUS_FINISHED)
                || status.equals(STATUS_ENDED);

        if (!isCompleted) {
            holder.textGrade.setVisibility(View.GONE);
            return;
        }

        if (!course.isResultPublished()) {
            holder.textGrade.setVisibility(View.GONE);
            return;
        }

        String grade = course.getFinalGrade();

        if (grade == null
                || grade.trim().isEmpty()
                || grade.equalsIgnoreCase("null")) {
            holder.textGrade.setVisibility(View.GONE);
            return;
        }

        holder.textGrade.setText(context.getString(R.string.grade_value, grade.trim()));
        holder.textGrade.setVisibility(View.VISIBLE);
    }

    private String getInitial(String title) {
        if (title == null || title.trim().isEmpty() || title.equals("-")) {
            return context.getString(R.string.course_default_initial);
        }

        return title.trim().substring(0, 1).toUpperCase();
    }

    private String display(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return context.getString(R.string.not_available_short);
        }

        return value.trim();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView textCourseInitial;
        TextView textCourseTitle;
        TextView textCourseCode;
        TextView textCourseInstructor;
        TextView textCourseMeta;
        TextView textStatusBadge;
        TextView textGrade;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);

            textCourseInitial = itemView.findViewById(R.id.textCourseInitial);
            textCourseTitle = itemView.findViewById(R.id.textCourseTitle);
            textCourseCode = itemView.findViewById(R.id.textCourseCode);
            textCourseInstructor = itemView.findViewById(R.id.textCourseInstructor);
            textCourseMeta = itemView.findViewById(R.id.textCourseMeta);
            textStatusBadge = itemView.findViewById(R.id.textStatusBadge);
            textGrade = itemView.findViewById(R.id.textGrade);
        }
    }
}