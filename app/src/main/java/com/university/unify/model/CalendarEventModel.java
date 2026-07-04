package com.university.unify.model;

public class CalendarEventModel {

    private String courseId;
    private String title;
    private String code;
    private String section;
    private String instructorName;
    private String dayName;
    private String timeText;
    private String roomText;
    private String fullScheduleText;

    private String courseStartAt;
    private String courseEndAt;

    public String getCourseStartAt() {
        return courseStartAt;
    }

    public void setCourseStartAt(String courseStartAt) {
        this.courseStartAt = safe(courseStartAt);
    }

    public String getCourseEndAt() {
        return courseEndAt;
    }

    public void setCourseEndAt(String courseEndAt) {
        this.courseEndAt = safe(courseEndAt);
    }

    public CalendarEventModel() {
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = safe(courseId);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = safe(title);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = safe(code);
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = safe(section);
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = safe(instructorName);
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = safe(dayName);
    }

    public String getTimeText() {
        return timeText;
    }

    public void setTimeText(String timeText) {
        this.timeText = safe(timeText);
    }

    public String getRoomText() {
        return roomText;
    }

    public void setRoomText(String roomText) {
        this.roomText = safe(roomText);
    }

    public String getFullScheduleText() {
        return fullScheduleText;
    }

    public void setFullScheduleText(String fullScheduleText) {
        this.fullScheduleText = safe(fullScheduleText);
    }

    private String safe(String value) {
        if (value == null || value.equals("null")) {
            return "";
        }

        return value.trim();
    }
}