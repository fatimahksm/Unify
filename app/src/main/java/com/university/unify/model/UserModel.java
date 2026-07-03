package com.university.unify.model;

public class UserModel {

    private String firstName;
    private String lastName;
    private String email;
    private String role;

    private String facultyId;
    private String facultyName;

    private String majorId;
    private String majorName;

    private String studyYear;

    private String phone;
    private String profileImageUrl;

    private boolean active;
    private boolean approved;
    private boolean createdByAdmin;

    private String studentNumber;

    private int allowedCredits = 18;
    private String employeeId;

    private long createdAt;
    private long updatedAt;

    public UserModel() {
    }

    public UserModel(String firstName,
                     String lastName,
                     String email,
                     String role,
                     String facultyId,
                     String facultyName,
                     String majorId,
                     String majorName,
                     String studyYear,
                     String phone,
                     String profileImageUrl,
                     boolean active,
                     boolean approved,
                     boolean createdByAdmin,
                     String studentNumber,
                     String employeeId,
                     long createdAt,
                     long updatedAt) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.majorId = majorId;
        this.majorName = majorName;
        this.studyYear = studyYear;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
        this.active = active;
        this.approved = approved;
        this.createdByAdmin = createdByAdmin;
        this.studentNumber = studentNumber;
        this.employeeId = employeeId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isCreatedByAdmin() {
        return createdByAdmin;
    }

    public void setCreatedByAdmin(boolean createdByAdmin) {
        this.createdByAdmin = createdByAdmin;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public int getAllowedCredits() {
        return allowedCredits;
    }

    public void setAllowedCredits(int allowedCredits) {
        this.allowedCredits = allowedCredits;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
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