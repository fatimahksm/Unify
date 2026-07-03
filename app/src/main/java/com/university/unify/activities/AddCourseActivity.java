package com.university.unify.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.adapter.CourseScheduleInputAdapter;
import com.university.unify.constants.SemesterConstants;
import com.university.unify.constants.StudyYearConstants;
import com.university.unify.model.CourseScheduleInputModel;
import com.university.unify.model.MajorModel;
import com.university.unify.network.ApiConfig;
import com.university.unify.utils.AcademicPeriodDefaults;
import com.university.unify.utils.AcademicYearUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddCourseActivity extends AppCompatActivity {

    private static final String TAG = "AddCourseActivity";

    private ImageButton buttonBack;

    private EditText editCourseTitle;
    private EditText editCourseCode;
    private EditText editCourseCredits;
    private EditText editCourseSection;
    private EditText editEnrollmentStart;
    private EditText editEnrollmentEnd;
    private EditText editCourseStart;
    private EditText editCourseEnd;
    private EditText editCourseDescription;

    private AutoCompleteTextView autoMajor;
    private AutoCompleteTextView autoStudyYear;
    private AutoCompleteTextView autoSemester;
    private AutoCompleteTextView autoAcademicYear;

    private MaterialButton buttonAddScheduleTime;
    private RecyclerView recyclerScheduleInputs;
    private TextView textNoScheduleTimes;

    private View buttonSaveCourse;
    private ProgressBar progressSave;

    private RequestQueue queue;

    private List<MajorModel> majorList;
    private List<String> majorNames;
    private List<CourseScheduleInputModel> scheduleList;

    private CourseScheduleInputAdapter scheduleAdapter;

    private MajorModel selectedMajor;
    private String facultyId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        queue = Volley.newRequestQueue(this);

        majorList = new ArrayList<MajorModel>();
        majorNames = new ArrayList<String>();
        scheduleList = new ArrayList<CourseScheduleInputModel>();

        connectViews();
        setupButtons();
        setupDropdowns();
        setupScheduleList();

        getFacultyAndLoadMajors();
    }

    private void connectViews() {
        buttonBack = findViewById(R.id.buttonBack);

        editCourseTitle = findViewById(R.id.editCourseTitle);
        editCourseCode = findViewById(R.id.editCourseCode);
        editCourseCredits = findViewById(R.id.editCourseCredits);
        editCourseSection = findViewById(R.id.editCourseSection);
        editEnrollmentStart = findViewById(R.id.editEnrollmentStart);
        editEnrollmentEnd = findViewById(R.id.editEnrollmentEnd);
        editCourseStart = findViewById(R.id.editCourseStart);
        editCourseEnd = findViewById(R.id.editCourseEnd);
        editCourseDescription = findViewById(R.id.editCourseDescription);

        autoMajor = findViewById(R.id.autoMajorForCourse);
        autoStudyYear = findViewById(R.id.autoStudyYearForCourse);
        autoSemester = findViewById(R.id.autoSemesterForCourse);
        autoAcademicYear = findViewById(R.id.autoAcademicYearForCourse);

        buttonAddScheduleTime = findViewById(R.id.buttonAddScheduleTime);
        recyclerScheduleInputs = findViewById(R.id.recyclerScheduleInputs);
        textNoScheduleTimes = findViewById(R.id.textNoScheduleTimes);

        buttonSaveCourse = findViewById(R.id.buttonSaveCourse);
        progressSave = findViewById(R.id.progressSave);
    }

    private void setupButtons() {
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setupDateTimePicker(editEnrollmentStart);
        setupDateTimePicker(editEnrollmentEnd);
        setupDateTimePicker(editCourseStart);
        setupDateTimePicker(editCourseEnd);

        buttonAddScheduleTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showScheduleDialog();
            }
        });

        buttonSaveCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitCourse();
            }
        });
    }

    private void setupDropdowns() {
        setupStudyYearDropdown();
        setupSemesterDropdown();
        setupAcademicYearDropdown();
        setupAutoPeriodFill();
    }

    private void setupScheduleList() {
        scheduleAdapter = new CourseScheduleInputAdapter(
                this,
                scheduleList,
                new CourseScheduleInputAdapter.OnScheduleActionListener() {
                    @Override
                    public void onRemoveClicked(int position) {
                        if (position >= 0 && position < scheduleList.size()) {
                            scheduleList.remove(position);
                            scheduleAdapter.notifyDataSetChanged();
                            showEmptyScheduleMessage();
                        }
                    }
                }
        );

        recyclerScheduleInputs.setLayoutManager(new LinearLayoutManager(this));
        recyclerScheduleInputs.setAdapter(scheduleAdapter);
        recyclerScheduleInputs.setNestedScrollingEnabled(false);

        showEmptyScheduleMessage();
    }

    private void showEmptyScheduleMessage() {
        if (scheduleList.size() == 0) {
            textNoScheduleTimes.setVisibility(View.VISIBLE);
            recyclerScheduleInputs.setVisibility(View.GONE);
        } else {
            textNoScheduleTimes.setVisibility(View.GONE);
            recyclerScheduleInputs.setVisibility(View.VISIBLE);
        }
    }

    private void getFacultyAndLoadMajors() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        facultyId = clean(databaseHelper.getLoggedInFacultyId());

        if (facultyId.length() == 0) {
            Toast.makeText(this, getString(R.string.error_faculty_not_assigned), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadMajors();
    }

    private void loadMajors() {
        setLoading(true);

        String url = ApiConfig.GET_MAJORS_BY_FACULTY + "?faculty_id=" + facultyId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        setLoading(false);

                        try {
                            JSONObject object = new JSONObject(response);

                            if (!object.optBoolean("success", false)) {
                                Toast.makeText(
                                        AddCourseActivity.this,
                                        object.optString("message", getString(R.string.error_loading_courses)),
                                        Toast.LENGTH_LONG
                                ).show();
                                return;
                            }

                            majorList.clear();
                            majorNames.clear();

                            JSONArray data = object.optJSONArray("data");

                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject item = data.getJSONObject(i);

                                    MajorModel major = new MajorModel();
                                    major.setMajorId(item.optString("major_id", ""));
                                    major.setFacultyId(item.optString("faculty_id", ""));
                                    major.setFacultyName(item.optString("faculty_name", ""));
                                    major.setName(item.optString("name", ""));
                                    major.setCode(item.optString("code", ""));
                                    major.setActive("1".equals(item.optString("is_active", "1")));

                                    majorList.add(major);
                                    majorNames.add(major.getName());
                                }
                            }

                            setupMajorDropdown();

                        } catch (Exception e) {
                            Log.e(TAG, "Major parse error", e);
                            Toast.makeText(
                                    AddCourseActivity.this,
                                    getString(R.string.error_loading_courses),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setLoading(false);
                        Toast.makeText(AddCourseActivity.this, getErrorMessage(error), Toast.LENGTH_LONG).show();
                    }
                }
        );

        queue.add(request);
    }

    private void setupMajorDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                majorNames
        );

        autoMajor.setAdapter(adapter);
        autoMajor.setThreshold(0);

        autoMajor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoMajor.showDropDown();
            }
        });

        autoMajor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < majorList.size()) {
                    selectedMajor = majorList.get(position);
                    autoMajor.setError(null);
                }
            }
        });
    }

    private void setupStudyYearDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                StudyYearConstants.STUDY_YEARS
        );

        autoStudyYear.setAdapter(adapter);
        autoStudyYear.setThreshold(0);

        autoStudyYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoStudyYear.showDropDown();
            }
        });
    }

    private void setupSemesterDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                SemesterConstants.SEMESTERS
        );

        autoSemester.setAdapter(adapter);
        autoSemester.setThreshold(0);

        autoSemester.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoSemester.showDropDown();
            }
        });
    }

    private void setupAcademicYearDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                AcademicYearUtils.getAcademicYears()
        );

        autoAcademicYear.setAdapter(adapter);
        autoAcademicYear.setThreshold(0);

        autoAcademicYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoAcademicYear.showDropDown();
            }
        });
    }

    private void setupAutoPeriodFill() {
        autoSemester.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fillDatesFromSemesterAndYear();
            }
        });

        autoAcademicYear.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fillDatesFromSemesterAndYear();
            }
        });
    }

    private void fillDatesFromSemesterAndYear() {
        String semester = getAutoText(autoSemester).toUpperCase(Locale.ROOT);
        String academicYear = getAutoText(autoAcademicYear);

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

    private void showScheduleDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        int padding = dp(20);
        layout.setPadding(padding, padding, padding, 0);

        final AutoCompleteTextView inputDay = new AutoCompleteTextView(this);
        inputDay.setHint(getString(R.string.select_day));
        inputDay.setInputType(0);
        inputDay.setFocusable(false);

        final EditText inputStart = new EditText(this);
        inputStart.setHint(getString(R.string.start_time_hint));
        inputStart.setInputType(0);
        inputStart.setFocusable(false);

        final EditText inputEnd = new EditText(this);
        inputEnd.setHint(getString(R.string.end_time_hint));
        inputEnd.setInputType(0);
        inputEnd.setFocusable(false);

        final EditText inputRoom = new EditText(this);
        inputRoom.setHint(getString(R.string.room_hint));

        addBottomSpace(inputDay, 10);
        addBottomSpace(inputStart, 10);
        addBottomSpace(inputEnd, 10);
        addBottomSpace(inputRoom, 0);

        final String[] selectedDayValue = new String[]{""};
        final String[] selectedDayText = new String[]{""};

        setupDayDropdown(inputDay, selectedDayValue, selectedDayText);
        setupTimePicker(inputStart);
        setupTimePicker(inputEnd);

        layout.addView(inputDay);
        layout.addView(inputStart);
        layout.addView(inputEnd);
        layout.addView(inputRoom);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_schedule_time)
                .setView(layout)
                .setPositiveButton(R.string.add, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dayValue = selectedDayValue[0];
                String dayText = selectedDayText[0];
                String start = normalizeTime(getText(inputStart));
                String end = normalizeTime(getText(inputEnd));
                String room = getText(inputRoom);

                if (TextUtils.isEmpty(dayValue)) {
                    inputDay.setError(getString(R.string.error_select_day));
                    return;
                }

                if (!isValidTime(start)) {
                    inputStart.setError(getString(R.string.error_invalid_time_format));
                    return;
                }

                if (!isValidTime(end)) {
                    inputEnd.setError(getString(R.string.error_invalid_time_format));
                    return;
                }

                if (timeToMinutes(start) >= timeToMinutes(end)) {
                    inputEnd.setError(getString(R.string.error_invalid_time_range));
                    return;
                }

                if (isDuplicateSchedule(dayValue, start, end)) {
                    Toast.makeText(
                            AddCourseActivity.this,
                            getString(R.string.schedule_time_already_added),
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                if (hasScheduleConflict(dayValue, start, end)) {
                    Toast.makeText(
                            AddCourseActivity.this,
                            getString(R.string.schedule_time_conflict),
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                CourseScheduleInputModel model = new CourseScheduleInputModel(
                        dayValue,
                        dayText,
                        start,
                        end,
                        room
                );

                scheduleList.add(model);
                scheduleAdapter.notifyDataSetChanged();
                showEmptyScheduleMessage();

                dialog.dismiss();
            }
        });
    }

    private void setupDayDropdown(final AutoCompleteTextView inputDay,
                                  final String[] selectedDayValue,
                                  final String[] selectedDayText) {

        final List<String> dayTexts = new ArrayList<String>();
        final List<String> dayValues = new ArrayList<String>();

        dayTexts.add(getString(R.string.monday));
        dayValues.add("MONDAY");

        dayTexts.add(getString(R.string.tuesday));
        dayValues.add("TUESDAY");

        dayTexts.add(getString(R.string.wednesday));
        dayValues.add("WEDNESDAY");

        dayTexts.add(getString(R.string.thursday));
        dayValues.add("THURSDAY");

        dayTexts.add(getString(R.string.friday));
        dayValues.add("FRIDAY");

        dayTexts.add(getString(R.string.saturday));
        dayValues.add("SATURDAY");

        dayTexts.add(getString(R.string.sunday));
        dayValues.add("SUNDAY");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                dayTexts
        );

        inputDay.setAdapter(adapter);
        inputDay.setThreshold(0);

        inputDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputDay.showDropDown();
            }
        });

        inputDay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < dayValues.size()) {
                    selectedDayValue[0] = dayValues.get(position);
                    selectedDayText[0] = dayTexts.get(position);
                    inputDay.setError(null);
                }
            }
        });
    }

    private void setupDateTimePicker(final EditText editText) {
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setInputType(0);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(editText);
            }
        });
    }

    private void showDatePicker(final EditText target) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        showTimePickerForDate(target, selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void showTimePickerForDate(final EditText target, final Calendar selectedDate) {
        Calendar now = Calendar.getInstance();

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(android.widget.TimePicker view, int hour, int minute) {
                        selectedDate.set(Calendar.HOUR_OF_DAY, hour);
                        selectedDate.set(Calendar.MINUTE, minute);
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
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );

        dialog.show();
    }

    private void setupTimePicker(final EditText editText) {
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setInputType(0);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker(editText);
            }
        });
    }

    private void showTimePicker(final EditText target) {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(android.widget.TimePicker view, int hour, int minute) {
                        String time = String.format(Locale.ROOT, "%02d:%02d:00", hour, minute);
                        target.setText(time);
                        target.setError(null);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        dialog.show();
    }

    private void submitCourse() {
        String title = getText(editCourseTitle);
        String code = getText(editCourseCode).toUpperCase(Locale.ROOT);
        String credits = getText(editCourseCredits);
        String section = getText(editCourseSection);
        String description = getText(editCourseDescription);

        String studyYear = getAutoText(autoStudyYear);
        String semester = getAutoText(autoSemester).toUpperCase(Locale.ROOT);
        String academicYear = getAutoText(autoAcademicYear);

        String enrollmentStart = getText(editEnrollmentStart);
        String enrollmentEnd = getText(editEnrollmentEnd);
        String courseStart = getText(editCourseStart);
        String courseEnd = getText(editCourseEnd);

        if (!isCourseValid(
                title,
                code,
                credits,
                studyYear,
                semester,
                academicYear,
                enrollmentStart,
                enrollmentEnd,
                courseStart,
                courseEnd
        )) {
            return;
        }

        if (scheduleList.size() == 0) {
            Toast.makeText(
                    this,
                    getString(R.string.error_add_at_least_one_schedule_time),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        saveCourse(
                title,
                code,
                description,
                section,
                studyYear,
                credits,
                semester,
                academicYear,
                enrollmentStart,
                enrollmentEnd,
                courseStart,
                courseEnd
        );
    }

    private boolean isCourseValid(String title,
                                  String code,
                                  String credits,
                                  String studyYear,
                                  String semester,
                                  String academicYear,
                                  String enrollmentStart,
                                  String enrollmentEnd,
                                  String courseStart,
                                  String courseEnd) {

        boolean valid = true;

        if (TextUtils.isEmpty(title)) {
            editCourseTitle.setError(getString(R.string.error_required));
            valid = false;
        }

        if (TextUtils.isEmpty(code)) {
            editCourseCode.setError(getString(R.string.error_required));
            valid = false;
        }

        if (selectedMajor == null) {
            autoMajor.setError(getString(R.string.error_select_major));
            valid = false;
        }

        if (TextUtils.isEmpty(studyYear)) {
            autoStudyYear.setError(getString(R.string.error_select_study_year));
            valid = false;
        }

        if (TextUtils.isEmpty(semester)) {
            autoSemester.setError(getString(R.string.error_required));
            valid = false;
        }

        if (TextUtils.isEmpty(academicYear)) {
            autoAcademicYear.setError(getString(R.string.error_required));
            valid = false;
        }

        if (TextUtils.isEmpty(credits)) {
            editCourseCredits.setError(getString(R.string.error_required));
            valid = false;
        } else if (!isPositiveNumber(credits)) {
            editCourseCredits.setError(getString(R.string.credits_must_be_positive));
            valid = false;
        }

        if (TextUtils.isEmpty(enrollmentStart)) {
            editEnrollmentStart.setError(getString(R.string.error_select_enrollment_start));
            valid = false;
        }

        if (TextUtils.isEmpty(enrollmentEnd)) {
            editEnrollmentEnd.setError(getString(R.string.error_select_enrollment_end));
            valid = false;
        }

        if (TextUtils.isEmpty(courseStart)) {
            editCourseStart.setError(getString(R.string.error_select_course_start));
            valid = false;
        }

        if (TextUtils.isEmpty(courseEnd)) {
            editCourseEnd.setError(getString(R.string.error_select_course_end));
            valid = false;
        }

        if (!TextUtils.isEmpty(enrollmentStart)
                && !TextUtils.isEmpty(enrollmentEnd)
                && enrollmentStart.compareTo(enrollmentEnd) >= 0) {
            editEnrollmentEnd.setError(getString(R.string.error_invalid_enrollment_period));
            valid = false;
        }

        if (!TextUtils.isEmpty(courseStart)
                && !TextUtils.isEmpty(courseEnd)
                && courseStart.compareTo(courseEnd) >= 0) {
            editCourseEnd.setError(getString(R.string.error_invalid_course_period));
            valid = false;
        }

        if (!TextUtils.isEmpty(enrollmentEnd)
                && !TextUtils.isEmpty(courseStart)
                && enrollmentEnd.compareTo(courseStart) > 0) {
            editEnrollmentEnd.setError(getString(R.string.error_enrollment_before_course));
            valid = false;
        }

        if (!TextUtils.isEmpty(semester)
                && !TextUtils.isEmpty(academicYear)
                && !TextUtils.isEmpty(courseStart)
                && !isAcademicPeriodValid(semester, academicYear, courseStart)) {
            editCourseStart.setError(getString(R.string.error_invalid_academic_period));
            valid = false;
        }

        return valid;
    }

    private boolean isAcademicPeriodValid(String semester, String academicYear, String courseStart) {
        return AcademicPeriodDefaults.isAcademicPeriodValid(semester, academicYear, courseStart);
    }

    private void saveCourse(final String title,
                            final String code,
                            final String description,
                            final String section,
                            final String studyYear,
                            final String credits,
                            final String semester,
                            final String academicYear,
                            final String enrollmentStart,
                            final String enrollmentEnd,
                            final String courseStart,
                            final String courseEnd) {

        setLoading(true);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADD_COURSE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Add course response: " + response);

                        try {
                            JSONObject object = new JSONObject(response);
                            boolean success = object.optBoolean("success", false);
                            String message = object.optString("message", getString(R.string.error_adding_course));

                            Toast.makeText(AddCourseActivity.this, message, Toast.LENGTH_LONG).show();

                            if (success) {
                                String courseId = object.optString("course_id", "");

                                if (courseId.length() == 0) {
                                    setLoading(false);
                                    Toast.makeText(
                                            AddCourseActivity.this,
                                            getString(R.string.error_adding_schedule),
                                            Toast.LENGTH_LONG
                                    ).show();
                                    return;
                                }

                                saveSchedulesOneByOne(courseId, 0);
                            } else {
                                setLoading(false);
                            }

                        } catch (Exception e) {
                            setLoading(false);
                            Log.e(TAG, "Course parse error", e);
                            Toast.makeText(
                                    AddCourseActivity.this,
                                    getString(R.string.error_adding_course),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setLoading(false);
                        Toast.makeText(AddCourseActivity.this, getErrorMessage(error), Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("title", title);
                params.put("code", code);
                params.put("description", description);
                params.put("section", section);
                params.put("department", clean(selectedMajor.getName()));

                params.put("faculty_id", facultyId);
                params.put("major_id", clean(selectedMajor.getMajorId()));

                params.put("instructor_id", "");
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

        queue.add(request);
    }

    private void saveSchedulesOneByOne(final String courseId, final int index) {
        if (index >= scheduleList.size()) {
            setLoading(false);

            Toast.makeText(
                    this,
                    getString(R.string.schedule_added_successfully),
                    Toast.LENGTH_SHORT
            ).show();

            setResult(RESULT_OK);
            finish();
            return;
        }

        CourseScheduleInputModel schedule = scheduleList.get(index);

        saveOneSchedule(
                courseId,
                clean(schedule.getDayOfWeek()),
                clean(schedule.getStartTime()),
                clean(schedule.getEndTime()),
                clean(schedule.getRoom()),
                index
        );
    }

    private void saveOneSchedule(final String courseId,
                                 final String day,
                                 final String start,
                                 final String end,
                                 final String room,
                                 final int index) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADD_COURSE_SCHEDULE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Add schedule response: " + response);

                        try {
                            JSONObject object = new JSONObject(response);

                            if (object.optBoolean("success", false)) {
                                saveSchedulesOneByOne(courseId, index + 1);
                                return;
                            }

                            setLoading(false);

                            Toast.makeText(
                                    AddCourseActivity.this,
                                    object.optString("message", getString(R.string.error_adding_schedule)),
                                    Toast.LENGTH_LONG
                            ).show();

                        } catch (Exception e) {
                            setLoading(false);
                            Log.e(TAG, "Schedule parse error", e);

                            Toast.makeText(
                                    AddCourseActivity.this,
                                    getString(R.string.error_adding_schedule),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setLoading(false);
                        Toast.makeText(AddCourseActivity.this, getErrorMessage(error), Toast.LENGTH_LONG).show();
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

        queue.add(request);
    }

    private boolean isDuplicateSchedule(String day, String start, String end) {
        for (int i = 0; i < scheduleList.size(); i++) {
            CourseScheduleInputModel item = scheduleList.get(i);

            if (clean(item.getDayOfWeek()).equals(day)
                    && clean(item.getStartTime()).equals(start)
                    && clean(item.getEndTime()).equals(end)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasScheduleConflict(String day, String start, String end) {
        int newStart = timeToMinutes(start);
        int newEnd = timeToMinutes(end);

        for (int i = 0; i < scheduleList.size(); i++) {
            CourseScheduleInputModel item = scheduleList.get(i);

            if (!clean(item.getDayOfWeek()).equals(day)) {
                continue;
            }

            int oldStart = timeToMinutes(clean(item.getStartTime()));
            int oldEnd = timeToMinutes(clean(item.getEndTime()));

            if (newStart < oldEnd && newEnd > oldStart) {
                return true;
            }
        }

        return false;
    }

    private void setLoading(boolean loading) {
        if (progressSave != null) {
            if (loading) {
                progressSave.setVisibility(View.VISIBLE);
            } else {
                progressSave.setVisibility(View.GONE);
            }
        }

        if (buttonSaveCourse != null) {
            buttonSaveCourse.setEnabled(!loading);

            if (loading) {
                buttonSaveCourse.setAlpha(0.6f);
            } else {
                buttonSaveCourse.setAlpha(1f);
            }
        }

        if (buttonAddScheduleTime != null) {
            buttonAddScheduleTime.setEnabled(!loading);

            if (loading) {
                buttonAddScheduleTime.setAlpha(0.6f);
            } else {
                buttonAddScheduleTime.setAlpha(1f);
            }
        }
    }

    private void addBottomSpace(View view, int bottomDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, dp(bottomDp));
        view.setLayoutParams(params);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String getText(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }

    private String getAutoText(AutoCompleteTextView input) {
        if (input == null || input.getText() == null) {
            return "";
        }

        return input.getText().toString().trim();
    }

    private String clean(String value) {
        if (value == null || value.equals("null")) {
            return "";
        }

        return value.trim();
    }

    private boolean isPositiveNumber(String value) {
        try {
            int number = Integer.parseInt(value);
            return number > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String normalizeTime(String value) {
        if (value == null) {
            return "";
        }

        String time = value.trim();

        if (time.matches("^\\d{1}:\\d{2}$")) {
            time = "0" + time;
        }

        if (time.matches("^\\d{2}:\\d{2}$")) {
            return time + ":00";
        }

        return time;
    }

    private boolean isValidTime(String value) {
        if (value == null) {
            return false;
        }

        String time = value.trim();

        if (!time.matches("^\\d{2}:\\d{2}:\\d{2}$")) {
            return false;
        }

        try {
            String[] parts = time.split(":");

            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            int second = Integer.parseInt(parts[2]);

            return hour >= 0 && hour <= 23
                    && minute >= 0 && minute <= 59
                    && second >= 0 && second <= 59;

        } catch (Exception e) {
            return false;
        }
    }

    private int timeToMinutes(String value) {
        if (value == null) {
            return -1;
        }

        String time = value.trim();

        if (time.matches("^\\d{2}:\\d{2}:\\d{2}$")) {
            time = time.substring(0, 5);
        }

        if (time.matches("^\\d{1}:\\d{2}$")) {
            time = "0" + time;
        }

        if (!time.matches("^\\d{2}:\\d{2}$")) {
            return -1;
        }

        try {
            String[] parts = time.split(":");

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

    private String getErrorMessage(VolleyError error) {
        if (error == null) {
            return getString(R.string.error_network);
        }

        if (error.networkResponse != null && error.networkResponse.data != null) {
            return new String(error.networkResponse.data);
        }

        if (error.getMessage() != null) {
            return error.getMessage();
        }

        return getString(R.string.error_network);
    }
}