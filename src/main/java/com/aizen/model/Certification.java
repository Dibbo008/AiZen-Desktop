package com.aizen.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Certification {
    private int id;
    private int resumeId;
    private String name = "";
    private String issuer = "";
    private String year = "";

    public Certification() {
    }

    public Certification(String name, String issuer, String year) {
        setName(name);
        setIssuer(issuer);
        setYear(year);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getResumeId() { return resumeId; }
    public void setResumeId(int resumeId) { this.resumeId = resumeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name == null ? "" : name; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer == null ? "" : issuer; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year == null ? "" : year; }
}
