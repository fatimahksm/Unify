package com.university.unify.utils;

import java.util.Calendar;
import java.util.Locale;

public class AcademicPeriodDefaults {

    private static final int ENROLLMENT_CLOSE_BEFORE_COURSE_DAYS = 21;
    private static final int DEFAULT_ENROLLMENT_DURATION_DAYS = 30;

    private AcademicPeriodDefaults() {
        // Utility class.
    }

    public static PeriodDates getDefaultDates(String semester, String academicYear) {
        int expectedYear = getExpectedCourseYear(semester, academicYear);

        if (expectedYear <= 0) {
            return null;
        }

        String cleanSemester = clean(semester).toUpperCase(Locale.ROOT);

        Calendar courseStart = Calendar.getInstance();
        Calendar courseEnd = Calendar.getInstance();

        if (cleanSemester.equals("FALL")) {
            courseStart = createDate(expectedYear, 10, 1, 0, 0, 0);
            courseEnd = createDate(expectedYear + 1, 1, 31, 23, 59, 0);
        } else if (cleanSemester.equals("SPRING")) {
            courseStart = createDate(expectedYear, 3, 1, 0, 0, 0);
            courseEnd = createDate(expectedYear, 6, 30, 23, 59, 0);
        } else if (cleanSemester.equals("SUMMER")) {
            courseStart = createDate(expectedYear, 6, 1, 0, 0, 0);
            courseEnd = createDate(expectedYear, 8, 15, 23, 59, 0);
        } else {
            return null;
        }

        Calendar enrollmentEnd = (Calendar) courseStart.clone();
        enrollmentEnd.add(Calendar.DAY_OF_MONTH, -ENROLLMENT_CLOSE_BEFORE_COURSE_DAYS);
        enrollmentEnd.set(Calendar.HOUR_OF_DAY, 23);
        enrollmentEnd.set(Calendar.MINUTE, 59);
        enrollmentEnd.set(Calendar.SECOND, 0);

        Calendar enrollmentStart = (Calendar) enrollmentEnd.clone();
        enrollmentStart.add(Calendar.DAY_OF_MONTH, -DEFAULT_ENROLLMENT_DURATION_DAYS);
        enrollmentStart.set(Calendar.HOUR_OF_DAY, 0);
        enrollmentStart.set(Calendar.MINUTE, 0);
        enrollmentStart.set(Calendar.SECOND, 0);

        return new PeriodDates(
                formatDateTime(enrollmentStart),
                formatDateTime(enrollmentEnd),
                formatDateTime(courseStart),
                formatDateTime(courseEnd)
        );
    }

    public static int getExpectedCourseYear(String semester, String academicYear) {
        String cleanSemester = clean(semester).toUpperCase(Locale.ROOT);
        String cleanYear = clean(academicYear);

        String[] parts = cleanYear.split("/");

        if (parts.length != 2) {
            return -1;
        }

        try {
            int firstYear = Integer.parseInt(parts[0]);
            int secondYear = Integer.parseInt(parts[1]);

            if (cleanSemester.equals("FALL")) {
                return firstYear;
            }

            if (cleanSemester.equals("SPRING") || cleanSemester.equals("SUMMER")) {
                return secondYear;
            }

        } catch (Exception e) {
            return -1;
        }

        return -1;
    }

    public static boolean isAcademicPeriodValid(String semester, String academicYear, String courseStart) {
        int expectedYear = getExpectedCourseYear(semester, academicYear);

        if (expectedYear <= 0 || courseStart == null || courseStart.length() < 4) {
            return false;
        }

        try {
            int actualYear = Integer.parseInt(courseStart.substring(0, 4));
            return actualYear == expectedYear;
        } catch (Exception e) {
            return false;
        }
    }

    private static Calendar createDate(int year,
                                       int month,
                                       int day,
                                       int hour,
                                       int minute,
                                       int second) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    private static String formatDateTime(Calendar calendar) {
        return String.format(
                Locale.ROOT,
                "%04d-%02d-%02d %02d:%02d:%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)
        );
    }

    private static String clean(String value) {
        if (value == null || value.equalsIgnoreCase("null")) {
            return "";
        }

        return value.trim();
    }

    public static class PeriodDates {

        private final String enrollmentStart;
        private final String enrollmentEnd;
        private final String courseStart;
        private final String courseEnd;

        public PeriodDates(String enrollmentStart,
                           String enrollmentEnd,
                           String courseStart,
                           String courseEnd) {
            this.enrollmentStart = enrollmentStart;
            this.enrollmentEnd = enrollmentEnd;
            this.courseStart = courseStart;
            this.courseEnd = courseEnd;
        }

        public String getEnrollmentStart() {
            return enrollmentStart;
        }

        public String getEnrollmentEnd() {
            return enrollmentEnd;
        }

        public String getCourseStart() {
            return courseStart;
        }

        public String getCourseEnd() {
            return courseEnd;
        }
    }
}