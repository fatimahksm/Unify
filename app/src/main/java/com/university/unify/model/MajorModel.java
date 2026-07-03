package com.university.unify.model;

public class MajorModel {

    private String majorId;
    private String facultyId;
    private String facultyName;
    private String name;
    private String code;
    private boolean active;
    private long createdAt;
    private long updatedAt;

    public MajorModel() {
    }

    public MajorModel(String majorId, String facultyId, String facultyName, String name, String code,
                      boolean active, long createdAt, long updatedAt) {
        this.majorId = majorId;
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.name = name;
        this.code = code;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getMajorId() {
        return majorId;
    }

    public void setMajorId(String majorId) {
        this.majorId = majorId;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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