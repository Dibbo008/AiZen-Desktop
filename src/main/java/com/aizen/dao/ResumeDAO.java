package com.aizen.dao;

import com.aizen.db.Database;
import com.aizen.exception.DatabaseException;
import com.aizen.model.Certification;
import com.aizen.model.Education;
import com.aizen.model.Experience;
import com.aizen.model.Project;
import com.aizen.model.Resume;
import com.aizen.model.Skill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC implementation for resumes and all of their child rows (one transaction per write). */
public class ResumeDAO implements GenericDAO<Resume> {
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String INSERT_RESUME =
            "INSERT INTO resumes(user_id, title, template, full_name, email, phone, job_title, location, "
                    + "linkedin, summary, created_at, updated_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_RESUME =
            "UPDATE resumes SET title=?, template=?, full_name=?, email=?, phone=?, job_title=?, location=?, "
                    + "linkedin=?, summary=?, updated_at=? WHERE id=? AND user_id=?";

    // ------------------------------------------------------------------ writes

    @Override
    public Resume insert(Resume r) throws DatabaseException {
        String now = LocalDateTime.now().format(STAMP);
        r.setCreatedAt(now);
        r.setUpdatedAt(now);
        Connection c = null;
        try {
            c = Database.getConnection();
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(INSERT_RESUME)) {
                ps.setInt(1, r.getUserId());
                ps.setString(2, r.getTitle());
                ps.setString(3, r.getTemplate());
                ps.setString(4, r.getFullName());
                ps.setString(5, r.getEmail());
                ps.setString(6, r.getPhone());
                ps.setString(7, r.getJobTitle());
                ps.setString(8, r.getLocation());
                ps.setString(9, r.getLinkedin());
                ps.setString(10, r.getSummary());
                ps.setString(11, r.getCreatedAt());
                ps.setString(12, r.getUpdatedAt());
                ps.executeUpdate();
            }
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    r.setId(rs.getInt(1));
                }
            }
            insertChildren(c, r);
            c.commit();
            return r;
        } catch (SQLException e) {
            rollbackQuietly(c);
            throw new DatabaseException("Could not save the resume: " + e.getMessage(), e);
        } finally {
            closeQuietly(c);
        }
    }

    @Override
    public Resume update(Resume r) throws DatabaseException {
        r.setUpdatedAt(LocalDateTime.now().format(STAMP));
        Connection c = null;
        try {
            c = Database.getConnection();
            c.setAutoCommit(false);
            int changed;
            try (PreparedStatement ps = c.prepareStatement(UPDATE_RESUME)) {
                ps.setString(1, r.getTitle());
                ps.setString(2, r.getTemplate());
                ps.setString(3, r.getFullName());
                ps.setString(4, r.getEmail());
                ps.setString(5, r.getPhone());
                ps.setString(6, r.getJobTitle());
                ps.setString(7, r.getLocation());
                ps.setString(8, r.getLinkedin());
                ps.setString(9, r.getSummary());
                ps.setString(10, r.getUpdatedAt());
                ps.setInt(11, r.getId());
                ps.setInt(12, r.getUserId());
                changed = ps.executeUpdate();
            }
            if (changed == 0) {
                throw new DatabaseException("Resume not found (it may have been deleted).");
            }
            deleteChildren(c, r.getId());
            insertChildren(c, r);
            c.commit();
            return r;
        } catch (SQLException e) {
            rollbackQuietly(c);
            throw new DatabaseException("Could not update the resume: " + e.getMessage(), e);
        } catch (DatabaseException e) {
            rollbackQuietly(c);
            throw e;
        } finally {
            closeQuietly(c);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseException {
        Connection c = null;
        try {
            c = Database.getConnection();
            c.setAutoCommit(false);
            deleteChildren(c, id);
            int rows;
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM resumes WHERE id = ?")) {
                ps.setInt(1, id);
                rows = ps.executeUpdate();
            }
            c.commit();
            return rows > 0;
        } catch (SQLException e) {
            rollbackQuietly(c);
            throw new DatabaseException("Could not delete the resume: " + e.getMessage(), e);
        } finally {
            closeQuietly(c);
        }
    }

    // ------------------------------------------------------------------- reads

    @Override
    public Optional<Resume> findById(int id) throws DatabaseException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM resumes WHERE id = ?")) {
            ps.setInt(1, id);
            Resume r = null;
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    r = mapResume(rs);
                }
            }
            if (r != null) {
                loadChildren(c, r);
            }
            return Optional.ofNullable(r);
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the resume: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Resume> findAll() throws DatabaseException {
        return query("SELECT * FROM resumes ORDER BY updated_at DESC, id DESC", null);
    }

    public List<Resume> findByUserId(int userId) throws DatabaseException {
        return query("SELECT * FROM resumes WHERE user_id = ? ORDER BY updated_at DESC, id DESC", userId);
    }

    public int countByUserId(int userId) throws DatabaseException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM resumes WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not count resumes: " + e.getMessage(), e);
        }
    }

    private List<Resume> query(String sql, Integer userId) throws DatabaseException {
        List<Resume> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (userId != null) {
                ps.setInt(1, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResume(rs));
                }
            }
            for (Resume r : list) {
                loadChildren(c, r);
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Could not list resumes: " + e.getMessage(), e);
        }
    }

    // ----------------------------------------------------------------- helpers

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static Resume mapResume(ResultSet rs) throws SQLException {
        Resume r = new Resume();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setTitle(rs.getString("title"));
        r.setTemplate(rs.getString("template"));
        r.setFullName(rs.getString("full_name"));
        r.setEmail(rs.getString("email"));
        r.setPhone(rs.getString("phone"));
        r.setJobTitle(rs.getString("job_title"));
        r.setLocation(rs.getString("location"));
        r.setLinkedin(rs.getString("linkedin"));
        r.setSummary(rs.getString("summary"));
        r.setCreatedAt(rs.getString("created_at"));
        r.setUpdatedAt(rs.getString("updated_at"));
        return r;
    }

    private static void deleteChildren(Connection c, int resumeId) throws SQLException {
        for (String table : new String[]{"experiences", "education", "projects", "certifications", "skills"}) {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + table + " WHERE resume_id = ?")) {
                ps.setInt(1, resumeId);
                ps.executeUpdate();
            }
        }
    }

    private static void insertChildren(Connection c, Resume r) throws SQLException {
        int id = r.getId();
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO experiences(resume_id, company, role, start_date, end_date, description) VALUES(?,?,?,?,?,?)")) {
            for (Experience e : r.getExperiences()) {
                ps.setInt(1, id);
                ps.setString(2, e.getCompany());
                ps.setString(3, e.getRole());
                ps.setString(4, e.getStartDate());
                ps.setString(5, e.getEndDate());
                ps.setString(6, e.getDescription());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO education(resume_id, institution, degree, year, grade) VALUES(?,?,?,?,?)")) {
            for (Education e : r.getEducations()) {
                ps.setInt(1, id);
                ps.setString(2, e.getInstitution());
                ps.setString(3, e.getDegree());
                ps.setString(4, e.getYear());
                ps.setString(5, e.getGrade());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO projects(resume_id, name, link, description) VALUES(?,?,?,?)")) {
            for (Project p : r.getProjects()) {
                ps.setInt(1, id);
                ps.setString(2, p.getName());
                ps.setString(3, p.getLink());
                ps.setString(4, p.getDescription());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO certifications(resume_id, name, issuer, year) VALUES(?,?,?,?)")) {
            for (Certification cert : r.getCertifications()) {
                ps.setInt(1, id);
                ps.setString(2, cert.getName());
                ps.setString(3, cert.getIssuer());
                ps.setString(4, cert.getYear());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO skills(resume_id, name) VALUES(?,?)")) {
            for (Skill s : r.getSkills()) {
                ps.setInt(1, id);
                ps.setString(2, s.getName());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void loadChildren(Connection c, Resume r) throws SQLException {
        r.getExperiences().clear();
        r.getEducations().clear();
        r.getProjects().clear();
        r.getCertifications().clear();
        r.getSkills().clear();

        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM experiences WHERE resume_id = ? ORDER BY id")) {
            ps.setInt(1, r.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Experience e = new Experience(nz(rs.getString("company")), nz(rs.getString("role")),
                            nz(rs.getString("start_date")), nz(rs.getString("end_date")), nz(rs.getString("description")));
                    e.setId(rs.getInt("id"));
                    e.setResumeId(r.getId());
                    r.getExperiences().add(e);
                }
            }
        }
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM education WHERE resume_id = ? ORDER BY id")) {
            ps.setInt(1, r.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Education e = new Education(nz(rs.getString("institution")), nz(rs.getString("degree")),
                            nz(rs.getString("year")), nz(rs.getString("grade")));
                    e.setId(rs.getInt("id"));
                    e.setResumeId(r.getId());
                    r.getEducations().add(e);
                }
            }
        }
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM projects WHERE resume_id = ? ORDER BY id")) {
            ps.setInt(1, r.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Project p = new Project(nz(rs.getString("name")), nz(rs.getString("link")),
                            nz(rs.getString("description")));
                    p.setId(rs.getInt("id"));
                    p.setResumeId(r.getId());
                    r.getProjects().add(p);
                }
            }
        }
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM certifications WHERE resume_id = ? ORDER BY id")) {
            ps.setInt(1, r.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Certification cert = new Certification(nz(rs.getString("name")), nz(rs.getString("issuer")),
                            nz(rs.getString("year")));
                    cert.setId(rs.getInt("id"));
                    cert.setResumeId(r.getId());
                    r.getCertifications().add(cert);
                }
            }
        }
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM skills WHERE resume_id = ? ORDER BY id")) {
            ps.setInt(1, r.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Skill s = new Skill(nz(rs.getString("name")));
                    s.setId(rs.getInt("id"));
                    s.setResumeId(r.getId());
                    r.getSkills().add(s);
                }
            }
        }
    }

    private static void rollbackQuietly(Connection c) {
        if (c != null) {
            try {
                c.rollback();
            } catch (SQLException ignored) {
                // nothing more can be done
            }
        }
    }

    private static void closeQuietly(Connection c) {
        if (c != null) {
            try {
                c.close();
            } catch (SQLException ignored) {
                // nothing more can be done
            }
        }
    }
}
