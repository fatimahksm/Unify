package com.university.unify.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.university.unify.R;
import com.university.unify.adapter.CourseInstructorAdapter;
import com.university.unify.model.CourseInstructorModel;
import com.university.unify.model.UserModel;
import com.university.unify.network.ApiConfig;
import com.university.unify.utils.AcademicPeriodDefaults;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CourseDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE_ID = "extra_course_id";
    public static final String EXTRA_COURSE_CODE = "extra_course_code";
    public static final String EXTRA_COURSE_TITLE = "extra_course_title";
    public static final String EXTRA_COURSE_DESCRIPTION = "extra_course_description";
    public static final String EXTRA_SECTION = "extra_section";
    public static final String EXTRA_DEPARTMENT = "extra_department";
    public static final String EXTRA_FACULTY_NAME = "extra_faculty_name";
    public static final String EXTRA_INSTRUCTOR_ID = "extra_instructor_id";
    public static final String EXTRA_STUDY_YEAR = "extra_study_year";
    public static final String EXTRA_SEMESTER = "extra_semester";
    public static final String EXTRA_ACADEMIC_YEAR = "extra_academic_year";
    public static final String EXTRA_MAJOR_NAME = "extra_major_name";
    public static final String EXTRA_INSTRUCTOR_NAME = "extra_instructor_name";
    public static final String EXTRA_CREDITS = "extra_credits";
    public static final String EXTRA_SCHEDULE = "extra_schedule";
    public static final String EXTRA_FACULTY_ID = "extra_faculty_id";

    public static final String EXTRA_ENROLLMENT_START = "extra_enrollment_start";
    public static final String EXTRA_ENROLLMENT_END = "extra_enrollment_end";
    public static final String EXTRA_COURSE_START = "extra_course_start";
    public static final String EXTRA_COURSE_END = "extra_course_end";

    private ImageButton buttonBack;
    private Button buttonAssignInstructor;
    private Button buttonEditCourse;
    private Button buttonEditSchedule;
    private Button buttonAddSchedule;

    private TextView textCode;
    private TextView textTitle;
    private TextView textDescription;
    private TextView textSection;
    private TextView textDepartment;
    private TextView textMajor;
    private TextView textFaculty;
    private TextView textStudyYear;
    private TextView textSemester;
    private TextView textAcademicYear;
    private TextView textCourseId;
    private TextView textSchedule;
    private TextView textCredits;
    private TextView textEmptyCourseInstructors;

    private RecyclerView recyclerCourseInstructors;
    private CourseInstructorAdapter courseInstructorAdapter;

    private final List<CourseInstructorModel> courseInstructorList = new ArrayList<CourseInstructorModel>();
    private final List<UserModel> allFacultyInstructors = new ArrayList<UserModel>();
    private final List<String> instructorNames = new ArrayList<String>();
    private final List<String> instructorIds = new ArrayList<String>();

    private String courseId = "";
    private String facultyId = "";
    private String facultyName = "";

    private String currentCode = "";
    private String currentTitle = "";
    private String currentDescription = "";
    private String currentSection = "";
    private String currentDepartment = "";
    private String currentMajorName = "";
    private String currentStudyYear = "";
    private String currentSemester = "";
    private String currentAcademicYear = "";
    private String currentCredits = "";
    private String currentScheduleText = "";

    private String currentEnrollmentStart = "";
    private String currentEnrollmentEnd = "";
    private String currentCourseStart = "";
    private String currentCourseEnd = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        initViews();
        applyInsets();
        setupRecycler();
        bindData();
        setupListeners();

        loadCourseInstructors();
    }

    private void initViews() {
        buttonBack = findViewById(R.id.buttonBack);
        buttonAssignInstructor = findViewById(R.id.buttonAssignInstructor);
        buttonEditCourse = findViewById(R.id.buttonEditCourse);
        buttonEditSchedule = findViewById(R.id.buttonEditSchedule);
        buttonAddSchedule = findViewById(R.id.buttonAddSchedule);

        textCode = findViewById(R.id.textDetailsCourseCode);
        textTitle = findViewById(R.id.textDetailsCourseTitle);
        textDescription = findViewById(R.id.textDetailsCourseDescription);
        textSection = findViewById(R.id.textDetailsSection);
        textDepartment = findViewById(R.id.textDetailsDepartment);
        textMajor = findViewById(R.id.textDetailsMajor);
        textFaculty = findViewById(R.id.textDetailsFaculty);
        textStudyYear = findViewById(R.id.textDetailsStudyYear);
        textSemester = findViewById(R.id.textDetailsSemester);
        textAcademicYear = findViewById(R.id.textDetailsAcademicYear);
        textCourseId = findViewById(R.id.textDetailsCourseId);
        textSchedule = findViewById(R.id.textDetailsSchedule);
        textCredits = findViewById(R.id.textDetailsCredits);
        textEmptyCourseInstructors = findViewById(R.id.textEmptyCourseInstructors);
        recyclerCourseInstructors = findViewById(R.id.recyclerCourseInstructors);
    }

    private void applyInsets() {
        View root = findViewById(R.id.rootCourseDetails);

        ViewCompat.setOnApplyWindowInsetsListener(root, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
                int top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;

                view.setPadding(
                        view.getPaddingLeft(),
                        top,
                        view.getPaddingRight(),
                        view.getPaddingBottom()
                );

                return insets;
            }
        });
    }

    private void setupRecycler() {
        courseInstructorAdapter = new CourseInstructorAdapter(
                this,
                courseInstructorList,
                new CourseInstructorAdapter.OnInstructorActionListener() {
                    @Override
                    public void onRemoveClicked(int position, CourseInstructorModel instructor) {
                        confirmRemoveInstructor(position, instructor);
                    }
                }
        );

        recyclerCourseInstructors.setLayoutManager(new LinearLayoutManager(this));
        recyclerCourseInstructors.setAdapter(courseInstructorAdapter);
        recyclerCourseInstructors.setNestedScrollingEnabled(false);
    }

    private void bindData() {
        courseId = safe(getIntent().getStringExtra(EXTRA_COURSE_ID));
        facultyId = safe(getIntent().getStringExtra(EXTRA_FACULTY_ID));

        currentCode = display(getIntent().getStringExtra(EXTRA_COURSE_CODE));
        currentTitle = display(getIntent().getStringExtra(EXTRA_COURSE_TITLE));
        currentDescription = display(getIntent().getStringExtra(EXTRA_COURSE_DESCRIPTION));
        currentSection = display(getIntent().getStringExtra(EXTRA_SECTION));
        currentDepartment = display(getIntent().getStringExtra(EXTRA_DEPARTMENT));
        facultyName = display(getIntent().getStringExtra(EXTRA_FACULTY_NAME));
        currentMajorName = display(getIntent().getStringExtra(EXTRA_MAJOR_NAME));
        currentStudyYear = display(getIntent().getStringExtra(EXTRA_STUDY_YEAR));
        currentSemester = display(getIntent().getStringExtra(EXTRA_SEMESTER));
        currentAcademicYear = display(getIntent().getStringExtra(EXTRA_ACADEMIC_YEAR));
        currentCredits = display(getIntent().getStringExtra(EXTRA_CREDITS));
        currentScheduleText = display(getIntent().getStringExtra(EXTRA_SCHEDULE));

        currentEnrollmentStart = cleanDisplay(getIntent().getStringExtra(EXTRA_ENROLLMENT_START));
        currentEnrollmentEnd = cleanDisplay(getIntent().getStringExtra(EXTRA_ENROLLMENT_END));
        currentCourseStart = cleanDisplay(getIntent().getStringExtra(EXTRA_COURSE_START));
        currentCourseEnd = cleanDisplay(getIntent().getStringExtra(EXTRA_COURSE_END));

        if (currentScheduleText.equals(getString(R.string.not_available_short))) {
            currentScheduleText = getString(R.string.no_schedule_available);
        }

        refreshCourseTexts();
    }

    private void refreshCourseTexts() {
        textCode.setText(currentCode);
        textTitle.setText(currentTitle);
        textDescription.setText(currentDescription);

        textSection.setText(getString(R.string.section_value, currentSection));
        textDepartment.setText(getString(R.string.department_value, currentDepartment));
        textMajor.setText(getString(R.string.major_value, currentMajorName));
        textFaculty.setText(getString(R.string.faculty_value, facultyName));

        textSchedule.setText(getString(R.string.schedule_value, currentScheduleText));
        textCredits.setText(getString(R.string.credits_value, currentCredits));
        textStudyYear.setText(getString(R.string.study_year_value, currentStudyYear));
        textSemester.setText(getString(R.string.semester_value, currentSemester));
        textAcademicYear.setText(getString(R.string.academic_year_value, currentAcademicYear));

        textCourseId.setText(getString(
                R.string.course_id_value,
                courseId.isEmpty() ? getString(R.string.not_available_short) : courseId
        ));
    }

    private void setupListeners() {
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        buttonAssignInstructor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFacultyInstructorsAndShowDialog();
            }
        });

        buttonEditCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditCourseDialog();
            }
        });

        buttonEditSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSchedulesAndShowEditDialog();
            }
        });

        buttonAddSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddScheduleDialog();
            }
        });
    }

    private void showEditCourseDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_course, null, false);

        final EditText editTitle = view.findViewById(R.id.editCourseTitle);
        final EditText editCode = view.findViewById(R.id.editCourseCode);
        final EditText editDescription = view.findViewById(R.id.editCourseDescription);
        final EditText editSection = view.findViewById(R.id.editCourseSection);
        final EditText editDepartment = view.findViewById(R.id.editCourseDepartment);
        final EditText editStudyYear = view.findViewById(R.id.editCourseStudyYear);
        final EditText editCredits = view.findViewById(R.id.editCourseCredits);
        final AutoCompleteTextView autoSemester = view.findViewById(R.id.autoCourseSemester);
        final EditText editAcademicYear = view.findViewById(R.id.editCourseAcademicYear);

        final EditText editEnrollmentStart = view.findViewById(R.id.editEnrollmentStart);
        final EditText editEnrollmentEnd = view.findViewById(R.id.editEnrollmentEnd);
        final EditText editCourseStart = view.findViewById(R.id.editCourseStart);
        final EditText editCourseEnd = view.findViewById(R.id.editCourseEnd);

        editTitle.setText(cleanDisplay(currentTitle));
        editCode.setText(cleanDisplay(currentCode));
        editDescription.setText(cleanDisplay(currentDescription));
        editSection.setText(cleanDisplay(currentSection));
        editDepartment.setText(cleanDisplay(currentDepartment));
        editStudyYear.setText(cleanDisplay(currentStudyYear));
        editCredits.setText(cleanDisplay(currentCredits));
        autoSemester.setText(cleanDisplay(currentSemester), false);
        editAcademicYear.setText(cleanDisplay(currentAcademicYear));

        editEnrollmentStart.setText(cleanDisplay(currentEnrollmentStart));
        editEnrollmentEnd.setText(cleanDisplay(currentEnrollmentEnd));
        editCourseStart.setText(cleanDisplay(currentCourseStart));
        editCourseEnd.setText(cleanDisplay(currentCourseEnd));

        setupEditDateTimePicker(editEnrollmentStart);
        setupEditDateTimePicker(editEnrollmentEnd);
        setupEditDateTimePicker(editCourseStart);
        setupEditDateTimePicker(editCourseEnd);

        String[] semesters = {"FALL", "SPRING", "SUMMER"};

        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                semesters
        );

        autoSemester.setAdapter(semesterAdapter);
        autoSemester.setThreshold(0);

        autoSemester.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoSemester.showDropDown();
            }
        });

        setupEditAutoPeriodFill(
                autoSemester,
                editAcademicYear,
                editEnrollmentStart,
                editEnrollmentEnd,
                editCourseStart,
                editCourseEnd
        );

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.edit_course)
                .setView(view)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = getText(editTitle);
                String code = getText(editCode).toUpperCase(Locale.ROOT);
                String description = getText(editDescription);
                String section = getText(editSection);
                String department = getText(editDepartment);
                String studyYear = getText(editStudyYear);
                String credits = getText(editCredits);
                String semester = getText(autoSemester).toUpperCase(Locale.ROOT);
                String academicYear = getText(editAcademicYear);

                String enrollmentStart = getText(editEnrollmentStart);
                String enrollmentEnd = getText(editEnrollmentEnd);
                String courseStart = getText(editCourseStart);
                String courseEnd = getText(editCourseEnd);

                boolean valid = isEditCourseValid(
                        title,
                        code,
                        studyYear,
                        credits,
                        semester,
                        academicYear,
                        enrollmentStart,
                        enrollmentEnd,
                        courseStart,
                        courseEnd,
                        editTitle,
                        editCode,
                        editStudyYear,
                        editCredits,
                        autoSemester,
                        editAcademicYear,
                        editEnrollmentStart,
                        editEnrollmentEnd,
                        editCourseStart,
                        editCourseEnd
                );

                if (!valid) {
                    return;
                }

                updateCourse(
                        title,
                        code,
                        description,
                        section,
                        department,
                        studyYear,
                        credits,
                        semester,
                        academicYear,
                        enrollmentStart,
                        enrollmentEnd,
                        courseStart,
                        courseEnd,
                        dialog
                );
            }
        });
    }

    private void setupEditAutoPeriodFill(final AutoCompleteTextView autoSemester,
                                         final EditText editAcademicYear,
                                         final EditText editEnrollmentStart,
                                         final EditText editEnrollmentEnd,
                                         final EditText editCourseStart,
                                         final EditText editCourseEnd) {

        autoSemester.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fillEditDatesFromSemesterAndYear(
                        autoSemester,
                        editAcademicYear,
                        editEnrollmentStart,
                        editEnrollmentEnd,
                        editCourseStart,
                        editCourseEnd
                );
            }
        });

        editAcademicYear.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    fillEditDatesFromSemesterAndYear(
                            autoSemester,
                            editAcademicYear,
                            editEnrollmentStart,
                            editEnrollmentEnd,
                            editCourseStart,
                            editCourseEnd
                    );
                }
            }
        });
    }

    private void fillEditDatesFromSemesterAndYear(AutoCompleteTextView autoSemester,
                                                  EditText editAcademicYear,
                                                  EditText editEnrollmentStart,
                                                  EditText editEnrollmentEnd,
                                                  EditText editCourseStart,
                                                  EditText editCourseEnd) {

        String semester = getText(autoSemester).toUpperCase(Locale.ROOT);
        String academicYear = getText(editAcademicYear);

        if (TextUtils.isEmpty(semester) || TextUtils.isEmpty(academicYear)) {
            return;
        }

        AcademicPeriodDefaults.PeriodDates dates =
                AcademicPeriodDefaults.getDefaultDates(semester, academicYear);

        if (dates == null) {
            return;
        }

        editEnrollmentStart.setText(dates.getEnrollmentStart());
        editEnrollmentEnd.setText(dates.getEnrollmentEnd());
        editCourseStart.setText(dates.getCourseStart());
        editCourseEnd.setText(dates.getCourseEnd());

        editEnrollmentStart.setError(null);
        editEnrollmentEnd.setError(null);
        editCourseStart.setError(null);
        editCourseEnd.setError(null);
    }

    private void updateCourse(final String title,
                              final String code,
                              final String description,
                              final String section,
                              final String department,
                              final String studyYear,
                              final String credits,
                              final String semester,
                              final String academicYear,
                              final String enrollmentStart,
                              final String enrollmentEnd,
                              final String courseStart,
                              final String courseEnd,
                              final AlertDialog dialog) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.UPDATE_COURSE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(cleanJson(response));
                            boolean success = obj.optBoolean("success", false);

                            String message = obj.optString(
                                    "message",
                                    success ? getString(R.string.course_updated_successfully) : getString(R.string.failed_update_course)
                            );

                            Toast.makeText(CourseDetailsActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (success) {
                                currentTitle = title;
                                currentCode = code;
                                currentDescription = description;
                                currentSection = section;
                                currentDepartment = department;
                                currentStudyYear = studyYear;
                                currentCredits = credits;
                                currentSemester = semester;
                                currentAcademicYear = academicYear;

                                currentEnrollmentStart = enrollmentStart;
                                currentEnrollmentEnd = enrollmentEnd;
                                currentCourseStart = courseStart;
                                currentCourseEnd = courseEnd;

                                refreshCourseTexts();
                                setResult(RESULT_OK);
                                dialog.dismiss();
                            }

                        } catch (Exception e) {
                            Toast.makeText(
                                    CourseDetailsActivity.this,
                                    getString(R.string.invalid_server_response),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(
                                CourseDetailsActivity.this,
                                getString(R.string.error_updating_course),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("course_id", courseId);
                params.put("title", title);
                params.put("code", code);
                params.put("description", description);
                params.put("section", section);
                params.put("department", department);
                params.put("study_year", studyYear);
                params.put("credits", credits);
                params.put("semester", semester);
                params.put("academic_year", academicYear);

                params.put("enrollment_start_at", enrollmentStart);
                params.put("enrollment_end_at", enrollmentEnd);
                params.put("course_start_at", courseStart);
                params.put("course_end_at", courseEnd);

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private boolean isEditCourseValid(String title,
                                      String code,
                                      String studyYear,
                                      String credits,
                                      String semester,
                                      String academicYear,
                                      String enrollmentStart,
                                      String enrollmentEnd,
                                      String courseStart,
                                      String courseEnd,
                                      EditText editTitle,
                                      EditText editCode,
                                      EditText editStudyYear,
                                      EditText editCredits,
                                      AutoCompleteTextView autoSemester,
                                      EditText editAcademicYear,
                                      EditText editEnrollmentStart,
                                      EditText editEnrollmentEnd,
                                      EditText editCourseStart,
                                      EditText editCourseEnd) {

        if (TextUtils.isEmpty(title)) {
            editTitle.setError(getString(R.string.error_required));
            return false;
        }

        if (TextUtils.isEmpty(code)) {
            editCode.setError(getString(R.string.error_required));
            return false;
        }

        if (TextUtils.isEmpty(studyYear)) {
            editStudyYear.setError(getString(R.string.error_required));
            return false;
        }

        if (TextUtils.isEmpty(credits)) {
            editCredits.setError(getString(R.string.error_required));
            return false;
        }

        if (!isPositiveNumber(credits)) {
            editCredits.setError(getString(R.string.credits_must_be_positive));
            return false;
        }

        if (TextUtils.isEmpty(semester)) {
            autoSemester.setError(getString(R.string.error_required));
            return false;
        }

        if (TextUtils.isEmpty(academicYear)) {
            editAcademicYear.setError(getString(R.string.error_required));
            return false;
        }

        if (TextUtils.isEmpty(enrollmentStart)) {
            editEnrollmentStart.setError(getString(R.string.error_select_enrollment_start));
            return false;
        }

        if (TextUtils.isEmpty(enrollmentEnd)) {
            editEnrollmentEnd.setError(getString(R.string.error_select_enrollment_end));
            return false;
        }

        if (TextUtils.isEmpty(courseStart)) {
            editCourseStart.setError(getString(R.string.error_select_course_start));
            return false;
        }

        if (TextUtils.isEmpty(courseEnd)) {
            editCourseEnd.setError(getString(R.string.error_select_course_end));
            return false;
        }

        if (enrollmentStart.compareTo(enrollmentEnd) >= 0) {
            editEnrollmentEnd.setError(getString(R.string.error_invalid_enrollment_period));
            return false;
        }

        if (courseStart.compareTo(courseEnd) >= 0) {
            editCourseEnd.setError(getString(R.string.error_invalid_course_period));
            return false;
        }

        if (enrollmentEnd.compareTo(courseStart) > 0) {
            editEnrollmentEnd.setError(getString(R.string.error_enrollment_before_course));
            return false;
        }

        if (!isAcademicPeriodValid(semester, academicYear, courseStart)) {
            editCourseStart.setError(getString(R.string.error_invalid_academic_period));
            return false;
        }

        return true;
    }

    private boolean isAcademicPeriodValid(String semester, String academicYear, String courseStart) {
        return AcademicPeriodDefaults.isAcademicPeriodValid(semester, academicYear, courseStart);
    }

    private void showAddScheduleDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_schedule, null, false);

        final AutoCompleteTextView autoDay = view.findViewById(R.id.autoScheduleDay);
        final EditText editStart = view.findViewById(R.id.editScheduleStartTime);
        final EditText editEnd = view.findViewById(R.id.editScheduleEndTime);
        final EditText editRoom = view.findViewById(R.id.editScheduleRoom);

        setupDayDropdown(autoDay);
        setupTimePicker(editStart);
        setupTimePicker(editEnd);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_schedule)
                .setView(view)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String day = getText(autoDay).toUpperCase(Locale.ROOT);
                String start = normalizeTime(getText(editStart));
                String end = normalizeTime(getText(editEnd));
                String room = getText(editRoom);

                if (TextUtils.isEmpty(day)) {
                    autoDay.setError(getString(R.string.error_required));
                    return;
                }

                if (TextUtils.isEmpty(start)) {
                    editStart.setError(getString(R.string.error_required));
                    return;
                }

                if (TextUtils.isEmpty(end)) {
                    editEnd.setError(getString(R.string.error_required));
                    return;
                }

                int startMinutes = timeToMinutes(start);
                int endMinutes = timeToMinutes(end);

                if (startMinutes < 0 || endMinutes < 0 || startMinutes >= endMinutes) {
                    editEnd.setError(getString(R.string.time_start_before_end));
                    return;
                }

                addCourseSchedule(day, start, end, room, dialog);
            }
        });
    }

    private void addCourseSchedule(final String day,
                                   final String start,
                                   final String end,
                                   final String room,
                                   final AlertDialog dialog) {
        if (TextUtils.isEmpty(courseId)) {
            Toast.makeText(this, getString(R.string.course_id_missing), Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADD_COURSE_SCHEDULE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(cleanJson(response));
                            boolean success = obj.optBoolean("success", false);

                            String message = obj.optString(
                                    "message",
                                    success ? getString(R.string.schedule_added_successfully) : getString(R.string.failed_add_schedule)
                            );

                            Toast.makeText(CourseDetailsActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (success) {
                                currentScheduleText = day + " " + trimSeconds(start) + " - " + trimSeconds(end);

                                if (!TextUtils.isEmpty(room)) {
                                    currentScheduleText += " • " + room;
                                }

                                refreshCourseTexts();
                                dialog.dismiss();
                            }

                        } catch (Exception e) {
                            Toast.makeText(
                                    CourseDetailsActivity.this,
                                    getString(R.string.invalid_server_response),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(
                                CourseDetailsActivity.this,
                                getString(R.string.error_adding_schedule),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("course_id", courseId);
                params.put("day_of_week", day);
                params.put("start_time", start);
                params.put("end_time", end);
                params.put("room", room);

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void loadSchedulesAndShowEditDialog() {
        if (TextUtils.isEmpty(courseId)) {
            Toast.makeText(this, getString(R.string.course_id_missing), Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.GET_COURSE_SCHEDULES + "?course_id=" + courseId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray schedules = extractSchedulesArray(response);

                            if (schedules.length() == 0) {
                                Toast.makeText(
                                        CourseDetailsActivity.this,
                                        getString(R.string.no_schedule_found),
                                        Toast.LENGTH_LONG
                                ).show();
                                return;
                            }

                            if (schedules.length() == 1) {
                                showEditScheduleDialog(schedules.getJSONObject(0));
                            } else {
                                showSelectScheduleDialog(schedules);
                            }

                        } catch (Exception e) {
                            Toast.makeText(
                                    CourseDetailsActivity.this,
                                    getString(R.string.error_reading_schedules),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(
                                CourseDetailsActivity.this,
                                getString(R.string.error_loading_schedules),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private JSONArray extractSchedulesArray(String response) throws Exception {
        String clean = cleanJson(response);

        if (clean.startsWith("[")) {
            return new JSONArray(clean);
        }

        JSONObject obj = new JSONObject(clean);

        if (obj.has("data") && obj.optJSONArray("data") != null) {
            return obj.optJSONArray("data");
        }

        if (obj.has("schedules") && obj.optJSONArray("schedules") != null) {
            return obj.optJSONArray("schedules");
        }

        return new JSONArray();
    }

    private void showSelectScheduleDialog(final JSONArray schedules) {
        List<String> labels = new ArrayList<String>();

        for (int i = 0; i < schedules.length(); i++) {
            try {
                JSONObject item = schedules.getJSONObject(i);

                String label = item.optString("day_of_week", "") + " "
                        + trimSeconds(item.optString("start_time", "")) + " - "
                        + trimSeconds(item.optString("end_time", ""));

                String room = item.optString("room", "");

                if (!TextUtils.isEmpty(room)) {
                    label += " • " + room;
                }

                labels.add(label);

            } catch (Exception ignored) {
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_schedule)
                .setItems(labels.toArray(new String[0]), new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        try {
                            showEditScheduleDialog(schedules.getJSONObject(which));
                        } catch (Exception e) {
                            Toast.makeText(
                                    CourseDetailsActivity.this,
                                    getString(R.string.invalid_schedule),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showEditScheduleDialog(JSONObject schedule) {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_schedule, null, false);

        final AutoCompleteTextView autoDay = view.findViewById(R.id.autoScheduleDay);
        final EditText editStart = view.findViewById(R.id.editScheduleStartTime);
        final EditText editEnd = view.findViewById(R.id.editScheduleEndTime);
        final EditText editRoom = view.findViewById(R.id.editScheduleRoom);

        setupDayDropdown(autoDay);
        setupTimePicker(editStart);
        setupTimePicker(editEnd);

        final String scheduleId = schedule.optString("schedule_id", "");

        autoDay.setText(schedule.optString("day_of_week", ""), false);
        editStart.setText(trimSeconds(schedule.optString("start_time", "")));
        editEnd.setText(trimSeconds(schedule.optString("end_time", "")));
        editRoom.setText(schedule.optString("room", ""));

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.edit_schedule)
                .setView(view)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String day = getText(autoDay).toUpperCase(Locale.ROOT);
                String start = normalizeTime(getText(editStart));
                String end = normalizeTime(getText(editEnd));
                String room = getText(editRoom);

                if (TextUtils.isEmpty(day)) {
                    autoDay.setError(getString(R.string.error_required));
                    return;
                }

                if (TextUtils.isEmpty(start)) {
                    editStart.setError(getString(R.string.error_required));
                    return;
                }

                if (TextUtils.isEmpty(end)) {
                    editEnd.setError(getString(R.string.error_required));
                    return;
                }

                int startMinutes = timeToMinutes(start);
                int endMinutes = timeToMinutes(end);

                if (startMinutes < 0 || endMinutes < 0 || startMinutes >= endMinutes) {
                    editEnd.setError(getString(R.string.time_start_before_end));
                    return;
                }

                updateCourseSchedule(scheduleId, day, start, end, room, dialog);
            }
        });
    }

    private void updateCourseSchedule(final String scheduleId,
                                      final String day,
                                      final String start,
                                      final String end,
                                      final String room,
                                      final AlertDialog dialog) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.UPDATE_COURSE_SCHEDULE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(cleanJson(response));
                            boolean success = obj.optBoolean("success", false);

                            String message = obj.optString(
                                    "message",
                                    success ? getString(R.string.schedule_updated_successfully) : getString(R.string.failed_update_schedule)
                            );

                            Toast.makeText(CourseDetailsActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (success) {
                                currentScheduleText = day + " " + trimSeconds(start) + " - " + trimSeconds(end);

                                if (!TextUtils.isEmpty(room)) {
                                    currentScheduleText += " • " + room;
                                }

                                refreshCourseTexts();
                                dialog.dismiss();
                            }

                        } catch (Exception e) {
                            Toast.makeText(
                                    CourseDetailsActivity.this,
                                    getString(R.string.invalid_server_response),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(
                                CourseDetailsActivity.this,
                                getString(R.string.error_updating_schedule),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("schedule_id", scheduleId);
                params.put("course_id", courseId);
                params.put("day_of_week", day);
                params.put("start_time", start);
                params.put("end_time", end);
                params.put("room", room);

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void setupDayDropdown(final AutoCompleteTextView autoDay) {
        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                days
        );

        autoDay.setAdapter(dayAdapter);
        autoDay.setThreshold(0);

        autoDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoDay.showDropDown();
            }
        });
    }

    private void setupTimePicker(final EditText editText) {
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setCursorVisible(false);
        editText.setInputType(0);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTimePicker(editText);
            }
        });
    }

    private void openTimePicker(final EditText target) {
        int hour = 8;
        int minute = 0;

        String current = getText(target);

        if (!TextUtils.isEmpty(current)) {
            try {
                String[] parts = trimSeconds(current).split(":");

                if (parts.length >= 2) {
                    hour = Integer.parseInt(parts[0]);
                    minute = Integer.parseInt(parts[1]);
                }

            } catch (Exception ignored) {
            }
        }

        TimePickerDialog picker = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(android.widget.TimePicker view, int selectedHour, int selectedMinute) {
                        String time = String.format(Locale.ROOT, "%02d:%02d", selectedHour, selectedMinute);
                        target.setText(time);
                        target.setError(null);
                    }
                },
                hour,
                minute,
                true
        );

        picker.show();
    }

    private void setupEditDateTimePicker(final EditText editText) {
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setCursorVisible(false);
        editText.setInputType(0);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDatePicker(editText);
            }
        });
    }

    private void showEditDatePicker(final EditText target) {
        Calendar calendar = Calendar.getInstance();

        String current = getText(target);

        if (!TextUtils.isEmpty(current) && current.length() >= 10) {
            try {
                int year = Integer.parseInt(current.substring(0, 4));
                int month = Integer.parseInt(current.substring(5, 7)) - 1;
                int day = Integer.parseInt(current.substring(8, 10));

                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);

            } catch (Exception ignored) {
            }
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        showEditTimePickerForDate(target, selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void showEditTimePickerForDate(final EditText target, final Calendar selectedDate) {
        int hour = 8;
        int minute = 0;

        String current = getText(target);

        if (!TextUtils.isEmpty(current) && current.length() >= 16) {
            try {
                hour = Integer.parseInt(current.substring(11, 13));
                minute = Integer.parseInt(current.substring(14, 16));
            } catch (Exception ignored) {
            }
        }

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(android.widget.TimePicker view, int selectedHour, int selectedMinute) {
                        selectedDate.set(Calendar.HOUR_OF_DAY, selectedHour);
                        selectedDate.set(Calendar.MINUTE, selectedMinute);
                        selectedDate.set(Calendar.SECOND, 0);

                        String dateTime = String.format(
                                Locale.ROOT,
                                "%04d-%02d-%02d %02d:%02d:00",
                                selectedDate.get(Calendar.YEAR),
                                selectedDate.get(Calendar.MONTH) + 1,
                                selectedDate.get(Calendar.DAY_OF_MONTH),
                                selectedDate.get(Calendar.HOUR_OF_DAY),
                                selectedDate.get(Calendar.MINUTE)
                        );

                        target.setText(dateTime);
                        target.setError(null);
                    }
                },
                hour,
                minute,
                true
        );

        dialog.show();
    }

    private int timeToMinutes(String time) {
        if (time == null) {
            return -1;
        }

        String clean = trimSeconds(time);

        try {
            String[] parts = clean.split(":");

            if (parts.length < 2) {
                return -1;
            }

            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return -1;
            }

            return hour * 60 + minute;

        } catch (Exception e) {
            return -1;
        }
    }

    private String normalizeTime(String time) {
        if (time == null) {
            return "";
        }

        String clean = time.trim();

        if (clean.matches("^\\d{1}:\\d{2}$")) {
            clean = "0" + clean;
        }

        if (clean.matches("^\\d{2}:\\d{2}$")) {
            return clean + ":00";
        }

        if (clean.matches("^\\d{2}:\\d{2}:\\d{2}$")) {
            return clean;
        }

        return "";
    }

    private String trimSeconds(String time) {
        if (time == null) {
            return "";
        }

        String clean = time.trim();

        if (clean.length() >= 5) {
            return clean.substring(0, 5);
        }

        return clean;
    }

    private String getText(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }



    private boolean isPositiveNumber(String value) {
        try {
            int number = Integer.parseInt(value);
            return number > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String cleanJson(String response) {
        if (response == null) {
            return "{}";
        }

        String clean = response.trim();

        int startObj = clean.indexOf("{");
        int endObj = clean.lastIndexOf("}");
        int startArr = clean.indexOf("[");
        int endArr = clean.lastIndexOf("]");

        if (startObj != -1 && endObj != -1 && endObj > startObj) {
            return clean.substring(startObj, endObj + 1);
        }

        if (startArr != -1 && endArr != -1 && endArr > startArr) {
            return clean.substring(startArr, endArr + 1);
        }

        return clean;
    }

    private String cleanDisplay(String value) {
        if (value == null
                || value.trim().isEmpty()
                || value.equalsIgnoreCase("null")
                || value.equals(getString(R.string.not_available_short))) {
            return "";
        }

        return value.trim();
    }

    private void loadCourseInstructors() {
        if (TextUtils.isEmpty(courseId)) {
            showEmptyInstructors();
            return;
        }

        String url = ApiConfig.GET_COURSE_INSTRUCTORS + "?course_id=" + courseId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);

                            courseInstructorList.clear();

                            if (!success) {
                                showEmptyInstructors();
                                return;
                            }

                            JSONArray data = obj.optJSONArray("data");

                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject item = data.getJSONObject(i);

                                    CourseInstructorModel instructor = new CourseInstructorModel();
                                    instructor.setCourseInstructorId(item.optString("course_instructor_id", ""));
                                    instructor.setCourseId(item.optString("course_id", ""));
                                    instructor.setInstructorId(item.optString("instructor_id", ""));
                                    instructor.setFullName(item.optString("full_name", ""));
                                    instructor.setEmail(item.optString("email", ""));
                                    instructor.setPhoneNumber(item.optString("phone_number", ""));
                                    instructor.setProfileImageUrl(item.optString("profile_image_url", ""));
                                    instructor.setAssignedAt(item.optString("assigned_at", ""));

                                    courseInstructorList.add(instructor);
                                }
                            }

                            courseInstructorAdapter.notifyDataSetChanged();
                            updateInstructorsEmptyState();

                        } catch (Exception e) {
                            showEmptyInstructors();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showEmptyInstructors();
                    }
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void loadFacultyInstructorsAndShowDialog() {
        if (TextUtils.isEmpty(facultyId)) {
            Toast.makeText(
                    this,
                    getString(R.string.error_faculty_not_assigned),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String url = ApiConfig.GET_INSTRUCTORS_BY_FACULTY + "?faculty_id=" + facultyId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);

                            instructorNames.clear();
                            instructorIds.clear();

                            if (!success) {
                                Toast.makeText(
                                        CourseDetailsActivity.this,
                                        getString(R.string.error_loading_instructors),
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            JSONArray data = obj.optJSONArray("data");

                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject item = data.getJSONObject(i);

                                    String userId = item.optString("user_id", "");
                                    String fullName = item.optString("full_name", "");
                                    String email = item.optString("email", "");

                                    if (TextUtils.isEmpty(userId)) {
                                        continue;
                                    }

                                    if (isInstructorAlreadyAssigned(userId)) {
                                        continue;
                                    }

                                    instructorIds.add(userId);

                                    if (TextUtils.isEmpty(fullName)) {
                                        instructorNames.add(email);
                                    } else if (TextUtils.isEmpty(email)) {
                                        instructorNames.add(fullName);
                                    } else {
                                        instructorNames.add(fullName + " - " + email);
                                    }
                                }
                            }

                            if (instructorIds.isEmpty()) {
                                Toast.makeText(
                                        CourseDetailsActivity.this,
                                        getString(R.string.no_instructors_found),
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            showAssignInstructorDialog();

                        } catch (Exception e) {
                            Toast.makeText(
                                    CourseDetailsActivity.this,
                                    getString(R.string.error_loading_instructors),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(
                                CourseDetailsActivity.this,
                                getString(R.string.error_loading_instructors),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void showAssignInstructorDialog() {
        final AutoCompleteTextView autoInstructor = new AutoCompleteTextView(this);
        autoInstructor.setHint(getString(R.string.select_instructor));
        autoInstructor.setInputType(0);
        autoInstructor.setPadding(32, 32, 32, 32);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                instructorNames
        );

        autoInstructor.setAdapter(adapter);
        autoInstructor.setThreshold(0);

        autoInstructor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoInstructor.showDropDown();
            }
        });

        final int[] selectedPosition = {-1};

        autoInstructor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < instructorIds.size()) {
                    selectedPosition[0] = position;
                    autoInstructor.setError(null);
                }
            }
        });

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.add_instructor_to_course))
                .setView(autoInstructor)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedPosition[0] < 0 || selectedPosition[0] >= instructorIds.size()) {
                    autoInstructor.setError(getString(R.string.error_select_instructor));
                    return;
                }

                String instructorId = instructorIds.get(selectedPosition[0]);
                assignInstructorToCourse(instructorId, dialog);
            }
        });
    }

    private void assignInstructorToCourse(final String instructorId, final AlertDialog dialog) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ASSIGN_INSTRUCTOR_TO_COURSE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);
                            String code = obj.optString("code", "");
                            String message = getAssignInstructorMessage(code, success, obj);

                            Toast.makeText(CourseDetailsActivity.this, message, Toast.LENGTH_LONG).show();

                            if (success) {
                                dialog.dismiss();
                                loadCourseInstructors();
                            }

                        } catch (Exception e) {
                            Toast.makeText(
                                    CourseDetailsActivity.this,
                                    getString(R.string.error_assigning_instructor),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(
                                CourseDetailsActivity.this,
                                getString(R.string.error_assigning_instructor),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("course_id", courseId);
                params.put("instructor_id", instructorId);

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private String getAssignInstructorMessage(String code, boolean success, JSONObject obj) {
        if (success) {
            return getString(R.string.instructor_assigned_successfully);
        }

        if ("ALREADY_ASSIGNED".equals(code)) {
            return getString(R.string.already_assigned);
        }

        if ("TIME_CONFLICT".equals(code)) {
            String title = obj.optString("conflict_course_title", "");
            String courseCode = obj.optString("conflict_course_code", "");

            String message = getString(R.string.instructor_time_conflict);

            if (!TextUtils.isEmpty(title)) {
                message = message + " " + title;
            }

            if (!TextUtils.isEmpty(courseCode)) {
                message = message + " (" + courseCode + ")";
            }

            return message;
        }

        return getString(R.string.error_assigning_instructor);
    }

    private void confirmRemoveInstructor(final int position, final CourseInstructorModel instructor) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.remove_instructor)
                .setMessage(R.string.confirm_remove_instructor_from_course)
                .setPositiveButton(R.string.remove_instructor, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        removeInstructorFromCourse(position, instructor);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void removeInstructorFromCourse(final int position, final CourseInstructorModel instructor) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.REMOVE_COURSE_INSTRUCTOR,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean success = obj.optBoolean("success", false);

                            if (success) {
                                Toast.makeText(
                                        CourseDetailsActivity.this,
                                        getString(R.string.instructor_removed_successfully),
                                        Toast.LENGTH_SHORT
                                ).show();

                                if (position >= 0 && position < courseInstructorList.size()) {
                                    courseInstructorList.remove(position);
                                    courseInstructorAdapter.notifyItemRemoved(position);
                                }

                                updateInstructorsEmptyState();
                            } else {
                                Toast.makeText(
                                        CourseDetailsActivity.this,
                                        getString(R.string.error_removing_instructor),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                        } catch (Exception e) {
                            Toast.makeText(
                                    CourseDetailsActivity.this,
                                    getString(R.string.error_removing_instructor),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(
                                CourseDetailsActivity.this,
                                getString(R.string.error_removing_instructor),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("course_instructor_id", safe(instructor.getCourseInstructorId()));

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private boolean isInstructorAlreadyAssigned(String instructorId) {
        for (int i = 0; i < courseInstructorList.size(); i++) {
            CourseInstructorModel item = courseInstructorList.get(i);

            if (safe(item.getInstructorId()).equals(safe(instructorId))) {
                return true;
            }
        }

        return false;
    }

    private void updateInstructorsEmptyState() {
        if (courseInstructorList.isEmpty()) {
            showEmptyInstructors();
        } else {
            recyclerCourseInstructors.setVisibility(View.VISIBLE);
            textEmptyCourseInstructors.setVisibility(View.GONE);
        }
    }

    private void showEmptyInstructors() {
        recyclerCourseInstructors.setVisibility(View.GONE);
        textEmptyCourseInstructors.setVisibility(View.VISIBLE);
    }

    private String display(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return getString(R.string.not_available_short);
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