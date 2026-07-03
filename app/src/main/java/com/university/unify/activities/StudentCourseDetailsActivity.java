package com.university.unify.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.adapter.CourseAnnouncementAdapter;
import com.university.unify.adapter.StudentParticipantAdapter;
import com.university.unify.constants.ChatConstants;
import com.university.unify.model.CourseAnnouncementModel;
import com.university.unify.model.StudentParticipantModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StudentCourseDetailsActivity extends AppCompatActivity {

    private ImageButton buttonBack;
    private TextView cardMaterials;
    private TextView cardCourseChat;
    private TextView cardChatInstructor;

    private TextView textHeaderTitle;
    private TextView textCourseInitial;
    private TextView textCourseTitle;
    private TextView textCourseMeta;

    private TextView tabCourse;
    private TextView tabParticipants;
    private TextView tabGrades;

    private View tabUnderlineCourse;
    private View tabUnderlineParticipants;
    private View tabUnderlineGrades;

    private View cardCourseInfo;
    private View layoutParticipantsContent;
    private View layoutGradesContent;

    private TextView textInstructor;
    private TextView textDepartment;
    private TextView textSection;
    private TextView textDescription;

    private RecyclerView recyclerParticipants;
    private ProgressBar progressParticipants;
    private TextView textParticipantsEmpty;
    private TextInputEditText editParticipantSearch;

    private ProgressBar progressGrade;
    private TextView textGradeValue;
    private TextView textGradeStatus;
    private TextView textGradeCourseCode;
    private TextView textGradeSemester;
    private TextView textGradeAcademicYear;

    private RecyclerView recyclerCourseAnnouncements;
    private ProgressBar progressCourseAnnouncements;
    private TextView textCourseAnnouncementsEmpty;

    private RequestQueue queue;
    private StudentParticipantAdapter participantAdapter;
    private CourseAnnouncementAdapter courseAnnouncementAdapter;

    private final List<StudentParticipantModel> allParticipants = new ArrayList<>();
    private final List<StudentParticipantModel> visibleParticipants = new ArrayList<>();
    private final List<CourseAnnouncementModel> courseAnnouncements = new ArrayList<>();

    private boolean participantsLoaded = false;
    private boolean gradeLoaded = false;
    private boolean courseAnnouncementsLoaded = false;

    private String courseId = "";
    private String title = "";
    private String code = "";
    private String description = "";
    private String section = "";
    private String department = "";
    private String semester = "";
    private String academicYear = "";

    private String studentId = "";
    private String instructorId = "";
    private String instructorName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_course_details);

        queue = Volley.newRequestQueue(this);

        loadStudentFromSession();
        readIntentData();

        initViews();
        setupParticipantsRecycler();
        setupParticipantsSearch();
        setupCourseAnnouncementsRecycler();
        setupListeners();

        bindCourseData();
        selectTab("COURSE");
    }

    private void loadStudentFromSession() {
        DatabaseHelper db = new DatabaseHelper(this);
        studentId = safe(db.getLoggedInUserId());
    }

    private void readIntentData() {
        courseId = safe(getIntent().getStringExtra("course_id"));
        title = safe(getIntent().getStringExtra("title"));
        code = safe(getIntent().getStringExtra("code"));
        description = safe(getIntent().getStringExtra("description"));
        section = safe(getIntent().getStringExtra("section"));
        department = safe(getIntent().getStringExtra("department"));
        semester = safe(getIntent().getStringExtra("semester"));
        academicYear = safe(getIntent().getStringExtra("academic_year"));

        instructorId = safe(getIntent().getStringExtra("instructor_id"));
        instructorName = safe(getIntent().getStringExtra("instructor_name"));
    }

    private void initViews() {
        buttonBack = findViewById(R.id.buttonBack);

        cardMaterials = findViewById(R.id.cardMaterials);
        cardChatInstructor = findViewById(R.id.cardChatInstructor);
        cardCourseChat = findViewById(R.id.cardCourseChat);

        textHeaderTitle = findViewById(R.id.textHeaderTitle);
        textCourseInitial = findViewById(R.id.textCourseInitial);
        textCourseTitle = findViewById(R.id.textCourseTitle);
        textCourseMeta = findViewById(R.id.textCourseMeta);

        tabCourse = findViewById(R.id.tabCourse);
        tabParticipants = findViewById(R.id.tabParticipants);
        tabGrades = findViewById(R.id.tabGrades);

        tabUnderlineCourse = findViewById(R.id.tabUnderlineCourse);
        tabUnderlineParticipants = findViewById(R.id.tabUnderlineParticipants);
        tabUnderlineGrades = findViewById(R.id.tabUnderlineGrades);

        cardCourseInfo = findViewById(R.id.cardCourseInfo);
        layoutParticipantsContent = findViewById(R.id.layoutParticipantsContent);
        layoutGradesContent = findViewById(R.id.layoutGradesContent);

        textInstructor = findViewById(R.id.textInstructor);
        textDepartment = findViewById(R.id.textDepartment);
        textSection = findViewById(R.id.textSection);
        textDescription = findViewById(R.id.textDescription);

        recyclerParticipants = findViewById(R.id.recyclerParticipants);
        progressParticipants = findViewById(R.id.progressParticipants);
        textParticipantsEmpty = findViewById(R.id.textParticipantsEmpty);
        editParticipantSearch = findViewById(R.id.editParticipantSearch);

        progressGrade = findViewById(R.id.progressGrade);
        textGradeValue = findViewById(R.id.textGradeValue);
        textGradeStatus = findViewById(R.id.textGradeStatus);
        textGradeCourseCode = findViewById(R.id.textGradeCourseCode);
        textGradeSemester = findViewById(R.id.textGradeSemester);
        textGradeAcademicYear = findViewById(R.id.textGradeAcademicYear);

        recyclerCourseAnnouncements = findViewById(R.id.recyclerCourseAnnouncements);
        progressCourseAnnouncements = findViewById(R.id.progressCourseAnnouncements);
        textCourseAnnouncementsEmpty = findViewById(R.id.textCourseAnnouncementsEmpty);
    }

    private void setupListeners() {
        buttonBack.setOnClickListener(v -> finish());

        tabCourse.setOnClickListener(v -> selectTab("COURSE"));
        tabParticipants.setOnClickListener(v -> selectTab("PARTICIPANTS"));
        tabGrades.setOnClickListener(v -> selectTab("GRADES"));

        cardMaterials.setOnClickListener(v -> openCourseMaterials());
        cardCourseChat.setOnClickListener(v -> openCourseGroupChat());
        cardChatInstructor.setOnClickListener(v -> openPrivateChatWithInstructor());
    }

    private void openCourseGroupChat() {
        if (TextUtils.isEmpty(studentId)) {
            Toast.makeText(this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(courseId)) {
            Toast.makeText(this, getString(R.string.course_id_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        String chatId = ChatConstants.courseGroupChatId(courseId);

        String chatTitle = title;
        if (TextUtils.isEmpty(chatTitle)) {
            chatTitle = getString(R.string.course_group_chat);
        } else {
            chatTitle = chatTitle + " - " + getString(R.string.course_group_chat);
        }

        Intent intent = new Intent(this, SingleChatActivity.class);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("chat_type", ChatConstants.TYPE_COURSE_GROUP);
        intent.putExtra("course_id", courseId);
        intent.putExtra("chat_title", chatTitle);

        startActivity(intent);
    }
    private void openCourseMaterials() {
        if (TextUtils.isEmpty(courseId)) {
            Toast.makeText(this, getString(R.string.course_id_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, StudentCourseMaterialsActivity.class);
        intent.putExtra("course_id", courseId);
        intent.putExtra("course_title", title);
        intent.putExtra("course_code", code);
        startActivity(intent);
    }

    private void openPrivateChatWithInstructor() {
        if (TextUtils.isEmpty(studentId)) {
            Toast.makeText(this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(instructorId)) {
            Toast.makeText(this, getString(R.string.instructor_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(instructorName)) {
            instructorName = getString(R.string.instructor);
        }

        String chatId = ChatConstants.privateChatId(studentId, instructorId);

        Intent intent = new Intent(this, SingleChatActivity.class);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("chat_type", ChatConstants.TYPE_PRIVATE);
        intent.putExtra("other_user_id", instructorId);
        intent.putExtra("other_user_name", instructorName);
        intent.putExtra("chat_title", instructorName);

        startActivity(intent);
    }

    private void bindCourseData() {
        String cleanTitle = display(title);

        textHeaderTitle.setText(cleanTitle);
        textCourseTitle.setText(cleanTitle);
        textCourseInitial.setText(getInitial(cleanTitle));

        textCourseMeta.setText(display(semester));

        textInstructor.setText(getString(
                R.string.teacher_value,
                display(instructorName)
        ));

        textDepartment.setText(getString(
                R.string.department_value,
                display(department)
        ));

        textSection.setText(getString(
                R.string.section_value,
                display(section)
        ));

        textDescription.setText(displayLong(description));
    }

    private void setupCourseAnnouncementsRecycler() {
        courseAnnouncementAdapter = new CourseAnnouncementAdapter(this, courseAnnouncements);

        recyclerCourseAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        recyclerCourseAnnouncements.setAdapter(courseAnnouncementAdapter);
        recyclerCourseAnnouncements.setNestedScrollingEnabled(false);
    }

    private void loadCourseAnnouncements() {
        if (TextUtils.isEmpty(courseId)) {
            showCourseAnnouncementsEmpty(getString(R.string.course_id_not_found));
            return;
        }

        progressCourseAnnouncements.setVisibility(View.VISIBLE);
        recyclerCourseAnnouncements.setVisibility(View.GONE);
        textCourseAnnouncementsEmpty.setVisibility(View.GONE);

        String url = ApiConfig.GET_ANNOUNCEMENTS + "?course_id=" + courseId.trim();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressCourseAnnouncements.setVisibility(View.GONE);

                    try {
                        boolean success = response.optBoolean("success", false);
                        String message = response.optString(
                                "message",
                                getString(R.string.failed_to_load_course_announcements)
                        );

                        courseAnnouncements.clear();

                        if (!success) {
                            courseAnnouncementAdapter.notifyDataSetChanged();
                            showCourseAnnouncementsEmpty(message);
                            courseAnnouncementsLoaded = true;
                            return;
                        }

                        JSONArray data = response.optJSONArray("data");

                        if (data == null || data.length() == 0) {
                            courseAnnouncementAdapter.notifyDataSetChanged();
                            showCourseAnnouncementsEmpty(getString(R.string.no_course_announcements_found));
                            courseAnnouncementsLoaded = true;
                            return;
                        }

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.optJSONObject(i);

                            if (obj == null) {
                                continue;
                            }

                            CourseAnnouncementModel item = new CourseAnnouncementModel();

                            item.setAnnouncementId(obj.optString("announcement_id"));
                            item.setTitle(obj.optString("title"));
                            item.setBody(obj.optString("body"));
                            item.setCreatedAt(obj.optString("created_at"));
                            item.setIsPinned(obj.optString("is_pinned"));
                            item.setCreatedByName(obj.optString("created_by_name"));

                            courseAnnouncements.add(item);
                        }

                        courseAnnouncementAdapter.notifyDataSetChanged();
                        courseAnnouncementsLoaded = true;
                        updateCourseAnnouncementsEmptyState();

                    } catch (Exception e) {
                        showCourseAnnouncementsEmpty(getString(R.string.course_announcements_parse_error));
                    }
                },
                error -> {
                    progressCourseAnnouncements.setVisibility(View.GONE);
                    Toast.makeText(
                            this,
                            getString(R.string.failed_to_load_course_announcements),
                            Toast.LENGTH_SHORT
                    ).show();

                    showCourseAnnouncementsEmpty(getString(R.string.failed_to_load_course_announcements));
                }
        );

        queue.add(request);
    }

    private void showCourseAnnouncementsEmpty(String message) {
        recyclerCourseAnnouncements.setVisibility(View.GONE);
        textCourseAnnouncementsEmpty.setVisibility(View.VISIBLE);
        textCourseAnnouncementsEmpty.setText(message);
    }

    private void updateCourseAnnouncementsEmptyState() {
        if (courseAnnouncements.isEmpty()) {
            showCourseAnnouncementsEmpty(getString(R.string.no_course_announcements_found));
        } else {
            recyclerCourseAnnouncements.setVisibility(View.VISIBLE);
            textCourseAnnouncementsEmpty.setVisibility(View.GONE);
        }
    }

    private void setupParticipantsRecycler() {
        participantAdapter = new StudentParticipantAdapter(
                this,
                visibleParticipants,
                new StudentParticipantAdapter.OnParticipantActionListener() {
                    @Override
                    public void onChatClicked(StudentParticipantModel participant) {
                        openPrivateChatWithParticipant(participant);
                    }
                }
        );

        recyclerParticipants.setLayoutManager(new LinearLayoutManager(this));
        recyclerParticipants.setAdapter(participantAdapter);
        recyclerParticipants.setNestedScrollingEnabled(false);
    }

    private void openPrivateChatWithParticipant(StudentParticipantModel participant) {
        if (participant == null) {
            Toast.makeText(this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        String otherStudentId = safe(participant.getUserId());
        String otherStudentName = safe(participant.getFullName());

        if (TextUtils.isEmpty(studentId)) {
            Toast.makeText(this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(otherStudentId)) {
            Toast.makeText(this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        if (studentId.equals(otherStudentId)) {
            Toast.makeText(this, getString(R.string.cannot_chat_with_yourself), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(otherStudentName)) {
            otherStudentName = getString(R.string.unknown_student);
        }

        String chatId = ChatConstants.privateChatId(studentId, otherStudentId);

        Intent intent = new Intent(this, SingleChatActivity.class);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("chat_type", ChatConstants.TYPE_PRIVATE);
        intent.putExtra("other_user_id", otherStudentId);
        intent.putExtra("other_user_name", otherStudentName);
        intent.putExtra("chat_title", otherStudentName);

        startActivity(intent);
    }

    private void setupParticipantsSearch() {
        editParticipantSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterParticipants(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed.
            }
        });
    }

    private void selectTab(String selected) {
        boolean courseSelected = selected.equals("COURSE");
        boolean participantsSelected = selected.equals("PARTICIPANTS");
        boolean gradesSelected = selected.equals("GRADES");

        setTabState(tabCourse, courseSelected);
        setTabState(tabParticipants, participantsSelected);
        setTabState(tabGrades, gradesSelected);

        setUnderlineState(courseSelected, participantsSelected, gradesSelected);

        cardCourseInfo.setVisibility(courseSelected ? View.VISIBLE : View.GONE);
        layoutParticipantsContent.setVisibility(participantsSelected ? View.VISIBLE : View.GONE);
        layoutGradesContent.setVisibility(gradesSelected ? View.VISIBLE : View.GONE);

        if (courseSelected && !courseAnnouncementsLoaded) {
            loadCourseAnnouncements();
        }

        if (participantsSelected && !participantsLoaded) {
            loadParticipants();
        }

        if (gradesSelected && !gradeLoaded) {
            loadGrade();
        }
    }

    private void loadGrade() {
        if (TextUtils.isEmpty(studentId) || TextUtils.isEmpty(courseId)) {
            showGradeError(getString(R.string.failed_to_load_grade));
            return;
        }

        progressGrade.setVisibility(View.VISIBLE);
        textGradeValue.setText(getString(R.string.loading_grade));

        String url = ApiConfig.GET_STUDENT_COURSE_GRADE
                + "?student_id=" + studentId.trim()
                + "&course_id=" + courseId.trim();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressGrade.setVisibility(View.GONE);

                    try {
                        boolean success = response.optBoolean("success", false);

                        if (!success) {
                            showGradeError(getString(R.string.grade_not_available));
                            gradeLoaded = true;
                            return;
                        }

                        JSONObject data = response.optJSONObject("data");

                        if (data == null) {
                            showGradeError(getString(R.string.grade_not_available));
                            gradeLoaded = true;
                            return;
                        }

                        String grade = display(data.optString("final_grade"));
                        String status = display(data.optString("result"));
                        String courseCode = display(data.optString("course_code"));

                        String semesterValue = display(data.optString("enrollment_semester"));
                        if (semesterValue.equals(getString(R.string.not_available_short))) {
                            semesterValue = display(data.optString("course_semester"));
                        }

                        String academicYearValue = display(data.optString("enrollment_academic_year"));
                        if (academicYearValue.equals(getString(R.string.not_available_short))) {
                            academicYearValue = display(data.optString("course_academic_year"));
                        }

                        if (grade.equals(getString(R.string.not_available_short))) {
                            textGradeValue.setText(getString(R.string.grade_not_available));
                        } else {
                            textGradeValue.setText(grade);
                        }

                        textGradeStatus.setText(getString(R.string.grade_status_value, status));
                        textGradeCourseCode.setText(getString(R.string.course_code_value, courseCode));
                        textGradeSemester.setText(getString(R.string.semester_value, semesterValue));
                        textGradeAcademicYear.setText(getString(R.string.academic_year_value, academicYearValue));

                        gradeLoaded = true;

                    } catch (Exception e) {
                        showGradeError(getString(R.string.failed_to_load_grade));
                    }
                },
                error -> {
                    progressGrade.setVisibility(View.GONE);
                    Toast.makeText(this, getString(R.string.failed_to_load_grade), Toast.LENGTH_SHORT).show();
                    showGradeError(getString(R.string.failed_to_load_grade));
                }
        );

        queue.add(request);
    }

    private void showGradeError(String message) {
        progressGrade.setVisibility(View.GONE);
        textGradeValue.setText(message);
        textGradeStatus.setText(getString(R.string.not_available_short));
        textGradeCourseCode.setText(getString(R.string.not_available_short));
        textGradeSemester.setText(getString(R.string.not_available_short));
        textGradeAcademicYear.setText(getString(R.string.not_available_short));
    }

    private void loadParticipants() {
        if (TextUtils.isEmpty(courseId)) {
            showParticipantsEmpty(getString(R.string.course_id_not_found));
            return;
        }

        progressParticipants.setVisibility(View.VISIBLE);
        recyclerParticipants.setVisibility(View.GONE);
        textParticipantsEmpty.setVisibility(View.GONE);

        String url = ApiConfig.GET_COURSE_PARTICIPANTS + "?course_id=" + courseId.trim();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressParticipants.setVisibility(View.GONE);

                    try {
                        boolean success = response.optBoolean("success", false);
                        String message = response.optString(
                                "message",
                                getString(R.string.failed_to_load_participants)
                        );

                        allParticipants.clear();
                        visibleParticipants.clear();

                        if (!success) {
                            participantAdapter.notifyDataSetChanged();
                            showParticipantsEmpty(message);
                            return;
                        }

                        JSONArray data = response.optJSONArray("data");

                        if (data == null || data.length() == 0) {
                            participantAdapter.notifyDataSetChanged();
                            showParticipantsEmpty(getString(R.string.no_participants_found));
                            participantsLoaded = true;
                            return;
                        }

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.optJSONObject(i);

                            if (obj == null) {
                                continue;
                            }

                            StudentParticipantModel participant = new StudentParticipantModel();

                            participant.setUserId(obj.optString("user_id"));
                            participant.setFullName(obj.optString("full_name"));
                            participant.setEmail(obj.optString("email"));
                            participant.setStudentNumber(obj.optString("student_number"));
                            participant.setPhoneNumber(obj.optString("phone_number"));
                            participant.setStudyYear(obj.optString("study_year"));
                            participant.setProfileImageUrl(obj.optString("profile_image_url"));
                            participant.setFacultyName(obj.optString("faculty_name"));
                            participant.setMajorName(obj.optString("major_name"));

                            allParticipants.add(participant);
                        }

                        visibleParticipants.addAll(allParticipants);
                        participantAdapter.notifyDataSetChanged();

                        participantsLoaded = true;
                        updateParticipantsEmptyState();

                    } catch (Exception e) {
                        showParticipantsEmpty(getString(R.string.participants_parse_error));
                    }
                },
                error -> {
                    progressParticipants.setVisibility(View.GONE);
                    Toast.makeText(
                            this,
                            getString(R.string.failed_to_load_participants),
                            Toast.LENGTH_SHORT
                    ).show();

                    showParticipantsEmpty(getString(R.string.failed_to_load_participants));
                }
        );

        queue.add(request);
    }

    private void filterParticipants(String query) {
        String cleanQuery = query == null ? "" : query.trim().toLowerCase();

        visibleParticipants.clear();

        if (cleanQuery.isEmpty()) {
            visibleParticipants.addAll(allParticipants);
        } else {
            for (StudentParticipantModel participant : allParticipants) {
                String name = safe(participant.getFullName()).toLowerCase();
                String email = safe(participant.getEmail()).toLowerCase();
                String major = safe(participant.getMajorName()).toLowerCase();
                String studentNumber = safe(participant.getStudentNumber()).toLowerCase();

                if (name.contains(cleanQuery)
                        || email.contains(cleanQuery)
                        || major.contains(cleanQuery)
                        || studentNumber.contains(cleanQuery)) {
                    visibleParticipants.add(participant);
                }
            }
        }

        participantAdapter.notifyDataSetChanged();
        updateParticipantsEmptyState();
    }

    private void updateParticipantsEmptyState() {
        if (visibleParticipants.isEmpty()) {
            recyclerParticipants.setVisibility(View.GONE);
            textParticipantsEmpty.setVisibility(View.VISIBLE);
            textParticipantsEmpty.setText(getString(R.string.no_participants_found));
        } else {
            recyclerParticipants.setVisibility(View.VISIBLE);
            textParticipantsEmpty.setVisibility(View.GONE);
        }
    }

    private void showParticipantsEmpty(String message) {
        recyclerParticipants.setVisibility(View.GONE);
        textParticipantsEmpty.setVisibility(View.VISIBLE);
        textParticipantsEmpty.setText(message);
    }

    private void setTabState(TextView tab, boolean active) {
        tab.setTextColor(ContextCompat.getColor(
                this,
                active ? R.color.unify_blue : R.color.unify_text_secondary
        ));

        tab.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void setUnderlineState(boolean courseActive, boolean participantsActive, boolean gradesActive) {
        tabUnderlineCourse.setBackgroundColor(ContextCompat.getColor(
                this,
                courseActive ? R.color.unify_gold : android.R.color.transparent
        ));

        tabUnderlineParticipants.setBackgroundColor(ContextCompat.getColor(
                this,
                participantsActive ? R.color.unify_gold : android.R.color.transparent
        ));

        tabUnderlineGrades.setBackgroundColor(ContextCompat.getColor(
                this,
                gradesActive ? R.color.unify_gold : android.R.color.transparent
        ));
    }

    private String getInitial(String value) {
        if (value == null
                || value.trim().isEmpty()
                || value.equals(getString(R.string.not_available_short))) {
            return getString(R.string.course_default_initial);
        }

        return value.trim().substring(0, 1).toUpperCase();
    }

    private String display(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return getString(R.string.not_available_short);
        }

        return value.trim();
    }

    private String displayLong(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return getString(R.string.no_description_available);
        }

        return value.trim();
    }

    private String safe(String value) {
        if (value == null || value.equalsIgnoreCase("null")) {
            return "";
        }

        return value.trim();
    }
}