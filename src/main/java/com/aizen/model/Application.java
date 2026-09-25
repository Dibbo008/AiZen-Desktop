package com.aizen.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** A single job application the user is tracking (company, role, status, notes). */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Application {

    public static final String APPLIED = "Applied";
    public static final String INTERVIEW = "Interview";
    public static final String OFFER = "Offer";
    public static final String REJECTED = "Rejected";

    public static String[] statuses() {
        return new String[]{APPLIED, INTERVIEW, OFFER, REJECTED};
    }

    private int id;
    private int userId;
    private String company = "";
    private String roleTitle = "";
    private String status = APPLIED;
    private String appliedDate = "";
    private String notes = "";
    private String createdAt = "";
    private String updatedAt = "";

    public Application() {
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = nz(company); }
    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = nz(roleTitle); }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = (status == null || status.isBlank()) ? APPLIED : status; }
    public String getAppliedDate() { return appliedDate; }
    public void setAppliedDate(String appliedDate) { this.appliedDate = nz(appliedDate); }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = nz(notes); }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = nz(createdAt); }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = nz(updatedAt); }

    @Override
    public String toString() {
        return company.isBlank() ? "(untitled)" : company + " - " + roleTitle;
    }
}