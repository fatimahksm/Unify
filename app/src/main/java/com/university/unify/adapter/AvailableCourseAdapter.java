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
import com.university.unify.model.StudentCourseModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AvailableCourseAdapter extends RecyclerView.Adapter<AvailableCourseAdapter.AvailableCourseViewHolder> {

    public interface OnEnrollClickListener {
        void onEnrollClicked(StudentCourseModel course, int position);
    }

    private enum EnrollmentState {
        NOT_STARTED,   // window hasn't opened yet
        OPEN,          // can enroll now
        CLOSED,        // window has passed
        UNKNOWN        // dates missing — let server decide
    }

    private final Context context;
    private final List<StudentCourseModel> courses;
    private final OnEnrollClickListener listener;

    public AvailableCourseAdapter(
            Context context,
            List<StudentCourseModel> courses,
            OnEnrollClickListener listener
    ) {
        this.context = context;
        this.courses = courses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AvailableCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_available_course, parent, false);
        return new AvailableCourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvailableCourseViewHolder holder, int position) {
        StudentCourseModel course = courses.get(position);


        String title    = display(course.getTitle());
        String code     = display(course.getCode());
        String credits  = display(course.getCredits());
        String schedule = display(course.getScheduleText());

        if (schedule.equals(context.getString(R.string.not_available_short))) {
            schedule = context.getString(R.string.no_schedule_available);
        }

        holder.textCourseInitial.setText(getInitial(title));
        holder.textCourseTitle.setText(title);
        holder.textCourseCode.setText(code);
        holder.textCredits.setText(context.getString(R.string.credits_value, credits));
        holder.textSchedule.setText(context.getString(R.string.schedule_value, schedule));

        // ── enrollment window ───────────────────────────────────────────────
        EnrollmentState state = computeEnrollmentState(
                course.getEnrollmentStartAt(),
                course.getEnrollmentEndAt()
        );
        applyEnrollmentState(holder, state, course);

        // ── click ───────────────────────────────────────────────────────────
        holder.buttonEnroll.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) {
                listener.onEnrollClicked(course, pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courses == null ? 0 : courses.size();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Enrollment window helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Compare NOW against the enrollment window using the same
     * "yyyy-MM-dd HH:mm:ss" string format the server writes.
     */
    private EnrollmentState computeEnrollmentState(String startAt, String endAt) {
        if (empty(startAt) || empty(endAt)) {
            return EnrollmentState.UNKNOWN;
        }

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
                .format(new Date());

        if (now.compareTo(startAt) < 0) return EnrollmentState.NOT_STARTED;
        if (now.compareTo(endAt)   > 0) return EnrollmentState.CLOSED;
        return EnrollmentState.OPEN;
    }

    private void applyEnrollmentState(
            AvailableCourseViewHolder holder,
            EnrollmentState state,
            StudentCourseModel course
    ) {
        switch (state) {

            case OPEN:
                holder.buttonEnroll.setEnabled(true);
                holder.buttonEnroll.setAlpha(1f);
                holder.buttonEnroll.setText(R.string.enroll);
                holder.buttonEnroll.setBackgroundTintList(
                        androidx.core.content.ContextCompat.getColorStateList(context, R.color.unify_blue)
                );
                holder.textEnrollWindow.setVisibility(View.GONE);
                break;

            case NOT_STARTED:
                holder.buttonEnroll.setEnabled(false);
                holder.buttonEnroll.setAlpha(0.55f);
                holder.buttonEnroll.setText(R.string.enrollment_not_open_yet);
                holder.buttonEnroll.setBackgroundTintList(
                        androidx.core.content.ContextCompat.getColorStateList(context, R.color.unify_warning)
                );
                // Show "Opens: dd/MM" hint under the button
                holder.textEnrollWindow.setText(
                        context.getString(R.string.enrollment_opens_on, shortDate(course.getEnrollmentStartAt()))
                );
                holder.textEnrollWindow.setVisibility(View.VISIBLE);
                break;

            case CLOSED:
                holder.buttonEnroll.setEnabled(false);
                holder.buttonEnroll.setAlpha(0.45f);
                holder.buttonEnroll.setText(R.string.status_enrollment_closed);
                holder.buttonEnroll.setBackgroundTintList(
                        androidx.core.content.ContextCompat.getColorStateList(context, R.color.unify_text_secondary)
                );
                holder.textEnrollWindow.setVisibility(View.GONE);
                break;

            case UNKNOWN:
            default:
                // Dates missing from server — keep button active; server will reject if needed
                holder.buttonEnroll.setEnabled(true);
                holder.buttonEnroll.setAlpha(1f);
                holder.buttonEnroll.setText(R.string.enroll);
                holder.buttonEnroll.setBackgroundTintList(
                        androidx.core.content.ContextCompat.getColorStateList(context, R.color.unify_blue)
                );
                holder.textEnrollWindow.setVisibility(View.GONE);
                break;
        }
    }


    /** "2025-09-15 00:00:00"  →  "15/09" */
    private String shortDate(String dateTime) {
        if (empty(dateTime) || dateTime.length() < 10) return "";
        return dateTime.substring(8, 10) + "/" + dateTime.substring(5, 7);
    }

    private boolean empty(String s) {
        return s == null || s.trim().isEmpty() || s.equalsIgnoreCase("null");
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
        if (empty(value)) return context.getString(R.string.not_available_short);
        return value.trim();
    }

    static class AvailableCourseViewHolder extends RecyclerView.ViewHolder {

        TextView      textCourseInitial;
        TextView      textCourseTitle;
        TextView      textCourseCode;
        TextView      textCredits;
        TextView      textSchedule;
        TextView      textEnrollWindow;
        MaterialButton buttonEnroll;

        public AvailableCourseViewHolder(@NonNull View itemView) {
            super(itemView);
            textCourseInitial = itemView.findViewById(R.id.textCourseInitial);
            textCourseTitle   = itemView.findViewById(R.id.textCourseTitle);
            textCourseCode    = itemView.findViewById(R.id.textCourseCode);
            textCredits       = itemView.findViewById(R.id.textCredits);
            textSchedule      = itemView.findViewById(R.id.textSchedule);
            textEnrollWindow  = itemView.findViewById(R.id.textEnrollWindow);
            buttonEnroll      = itemView.findViewById(R.id.buttonEnroll);
        }
    }
}