package com.aizen.model;

import com.aizen.util.ValidationUtil;

/**
 * Abstract base class of the person hierarchy: Person -> Applicant -> Student.
 * Demonstrates abstraction (abstract getRole()) and encapsulation (validated setters).
 */
public abstract class Person {
    private int id;
    private String name;
    private String email;
    private String phone;

    protected Person() {
        this.name = "";
        this.email = "";
        this.phone = "";
    }

    protected Person(int id, String name, String email, String phone) {
        this.id = id;
        setName(name);
        setEmail(email);
        setPhone(phone);
    }

    /** Polymorphic hook - every subclass describes its own role. */
    public abstract String getRole();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = ValidationUtil.requireNonBlank("Name", name);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = ValidationUtil.requireEmail("E-mail", email);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = ValidationUtil.optionalPhone("Phone", phone);
    }

    @Override
    public String toString() {
        return getRole() + "[" + name + ", " + email + "]";
    }
}
