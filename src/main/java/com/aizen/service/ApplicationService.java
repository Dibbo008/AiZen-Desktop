package com.aizen.service;

import com.aizen.dao.ApplicationDAO;
import com.aizen.exception.DatabaseException;
import com.aizen.model.Application;
import com.aizen.util.ValidationUtil;

import java.util.List;

/** Business logic for tracked job applications: validation and persistence. */
public class ApplicationService {
    private final ApplicationDAO dao = new ApplicationDAO();

    public void validate(Application a) {
        ValidationUtil.requireNonBlank("Company", a.getCompany());
        ValidationUtil.requireMaxLength("Company", a.getCompany(), 120);
        ValidationUtil.requireNonBlank("Role", a.getRoleTitle());
        ValidationUtil.requireMaxLength("Role", a.getRoleTitle(), 120);
        ValidationUtil.requireMaxLength("Notes", a.getNotes(), 1000);
    }

    public Application save(Application a, int userId) throws DatabaseException {
        validate(a);
        a.setUserId(userId);
        return a.getId() > 0 ? dao.update(a) : dao.insert(a);
    }

    public List<Application> listForUser(int userId) throws DatabaseException {
        return dao.findByUserId(userId);
    }

    public boolean delete(int applicationId) throws DatabaseException {
        return dao.delete(applicationId);
    }
}