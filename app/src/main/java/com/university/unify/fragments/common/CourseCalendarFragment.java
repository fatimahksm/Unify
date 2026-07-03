package com.university.unify.fragments.common;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.model.CalendarEventModel;
import com.university.unify.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CourseCalendarFragment extends Fragment {

    private static final String REQUEST_TAG_CALENDAR_COURSES   = "REQUEST_TAG_CALENDAR_COURSES";
    private static final String REQUEST_TAG_CALENDAR_SCHEDULES = "REQUEST_TAG_CALENDAR_SCHEDULES";

    private TextView    textTitle;
    private TextView    textSubtitle;
    private TextView    textMonthYear;
    private TextView    buttonPreviousMonth;
    private TextView    buttonNextMonth;
    private TextView    textEmpty;
    private ProgressBar progressBar;
    private GridLayout  gridDayNames;
    private GridLayout  gridCalendar;

    private TextView textTodayCoursesCount;
    private TextView textTotalCoursesCount;
    private TextView textActiveDaysCount;

    private RequestQueue queue;
    private final Calendar currentMonth = Calendar.getInstance();
    private final List<CalendarEventModel> allEvents = new ArrayList<>();

    private String userId = "";
    private String role   = "";

    public CourseCalendarFragment() {
        super(R.layout.fragment_course_calendar);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queue = Volley.newRequestQueue(requireContext());

        initViews(view);
        setupClickListeners();
        setupDayNames();
        loadUserData();
    }

    private void initViews(View view) {
        textTitle           = view.findViewById(R.id.textCalendarTitle);
        textSubtitle        = view.findViewById(R.id.textCalendarSubtitle);
        textMonthYear       = view.findViewById(R.id.textMonthYear);
        buttonPreviousMonth = view.findViewById(R.id.buttonPreviousMonth);
        buttonNextMonth     = view.findViewById(R.id.buttonNextMonth);
        textEmpty           = view.findViewById(R.id.textCalendarEmpty);
        progressBar         = view.findViewById(R.id.progressCalendar);
        gridDayNames        = view.findViewById(R.id.gridDayNames);
        gridCalendar        = view.findViewById(R.id.gridCalendar);

        textTodayCoursesCount = view.findViewById(R.id.textTodayCoursesCount);
        textTotalCoursesCount = view.findViewById(R.id.textTotalCoursesCount);
        textActiveDaysCount   = view.findViewById(R.id.textActiveDaysCount);
    }

    private void setupClickListeners() {
        buttonPreviousMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            buildCalendar();
        });

        buttonNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            buildCalendar();
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Day-name header row — localised
    // ─────────────────────────────────────────────────────────────────────────

    private void setupDayNames() {
        if (!isFragmentReady()) return;

        gridDayNames.removeAllViews();

        // Use the string array defined in strings.xml / strings-ar.xml
        // so letters flip automatically when Arabic is active.
        String[] days = requireContext().getResources()
                .getStringArray(R.array.calendar_day_initials);

        for (String day : days) {
            TextView tv = new TextView(requireContext());
            tv.setText(day);
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.unify_text_secondary));
            tv.setTextSize(12);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width       = 0;
            params.height      = dp(32);
            params.columnSpec  = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(dp(2), dp(2), dp(2), dp(2));

            tv.setLayoutParams(params);
            gridDayNames.addView(tv);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Load
    // ─────────────────────────────────────────────────────────────────────────

    private void loadUserData() {
        if (!isFragmentReady()) return;

        DatabaseHelper db = new DatabaseHelper(requireContext());
        userId = safe(db.getLoggedInUserId());
        role   = safe(db.getLoggedInRole()).toUpperCase(Locale.ROOT);

        if (TextUtils.isEmpty(userId)) {
            showEmpty(getString(R.string.error_user_session_not_found));
            buildCalendar();
            return;
        }

        if (role.equals("INSTRUCTOR")) {
            textTitle.setText(R.string.calendar_title_instructor);
            textSubtitle.setText(R.string.calendar_subtitle_instructor);
            loadInstructorCourses();
        } else {
            textTitle.setText(R.string.calendar_title_student);
            textSubtitle.setText(R.string.calendar_subtitle_student);
            loadStudentCourses();
        }
    }

    private void loadStudentCourses() {
        if (!isFragmentReady()) return;

        showLoading(true);

        String url = ApiConfig.GET_STUDENT_COURSES + "?student_id=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    if (!isFragmentReady()) return;
                    parseCourses(response);
                },
                error -> {
                    if (!isFragmentReady()) return;
                    showLoading(false);
                    showEmpty(getString(R.string.error_failed_to_load_calendar));
                    buildCalendar();
                }
        );

        request.setTag(REQUEST_TAG_CALENDAR_COURSES);
        queue.add(request);
    }

    private void loadInstructorCourses() {
        if (!isFragmentReady()) return;

        showLoading(true);

        String url = ApiConfig.GET_INSTRUCTOR_COURSES + "?instructor_id=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    if (!isFragmentReady()) return;
                    parseCourses(response);
                },
                error -> {
                    if (!isFragmentReady()) return;
                    showLoading(false);
                    showEmpty(getString(R.string.error_failed_to_load_calendar));
                    buildCalendar();
                }
        );

        request.setTag(REQUEST_TAG_CALENDAR_COURSES);
        queue.add(request);
    }

    private void parseCourses(JSONObject response) {
        if (!isFragmentReady()) return;

        try {
            allEvents.clear();
            resetSummaryCards();

            if (!response.optBoolean("success", false)) {
                showLoading(false);
                showEmpty(response.optString("message",
                        getString(R.string.error_no_calendar_data)));
                buildCalendar();
                return;
            }

            /*
             * Student calendar: show only active courses (in-progress).
             * Backend returns: data + active_courses + completed_courses.
             * Using "data" would show completed courses too — wrong.
             */
            JSONArray courses = role.equals("STUDENT")
                    ? response.optJSONArray("active_courses")
                    : response.optJSONArray("data");

            if (courses == null || courses.length() == 0) {
                showLoading(false);
                showEmpty(getString(R.string.error_no_active_courses));
                buildCalendar();
                return;
            }

            loadSchedulesForCourses(courses);

        } catch (Exception e) {
            showLoading(false);
            showEmpty(getString(R.string.error_reading_calendar));
            buildCalendar();
        }
    }

    private void loadSchedulesForCourses(JSONArray courses) {
        if (!isFragmentReady()) return;

        final int total = courses.length();
        final int[] finished = {0};

        if (total == 0) {
            showLoading(false);
            showEmpty(getString(R.string.error_no_courses_found));
            buildCalendar();
            return;
        }

        for (int i = 0; i < total; i++) {
            final JSONObject course = courses.optJSONObject(i);

            if (course == null) {
                if (++finished[0] >= total) finishCalendarLoading();
                continue;
            }

            String courseId = safe(course.optString("course_id", ""));

            if (TextUtils.isEmpty(courseId)) {
                if (++finished[0] >= total) finishCalendarLoading();
                continue;
            }

            String url = ApiConfig.GET_COURSE_SCHEDULES + "?course_id=" + courseId;

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.GET, url, null,
                    response -> {
                        if (!isFragmentReady()) return;
                        addRawSchedulesToEvents(course, response);
                        if (++finished[0] >= total) finishCalendarLoading();
                    },
                    error -> {
                        if (!isFragmentReady()) return;
                        if (++finished[0] >= total) finishCalendarLoading();
                    }
            );

            req.setTag(REQUEST_TAG_CALENDAR_SCHEDULES);
            queue.add(req);
        }
    }

    private void finishCalendarLoading() {
        if (!isFragmentReady()) return;

        showLoading(false);

        if (allEvents.isEmpty()) {
            showEmpty(getString(R.string.error_no_schedules_found));
        } else {
            textEmpty.setVisibility(View.GONE);
        }

        updateSummaryCards();
        buildCalendar();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Schedule parsing
    // ─────────────────────────────────────────────────────────────────────────

    private void addRawSchedulesToEvents(JSONObject course, JSONObject response) {
        try {
            JSONArray schedules = findSchedulesArray(response);
            if (schedules == null || schedules.length() == 0) return;

            for (int i = 0; i < schedules.length(); i++) {
                JSONObject schedule = schedules.optJSONObject(i);
                if (schedule == null) continue;

                String day = getFirstValue(schedule,
                        "day_of_week", "dayOfWeek", "day",
                        "day_name", "dayName", "day_label", "dayLabel");

                String normalizedDay = normalizeDay(day);
                if (TextUtils.isEmpty(normalizedDay)) continue;

                String startTime = getFirstValue(schedule,
                        "start_time", "startTime", "from_time",
                        "fromTime", "start", "startHour", "start_hour");

                String endTime = getFirstValue(schedule,
                        "end_time", "endTime", "to_time",
                        "toTime", "end", "endHour", "end_hour");

                String room = getFirstValue(schedule,
                        "room", "room_name", "roomName",
                        "classroom", "location", "class_room", "classRoom");

                CalendarEventModel event = new CalendarEventModel();
                event.setCourseId(course.optString("course_id", ""));
                event.setTitle(course.optString("title", ""));
                event.setCode(course.optString("code", ""));
                event.setSection(course.optString("section", ""));
                event.setInstructorName(course.optString("instructor_name", ""));
                event.setDayName(normalizedDay);

                event.setTimeText((!TextUtils.isEmpty(startTime) && !TextUtils.isEmpty(endTime))
                        ? trimSeconds(startTime) + "-" + trimSeconds(endTime)
                        : "");

                // FIX: use getString so "Room:" translates when Arabic is active
                event.setRoomText(!TextUtils.isEmpty(room)
                        ? getString(R.string.room_prefix, room)
                        : "");

                event.setFullScheduleText(
                        normalizedDay + " " +
                                trimSeconds(startTime) + "-" +
                                trimSeconds(endTime) + " " + room);

                allEvents.add(event);
            }

        } catch (Exception ignored) { }
    }

    private JSONArray findSchedulesArray(JSONObject response) {
        JSONArray arr;

        arr = response.optJSONArray("data");
        if (arr != null) return arr;

        arr = response.optJSONArray("schedules");
        if (arr != null) return arr;

        arr = response.optJSONArray("course_schedules");
        if (arr != null) return arr;

        JSONObject dataObj = response.optJSONObject("data");
        if (dataObj != null) {
            arr = dataObj.optJSONArray("schedules");
            if (arr != null) return arr;

            arr = dataObj.optJSONArray("course_schedules");
            if (arr != null) return arr;
        }

        return null;
    }

    private String getFirstValue(JSONObject obj,
                                 String k1, String k2, String k3,
                                 String k4, String k5, String k6, String k7) {

        for (String key : new String[]{k1, k2, k3, k4, k5, k6, k7}) {
            String v = safe(obj.optString(key, ""));
            if (!TextUtils.isEmpty(v)) return v;
        }
        return "";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Calendar grid
    // ─────────────────────────────────────────────────────────────────────────

    private void buildCalendar() {
        if (!isFragmentReady()) return;

        gridCalendar.removeAllViews();

        // FIX: use device locale so the month name translates (e.g. "أكتوبر 2025")
        Locale locale = getResources().getConfiguration().getLocales().get(0);
        SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", locale);
        textMonthYear.setText(format.format(currentMonth.getTime()));

        Calendar monthStart = (Calendar) currentMonth.clone();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = monthStart.get(Calendar.DAY_OF_WEEK);
        int daysInMonth    = monthStart.getActualMaximum(Calendar.DAY_OF_MONTH);
        int totalCells     = 42;
        int dayNumber      = 1;
        Calendar today     = Calendar.getInstance();

        for (int cell = 1; cell <= totalCells; cell++) {
            LinearLayout dayCell = createDayCell();

            if (cell >= firstDayOfWeek && dayNumber <= daysInMonth) {
                Calendar date = (Calendar) currentMonth.clone();
                date.set(Calendar.DAY_OF_MONTH, dayNumber);
                fillDayCell(dayCell, date, dayNumber, today);
                dayNumber++;
            }

            gridCalendar.addView(dayCell);
        }
    }

    private LinearLayout createDayCell() {
        LinearLayout cell = new LinearLayout(requireContext());
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setPadding(dp(4), dp(5), dp(4), dp(4));
        cell.setGravity(Gravity.TOP);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width      = 0;
        params.height     = dp(96);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dp(3), dp(3), dp(3), dp(3));
        cell.setLayoutParams(params);

        applyDayCellBackground(cell, false, false);
        return cell;
    }

    private void fillDayCell(LinearLayout cell, Calendar date, int dayNumber, Calendar today) {
        String dayName = getDayName(date);
        List<CalendarEventModel> events = getEventsForDay(dayName);

        boolean hasCourses = events != null && !events.isEmpty();
        boolean isToday    = isSameDay(date, today);

        applyDayCellBackground(cell, hasCourses, isToday);

        // Day number circle
        TextView textDayNumber = new TextView(requireContext());
        textDayNumber.setText(String.valueOf(dayNumber));
        textDayNumber.setTextSize(12);
        textDayNumber.setTypeface(null, android.graphics.Typeface.BOLD);
        textDayNumber.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        dayParams.gravity = Gravity.CENTER_HORIZONTAL;
        textDayNumber.setLayoutParams(dayParams);

        if (isToday) {
            textDayNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            textDayNumber.setBackground(makeCircleDrawable(R.color.unify_blue));
        } else if (hasCourses) {
            textDayNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.unify_blue));
            textDayNumber.setBackground(makeCircleDrawable(R.color.white));
        } else {
            textDayNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.unify_text_primary));
        }

        cell.addView(textDayNumber);

        // Event chips (max 2)
        int maxChips = 2;
        for (int i = 0; i < events.size() && i < maxChips; i++) {
            cell.addView(createEventChip(events.get(i)));
        }

        // Overflow indicator
        if (events.size() > maxChips) {
            TextView more = new TextView(requireContext());
            more.setText("+" + (events.size() - maxChips));
            more.setGravity(Gravity.CENTER);
            more.setTextColor(ContextCompat.getColor(requireContext(), R.color.unify_text_secondary));
            more.setTextSize(9);

            LinearLayout.LayoutParams moreParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            moreParams.setMargins(0, dp(3), 0, 0);
            more.setLayoutParams(moreParams);

            cell.addView(more);
        }

        final List<CalendarEventModel> dayEvents = new ArrayList<>(events);
        final int                      dayNum    = dayNumber;
        final String                   dayNm     = dayName;

        cell.setOnClickListener(v -> showDayDialog(dayNum, dayNm, dayEvents));
    }

    private void applyDayCellBackground(LinearLayout cell, boolean hasCourses, boolean isToday) {
        if (!isFragmentReady()) return;

        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(dp(14));

        if (isToday && hasCourses) {
            d.setColor(ContextCompat.getColor(requireContext(), R.color.unify_gold_light));
            d.setStroke(dp(2), ContextCompat.getColor(requireContext(), R.color.unify_blue));
        } else if (isToday) {
            d.setColor(ContextCompat.getColor(requireContext(), R.color.unify_surface));
            d.setStroke(dp(2), ContextCompat.getColor(requireContext(), R.color.unify_blue));
        } else if (hasCourses) {
            d.setColor(ContextCompat.getColor(requireContext(), R.color.unify_gold_light));
            d.setStroke(dp(1), ContextCompat.getColor(requireContext(), R.color.unify_gold));
        } else {
            d.setColor(ContextCompat.getColor(requireContext(), R.color.unify_surface));
            d.setStroke(dp(1), ContextCompat.getColor(requireContext(), R.color.unify_border));
        }

        cell.setBackground(d);
    }

    private TextView createEventChip(CalendarEventModel event) {
        TextView chip = new TextView(requireContext());

        String label = safe(event.getCode());
        if (TextUtils.isEmpty(label)) label = safe(event.getTitle());
        if (!TextUtils.isEmpty(event.getTimeText()))
            label = event.getTimeText() + " " + label;

        chip.setText(label);
        chip.setSingleLine(true);
        chip.setEllipsize(TextUtils.TruncateAt.END);
        chip.setTextSize(8);
        chip.setGravity(Gravity.CENTER);
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        chip.setBackground(makeRoundedDrawable(R.color.unify_blue, 9));
        chip.setPadding(dp(4), dp(2), dp(4), dp(2));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(18));
        params.setMargins(0, dp(5), 0, 0);
        chip.setLayoutParams(params);

        return chip;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Day dialog — all labels localised
    // ─────────────────────────────────────────────────────────────────────────

    private void showDayDialog(int dayNumber, String dayName, List<CalendarEventModel> events) {
        if (!isFragmentReady()) return;

        android.app.AlertDialog.Builder builder =
                new android.app.AlertDialog.Builder(requireContext());

        builder.setTitle(formatDialogTitle(dayName, dayNumber));

        if (events == null || events.isEmpty()) {
            builder.setMessage(getString(R.string.calendar_no_courses_today));
        } else {
            StringBuilder msg = new StringBuilder();

            for (int i = 0; i < events.size(); i++) {
                CalendarEventModel event = events.get(i);

                msg.append("📘 ");

                if (!TextUtils.isEmpty(event.getTimeText()))
                    msg.append(event.getTimeText()).append("\n");

                msg.append(event.getTitle());

                if (!TextUtils.isEmpty(event.getCode()))
                    msg.append(" (").append(event.getCode()).append(")");

                if (!TextUtils.isEmpty(event.getSection()))
                    msg.append("\n").append(getString(R.string.section_label))
                            .append(event.getSection());

                if (!TextUtils.isEmpty(event.getRoomText()))
                    msg.append("\n").append(event.getRoomText());

                if (!TextUtils.isEmpty(event.getInstructorName()))
                    msg.append("\n").append(getString(R.string.instructor_label))
                            .append(event.getInstructorName());

                if (i < events.size() - 1) msg.append("\n\n");
            }

            builder.setMessage(msg.toString());
        }

        builder.setPositiveButton(R.string.ok, null);
        builder.show();
    }

    private String formatDialogTitle(String dayName, int dayNumber) {
        String clean = safe(dayName).toLowerCase(Locale.ROOT);
        if (!clean.isEmpty())
            clean = clean.substring(0, 1).toUpperCase(Locale.ROOT) + clean.substring(1);
        return clean + ", " + dayNumber;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Summary cards
    // ─────────────────────────────────────────────────────────────────────────

    private void updateSummaryCards() {
        if (!isFragmentReady()) return;

        Calendar today    = Calendar.getInstance();
        String todayName  = getDayName(today);
        int todayCourses  = getEventsForDay(todayName).size();
        int totalCourses  = countUniqueCourses();
        int activeDays    = countActiveDays();

        if (textTodayCoursesCount != null) textTodayCoursesCount.setText(String.valueOf(todayCourses));
        if (textTotalCoursesCount != null) textTotalCoursesCount.setText(String.valueOf(totalCourses));
        if (textActiveDaysCount   != null) textActiveDaysCount.setText(String.valueOf(activeDays));
    }

    private void resetSummaryCards() {
        if (textTodayCoursesCount != null) textTodayCoursesCount.setText("0");
        if (textTotalCoursesCount != null) textTotalCoursesCount.setText("0");
        if (textActiveDaysCount   != null) textActiveDaysCount.setText("0");
    }

    private int countUniqueCourses() {
        List<String> ids = new ArrayList<>();
        for (CalendarEventModel e : allEvents) {
            String id = safe(e.getCourseId());
            if (!TextUtils.isEmpty(id) && !ids.contains(id)) ids.add(id);
        }
        return ids.size();
    }

    private int countActiveDays() {
        List<String> days = new ArrayList<>();
        for (CalendarEventModel e : allEvents) {
            String d = safe(e.getDayName());
            if (!TextUtils.isEmpty(d) && !days.contains(d)) days.add(d);
        }
        return days.size();
    }

    private List<CalendarEventModel> getEventsForDay(String dayName) {
        List<CalendarEventModel> events = new ArrayList<>();
        for (CalendarEventModel e : allEvents) {
            if (safe(e.getDayName()).equalsIgnoreCase(dayName)) events.add(e);
        }
        return events;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Day-name utilities (internal — always English for map key matching)
    // ─────────────────────────────────────────────────────────────────────────

    private String getDayName(Calendar calendar) {
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:    return "MONDAY";
            case Calendar.TUESDAY:   return "TUESDAY";
            case Calendar.WEDNESDAY: return "WEDNESDAY";
            case Calendar.THURSDAY:  return "THURSDAY";
            case Calendar.FRIDAY:    return "FRIDAY";
            case Calendar.SATURDAY:  return "SATURDAY";
            default:                 return "SUNDAY";
        }
    }

    private String normalizeDay(String value) {
        String u = safe(value).toUpperCase(Locale.ROOT);

        if (u.equals("1") || u.equals("SUNDAY")    || u.equals("SUN")) return "SUNDAY";
        if (u.equals("2") || u.equals("MONDAY")    || u.equals("MON")) return "MONDAY";
        if (u.equals("3") || u.equals("TUESDAY")   || u.equals("TUE")) return "TUESDAY";
        if (u.equals("4") || u.equals("WEDNESDAY") || u.equals("WED")) return "WEDNESDAY";
        if (u.equals("5") || u.equals("THURSDAY")  || u.equals("THU")) return "THURSDAY";
        if (u.equals("6") || u.equals("FRIDAY")    || u.equals("FRI")) return "FRIDAY";
        if (u.equals("7") || u.equals("SATURDAY")  || u.equals("SAT")) return "SATURDAY";

        return "";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Small utilities
    // ─────────────────────────────────────────────────────────────────────────

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR)         == b.get(Calendar.YEAR)
                && a.get(Calendar.MONTH)        == b.get(Calendar.MONTH)
                && a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH);
    }

    private String trimSeconds(String time) {
        String clean = safe(time);
        return clean.length() >= 5 ? clean.substring(0, 5) : clean;
    }

    private GradientDrawable makeCircleDrawable(int colorRes) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(ContextCompat.getColor(requireContext(), colorRes));
        return d;
    }

    private GradientDrawable makeRoundedDrawable(int colorRes, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(dp(radiusDp));
        d.setColor(ContextCompat.getColor(requireContext(), colorRes));
        return d;
    }

    private void showLoading(boolean loading) {
        if (!isFragmentReady()) return;
        if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading && textEmpty != null) textEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        if (!isFragmentReady()) return;
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (textEmpty != null) {
            textEmpty.setVisibility(View.VISIBLE);
            textEmpty.setText(message);
        }
    }

    private boolean isFragmentReady() {
        return isAdded() && getContext() != null && getView() != null;
    }

    private String safe(String value) {
        if (value == null || value.equals("null")) return "";
        return value.trim();
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }

    @Override
    public void onDestroyView() {
        if (queue != null) {
            queue.cancelAll(REQUEST_TAG_CALENDAR_COURSES);
            queue.cancelAll(REQUEST_TAG_CALENDAR_SCHEDULES);
        }
        super.onDestroyView();
    }
}