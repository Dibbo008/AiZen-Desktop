package com.aizen.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Education {
    private int id;
    private int resumeId;
    private String institution = "";
    private String degree = "";
    private String year = "";
    private String grade = "";

    public Education() {
    }

    public Education(String institution, String degree, String year, String grade) {
        setInstitution(institution);
        setDegree(degree);
        setYear(year);
        setGrade(grade);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getResumeId() { return resumeId; }
    public void setResumeId(int resumeId) { this.resumeId = resumeId; }
    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution == null ? "" : institution; }
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree == null ? "" : degree; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year == null ? "" : year; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade == null ? "" : grade; }
}
