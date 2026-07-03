package com.university.unify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.university.unify.R;
import com.university.unify.constants.ChatConstants;

public class InstructorCourseDetailsActivity extends AppCompatActivity {

    private TextView textTitle;

    private TextView cardCourseChat;
    private TextView textCode;
    private TextView cardManageStudents;
    private TextView cardAnnouncements;
    private TextView cardMaterials;

    private String courseId = "";
    private String courseTitle = "";
    private String courseCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_course_details);

        textTitle = findViewById(R.id.textCourseDetailsTitle);
        textCode = findViewById(R.id.textCourseDetailsCode);
        cardManageStudents = findViewById(R.id.cardManageStudents);
        cardAnnouncements = findViewById(R.id.cardAnnouncements);
        cardMaterials = findViewById(R.id.cardMaterials);

        cardCourseChat = findViewById(R.id.cardCourseChat);

        courseId = getIntent().getStringExtra("course_id");
        courseTitle = getIntent().getStringExtra("course_title");
        courseCode = getIntent().getStringExtra("course_code");

        if (courseId == null) courseId = "";
        if (courseTitle == null) courseTitle = "";
        if (courseCode == null) courseCode = "";

        textTitle.setText(courseTitle.isEmpty() ? "-" : courseTitle);
        textCode.setText(courseCode.isEmpty() ? "-" : courseCode);

        cardCourseChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        InstructorCourseDetailsActivity.this,
                        SingleChatActivity.class
                );

                String chatId = ChatConstants.courseGroupChatId(courseId);

                intent.putExtra("chat_id", chatId);
                intent.putExtra("chat_type", ChatConstants.TYPE_COURSE_GROUP);
                intent.putExtra("course_id", courseId);
                intent.putExtra("chat_title", courseTitle.isEmpty()
                        ? getString(R.string.course_chat)
                        : courseTitle + " - " + getString(R.string.course_chat));

                startActivity(intent);
            }
        });

        cardManageStudents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        InstructorCourseDetailsActivity.this,
                        InstructorCourseStudentsActivity.class
                );

                intent.putExtra("course_id", courseId);
                intent.putExtra("course_title", courseTitle);
                intent.putExtra("course_code", courseCode);

                startActivity(intent);
            }
        });

        cardAnnouncements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        InstructorCourseDetailsActivity.this,
                        InstructorAnnouncementsActivity.class
                );

                intent.putExtra("course_id", courseId);
                intent.putExtra("course_title", courseTitle);
                intent.putExtra("course_code", courseCode);

                startActivity(intent);
            }
        });

        cardMaterials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        InstructorCourseDetailsActivity.this,
                        InstructorCourseMaterialsActivity.class
                );

                intent.putExtra("course_id", courseId);
                intent.putExtra("course_title", courseTitle);
                intent.putExtra("course_code", courseCode);

                startActivity(intent);
            }
        });
    }
}