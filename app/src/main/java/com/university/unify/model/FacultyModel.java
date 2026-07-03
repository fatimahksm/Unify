package com.university.unify.model;

public class FacultyModel {

    private String facultyId;
    private String name;
    private String code;
    private String adminUid;
    private boolean active;
    private long createdAt;
    private long updatedAt;

    // Empty constructor required for Firebase
    public FacultyModel() {
    }

    public FacultyModel(String facultyId, String name, String code, String adminUid,
                        boolean active, long createdAt, long updatedAt) {
        this.facultyId = facultyId;
        this.name = name;
        this.code = code;
        this.adminUid = adminUid;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAdminUid() {
        return adminUid;
    }

    public void setAdminUid(String adminUid) {
        this.adminUid = adminUid;
    }


    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }



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