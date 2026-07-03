package com.university.unify.model;

public class CourseScheduleInputModel {

    private String dayOfWeek;
    private String dayLabel;
    private String startTime;
    private String endTime;
    private String room;

    public CourseScheduleInputModel() {
    }

    public CourseScheduleInputModel(String dayOfWeek, String dayLabel, String startTime, String endTime, String room) {
        this.dayOfWeek = dayOfWeek;
        this.dayLabel = dayLabel;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDayLabel() {
        return dayLabel;
    }

    public void setDayLabel(String dayLabel) {
        this.dayLabel = dayLabel;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}