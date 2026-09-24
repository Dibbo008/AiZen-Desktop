package com.aizen.service;

import com.aizen.model.Certification;
import com.aizen.model.Education;
import com.aizen.model.Experience;
import com.aizen.model.Project;
import com.aizen.model.Resume;
import com.aizen.model.Skill;
import com.aizen.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Resume <-> JSON file import / export. */
public class JsonService {

    public File exportResume(Resume resume, File target) throws IOException {
        Files.writeString(target.toPath(), JsonUtil.toJson(resume), StandardCharsets.UTF_8);
        return target;
    }

    public Resume importResume(File source) throws IOException {
        String json = Files.readString(source.toPath(), StandardCharsets.UTF_8);
        Resume r = JsonUtil.fromJson(json, Resume.class);
        if (r == null) {
            throw new IOException("The file does not contain a resume.");
        }
        // imported data is always treated as a brand-new resume
        r.setId(0);
        r.setUserId(0);
        r.setCreatedAt("");
        r.setUpdatedAt("");
        for (Experience e : r.getExperiences()) { e.setId(0); e.setResumeId(0); }
        for (Education e : r.getEducations()) { e.setId(0); e.setResumeId(0); }
        for (Project p : r.getProjects()) { p.setId(0); p.setResumeId(0); }
        for (Certification c : r.getCertifications()) { c.setId(0); c.setResumeId(0); }
        for (Skill s : r.getSkills()) { s.setId(0); s.setResumeId(0); }
        return r;
    }
}
