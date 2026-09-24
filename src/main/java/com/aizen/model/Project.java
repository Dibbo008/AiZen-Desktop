package com.aizen.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {
    private int id;
    private int resumeId;
    private String name = "";
    private String link = "";
    private String description = "";

    public Project() {
    }

    public Project(String name, String link, String description) {
        setName(name);
        setLink(link);
        setDescription(description);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getResumeId() { return resumeId; }
    public void setResumeId(int resumeId) { this.resumeId = resumeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name == null ? "" : name; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link == null ? "" : link; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description == null ? "" : description; }
}
