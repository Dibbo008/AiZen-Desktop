package com.aizen.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Skill {
    private int id;
    private int resumeId;
    private String name = "";

    public Skill() {
    }

    public Skill(String name) {
        setName(name);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getResumeId() { return resumeId; }
    public void setResumeId(int resumeId) { this.resumeId = resumeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name == null ? "" : name; }

    @Override
    public String toString() {
        return name;
    }
}
