package com.aizen.model;

import com.aizen.exception.ValidationException;
import com.aizen.util.ValidationUtil;

/** A student applicant. Overrides getRole() (polymorphism). */
public class Student extends Applicant {
    private String university = "";
    private double cgpa;

    public Student() {
        super();
    }

    public Student(int id, String name, String email, String phone,
                   String jobTitle, String location, String linkedin, String summary,
                   String university, double cgpa) {
        super(id, name, email, phone, jobTitle, location, linkedin, summary);
        updateProfile(university, cgpa);
    }

    @Override
    public String getRole() {
        return "Student";
    }

    /** Overload #4 (different parameter types): academic details. */
    public void updateProfile(String university, double cgpa) {
        setUniversity(university);
        setCgpa(cgpa);
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = ValidationUtil.requireMaxLength("University", university, 150);
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        if (cgpa < 0 || cgpa > 10) {
            throw new ValidationException("CGPA must be between 0 and 10.");
        }
        this.cgpa = cgpa;
    }
}
