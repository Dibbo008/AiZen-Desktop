package com.aizen.dao;

import com.aizen.db.Database;
import com.aizen.exception.DatabaseException;
import com.aizen.model.Application;

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

/** CRUD access for tracked job applications. */
public class ApplicationDAO implements GenericDAO<Application> {
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static Application map(ResultSet rs) throws SQLException {
        Application a = new Application();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setCompany(rs.getString("company"));
        a.setRoleTitle(rs.getString("role_title"));
        a.setStatus(rs.getString("status"));
        a.setAppliedDate(rs.getString("applied_date"));
        a.setNotes(rs.getString("notes"));
        a.setCreatedAt(rs.getString("created_at"));
        a.setUpdatedAt(rs.getString("updated_at"));
        return a;
    }

    @Override
    public Application insert(Application a) throws DatabaseException {
        String sql = "INSERT INTO applications(user_id, company, role_title, status, applied_date, notes, "
                + "created_at, updated_at) VALUES(?,?,?,?,?,?,?,?)";
        String now = LocalDateTime.now().format(STAMP);
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, a.getUserId());
            ps.setString(2, a.getCompany());
            ps.setString(3, a.getRoleTitle());
            ps.setString(4, a.getStatus());
            ps.setString(5, a.getAppliedDate());
            ps.setString(6, a.getNotes());
            ps.setString(7, now);
            ps.setString(8, now);
            ps.executeUpdate();
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    a.setId(rs.getInt(1));
                }
            }
            a.setCreatedAt(now);
            a.setUpdatedAt(now);
            return a;
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the application: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Application> findById(int id) throws DatabaseException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM applications WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the application: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Application> findAll() throws DatabaseException {
        return query("SELECT * FROM applications ORDER BY applied_date DESC, id DESC", null);
    }

    public List<Application> findByUserId(int userId) throws DatabaseException {
        return query("SELECT * FROM applications WHERE user_id = ? ORDER BY applied_date DESC, id DESC", userId);
    }

    private List<Application> query(String sql, Integer userId) throws DatabaseException {
        List<Application> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (userId != null) {
                ps.setInt(1, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Could not list applications: " + e.getMessage(), e);
        }
    }

    @Override
    public Application update(Application a) throws DatabaseException {
        String sql = "UPDATE applications SET company = ?, role_title = ?, status = ?, applied_date = ?, "
                + "notes = ?, updated_at = ? WHERE id = ?";
        String now = LocalDateTime.now().format(STAMP);
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getCompany());
            ps.setString(2, a.getRoleTitle());
            ps.setString(3, a.getStatus());
            ps.setString(4, a.getAppliedDate());
            ps.setString(5, a.getNotes());
            ps.setString(6, now);
            ps.setInt(7, a.getId());
            ps.executeUpdate();
            a.setUpdatedAt(now);
            return a;
        } catch (SQLException e) {
            throw new DatabaseException("Could not update the application: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM applications WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete the application: " + e.getMessage(), e);
        }
    }
}