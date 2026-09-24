package com.aizen.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Experience {
    private int id;
    private int resumeId;
    private String company = "";
    private String role = "";
    private String startDate = "";
    private String endDate = "";
    private String description = "";

    public Experience() {
    }

    public Experience(String company, String role, String startDate, String endDate, String description) {
        setCompany(company);
        setRole(role);
        setStartDate(startDate);
        setEndDate(endDate);
        setDescription(description);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getResumeId() { return resumeId; }
    public void setResumeId(int resumeId) { this.resumeId = resumeId; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company == null ? "" : company; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role == null ? "" : role; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate == null ? "" : startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate == null ? "" : endDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description == null ? "" : description; }
}
