package com.aizen.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/** Aggregate root: a resume with its child collections (all generic ArrayLists). */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Resume {
    private int id;
    private int userId;
    private String title = "";
    private String template = "Classic";
    private String fullName = "";
    private String email = "";
    private String phone = "";
    private String jobTitle = "";
    private String location = "";
    private String linkedin = "";
    private String summary = "";
    private String createdAt = "";
    private String updatedAt = "";
    private List<Experience> experiences = new ArrayList<>();
    private List<Education> educations = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();
    private List<Certification> certifications = new ArrayList<>();
    private List<Skill> skills = new ArrayList<>();

    public Resume() {
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = nz(title); }
    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = (template == null || template.isBlank()) ? "Classic" : template; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = nz(fullName); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = nz(email); }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = nz(phone); }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = nz(jobTitle); }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = nz(location); }
    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = nz(linkedin); }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = nz(summary); }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = nz(createdAt); }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = nz(updatedAt); }

    public List<Experience> getExperiences() { return experiences; }
    public void setExperiences(List<Experience> v) { this.experiences = v == null ? new ArrayList<>() : v; }
    public List<Education> getEducations() { return educations; }
    public void setEducations(List<Education> v) { this.educations = v == null ? new ArrayList<>() : v; }
    public List<Project> getProjects() { return projects; }
    public void setProjects(List<Project> v) { this.projects = v == null ? new ArrayList<>() : v; }
    public List<Certification> getCertifications() { return certifications; }
    public void setCertifications(List<Certification> v) { this.certifications = v == null ? new ArrayList<>() : v; }
    public List<Skill> getSkills() { return skills; }
    public void setSkills(List<Skill> v) { this.skills = v == null ? new ArrayList<>() : v; }

    @Override
    public String toString() {
        return title.isBlank() ? "(untitled)" : title;
    }
}
