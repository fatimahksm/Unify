package com.university.unify.model;

public class FacultyAdminItem {

    private String facultyDocId;
    private String facultyName;
    private String facultyCode;

    private String adminUid;
    private String adminName;
    private String adminEmail;

    public FacultyAdminItem() {
    }

    public FacultyAdminItem(String facultyDocId, String facultyName, String facultyCode,
                            String adminUid, String adminName, String adminEmail) {
        this.facultyDocId = facultyDocId;
        this.facultyName = facultyName;
        this.facultyCode = facultyCode;
        this.adminUid = adminUid;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
    }

    public String getFacultyDocId() {
        return facultyDocId;
    }

    public void setFacultyDocId(String facultyDocId) {
        this.facultyDocId = facultyDocId;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getFacultyCode() {
        return facultyCode;
    }

    public void setFacultyCode(String facultyCode) {
        this.facultyCode = facultyCode;
    }

    public String getAdminUid() {
        return adminUid;
    }

    public void setAdminUid(String adminUid) {
        this.adminUid = adminUid;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }
}