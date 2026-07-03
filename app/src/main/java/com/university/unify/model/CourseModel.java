package com.university.unify.model;

public class CourseModel {

    private String courseId;
    private String title;
    private String code;
    private String description;
    private String section;
    private String department;

    private String facultyId;
    private String facultyName;

    private String majorId;
    private String majorName;
    private String studyYear;

    private int credits;

    private String instructorId;
    private String instructorName;

    private String semester;
    private String academicYear;

    private String enrollmentStartAt;
    private String enrollmentEndAt;
    private String courseStartAt;
    private String courseEndAt;

    private String status;
    private String courseStatus;
    private String calculatedStatus;

    private String scheduleText;

    private String studentsChatId;
    private String mainChatId;

    private long createdAt;
    private long updatedAt;

    public CourseModel() {
    }

    public CourseModel(String courseId,
                       String title,
                       String code,
                       String description,
                       String section,
                       String department,
                       String facultyId,
                       String facultyName,
                       String majorId,
                       String studyYear,
                       int credits,
                       String instructorId,
                       String semester,
                       String academicYear,
                       String studentsChatId,
                       String mainChatId,
                       long createdAt,
                       long updatedAt) {
        this.courseId = courseId;
        this.title = title;
        this.code = code;
        this.description = description;
        this.section = section;
        this.department = department;
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.majorId = majorId;
        this.studyYear = studyYear;
        this.credits = credits;
        this.instructorId = instructorId;
        this.semester = semester;
        this.academicYear = academicYear;
        this.studentsChatId = studentsChatId;
        this.mainChatId = mainChatId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getMajorId() {
        return majorId;
    }

    public void setMajorId(String majorId) {
        this.majorId = majorId;
    }

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public String getStudyYear() {
        return studyYear;
    }

    public void setStudyYear(String studyYear) {
        this.studyYear = studyYear;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getBestStatus() {
        if (calculatedStatus != null && !calculatedStatus.trim().isEmpty()) {
            return calculatedStatus;
        }

        if (courseStatus != null && !courseStatus.trim().isEmpty()) {
            return courseStatus;
        }

        if (status != null && !status.trim().isEmpty()) {
            return status;
        }

        return "";
    }

    public String getScheduleText() {
        return scheduleText;
    }

    public void setScheduleText(String scheduleText) {
        this.scheduleText = scheduleText;
    }

    public String getStudentsChatId() {
        return studentsChatId;
    }

    public void setStudentsChatId(String studentsChatId) {
        this.studentsChatId = studentsChatId;
    }

    public String getMainChatId() {
        return mainChatId;
    }

    public void setMainChatId(String mainChatId) {
        this.mainChatId = mainChatId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}