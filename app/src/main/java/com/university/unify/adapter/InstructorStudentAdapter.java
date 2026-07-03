package com.university.unify.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.university.unify.R;
import com.university.unify.model.InstructorStudentModel;

import java.util.List;

public class InstructorStudentAdapter extends RecyclerView.Adapter<InstructorStudentAdapter.StudentViewHolder> {

    public interface OnStudentActionListener {
        void onSetGradeClicked(InstructorStudentModel student);
        void onChatClicked(InstructorStudentModel student);
    }

    private final Context context;
    private final List<InstructorStudentModel> students;
    private final OnStudentActionListener listener;

    public InstructorStudentAdapter(Context context,
                                    List<InstructorStudentModel> students,
                                    OnStudentActionListener listener) {
        this.context = context;
        this.students = students;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_instructor_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        final InstructorStudentModel student = students.get(position);

        String name = safe(student.getFullName());
        String email = safe(student.getEmail());
        String studentNumber = safe(student.getStudentNumber());
        String studyYear = safe(student.getStudyYear());
        String finalGrade = safe(student.getFinalGrade());
        String result = safe(student.getResult());

        holder.textStudentName.setText(name.isEmpty()
                ? context.getString(R.string.unknown_student)
                : name);

        holder.textStudentEmail.setText(email.isEmpty() ? "-" : email);

        String avatar = "S";
        if (!name.isEmpty()) {
            avatar = name.substring(0, 1).toUpperCase();
        }

        holder.textStudentAvatar.setText(avatar);

        String meta = "";

        if (!studentNumber.isEmpty()) {
            meta = context.getString(R.string.student_number_value, studentNumber);
        }

        if (!studyYear.isEmpty()) {
            if (!meta.isEmpty()) {
                meta += " • ";
            }

            meta += context.getString(R.string.study_year_value, studyYear);
        }

        if (meta.isEmpty()) {
            meta = "-";
        }

        holder.textStudentMeta.setText(meta);

        if (TextUtils.isEmpty(finalGrade)) {
            holder.textStudentGrade.setText(context.getString(R.string.grade_not_set));
        } else {
            String resultText = TextUtils.isEmpty(result) ? "" : " • " + result;
            holder.textStudentGrade.setText(
                    context.getString(R.string.grade_value, finalGrade) + resultText
            );
        }

        holder.buttonSetGrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();

                if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onSetGradeClicked(student);
                }
            }
        });

        holder.buttonChatStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();

                if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onChatClicked(student);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return students == null ? 0 : students.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {

        TextView textStudentAvatar;
        TextView textStudentName;
        TextView textStudentEmail;
        TextView textStudentMeta;
        TextView textStudentGrade;
        MaterialButton buttonSetGrade;
        MaterialButton buttonChatStudent;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);

            textStudentAvatar = itemView.findViewById(R.id.textStudentAvatar);
            textStudentName = itemView.findViewById(R.id.textStudentName);
            textStudentEmail = itemView.findViewById(R.id.textStudentEmail);
            textStudentMeta = itemView.findViewById(R.id.textStudentMeta);
            textStudentGrade = itemView.findViewById(R.id.textStudentGrade);
            buttonSetGrade = itemView.findViewById(R.id.buttonSetGrade);
            buttonChatStudent = itemView.findViewById(R.id.buttonChatStudent);
        }
    }

    private String safe(String value) {
        return value == null || value.equalsIgnoreCase("null") ? "" : value.trim();
    }
}