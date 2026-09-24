package com.aizen.model;

import com.aizen.util.ValidationUtil;

/** A job applicant. Shows method overloading through the updateProfile(...) family. */
public class Applicant extends Person {
    private String jobTitle = "";
    private String location = "";
    private String linkedin = "";
    private String summary = "";

    public Applicant() {
        super();
    }

    public Applicant(int id, String name, String email, String phone,
                     String jobTitle, String location, String linkedin, String summary) {
        super(id, name, email, phone);
        updateProfile(jobTitle, location, linkedin, summary);
    }

    @Override
    public String getRole() {
        return "Applicant";
    }

    /** Overload #1: basic identity. */
    public void updateProfile(String name, String email) {
        setName(name);
        setEmail(email);
    }

    /** Overload #2: identity + phone. */
    public void updateProfile(String name, String email, String phone) {
        updateProfile(name, email);
        setPhone(phone);
    }

    /** Overload #3: professional details. */
    public void updateProfile(String jobTitle, String location, String linkedin, String summary) {
        setJobTitle(jobTitle);
        setLocation(location);
        setLinkedin(linkedin);
        setSummary(summary);
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = ValidationUtil.requireMaxLength("Job title", jobTitle, 120);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = ValidationUtil.requireMaxLength("Location", location, 120);
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = ValidationUtil.requireMaxLength("LinkedIn", linkedin, 200);
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = ValidationUtil.requireMaxLength("Summary", summary, 2000);
    }
}
