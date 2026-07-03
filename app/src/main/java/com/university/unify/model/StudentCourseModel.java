package com.university.unify.model;

public class StudentCourseModel {

    private String courseId;
    private String title;
    private String code;
    private String description;
    private String section;
    private String department;

    private String instructorId;
    private String instructorName;

    private String semester;
    private String academicYear;

    private String credits;
    private String scheduleText;

    private String enrollmentId;
    private String enrollmentStatus;
    private String enrolledAt;

    private String result;
    private String finalGrade;
    private boolean resultPublished;

    private String enrollmentStartAt;
    private String enrollmentEndAt;
    private String courseStartAt;
    private String courseEndAt;

    private String courseStatus;
    private String calculatedStatus;

    public StudentCourseModel() {
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(String instructorId) {
        this.instructorId = instructorId;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public String getScheduleText() {
        return scheduleText;
    }

    public void setScheduleText(String scheduleText) {
        this.scheduleText = scheduleText;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }

    public String getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(String enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(String finalGrade) {
        this.finalGrade = finalGrade;
    }

    public boolean isResultPublished() {
        return resultPublished;
    }

    public void setResultPublished(boolean resultPublished) {
        this.resultPublished = resultPublished;
    }

    public String getEnrollmentStartAt() {
        return enrollmentStartAt;
    }

    public void setEnrollmentStartAt(String enrollmentStartAt) {
        this.enrollmentStartAt = enrollmentStartAt;
    }

    public String getEnrollmentEndAt() {
        return enrollmentEndAt;
    }

    public void setEnrollmentEndAt(String enrollmentEndAt) {
        this.enrollmentEndAt = enrollmentEndAt;
    }

    public String getCourseStartAt() {
        return courseStartAt;
    }

    public void setCourseStartAt(String courseStartAt) {
        this.courseStartAt = courseStartAt;
    }

    public String getCourseEndAt() {
        return courseEndAt;
    }

    public void setCourseEndAt(String courseEndAt) {
        this.courseEndAt = courseEndAt;
    }

    public String getCourseStatus() {
        return courseStatus;
    }

    public void setCourseStatus(String courseStatus) {
        this.courseStatus = courseStatus;
    }

    public String getCalculatedStatus() {
        return calculatedStatus;
    }

    public void setCalculatedStatus(String calculatedStatus) {
        this.calculatedStatus = calculatedStatus;
    }

    public String getBestCourseStatus() {
        if (calculatedStatus != null && !calculatedStatus.trim().isEmpty()) {
            return calculatedStatus;
        }

        if (courseStatus != null && !courseStatus.trim().isEmpty()) {
            return courseStatus;
        }

        return "";
    }
}