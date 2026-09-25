package com.aizen.db;

import com.aizen.exception.DatabaseException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Creates all tables on start-up (idempotent). */
public final class DatabaseSetup {
    private static final String[] DDL = {
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                full_name TEXT NOT NULL,
                email TEXT NOT NULL
            )""",
            """
            CREATE TABLE IF NOT EXISTS resumes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                template TEXT NOT NULL DEFAULT 'Classic',
                full_name TEXT,
                email TEXT,
                phone TEXT,
                job_title TEXT,
                location TEXT,
                linkedin TEXT,
                summary TEXT,
                created_at TEXT,
                updated_at TEXT,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )""",
            """
            CREATE TABLE IF NOT EXISTS experiences (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                resume_id INTEGER NOT NULL,
                company TEXT, role TEXT, start_date TEXT, end_date TEXT, description TEXT,
                FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
            )""",
            """
            CREATE TABLE IF NOT EXISTS education (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                resume_id INTEGER NOT NULL,
                institution TEXT, degree TEXT, year TEXT, grade TEXT,
                FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
            )""",
            """
            CREATE TABLE IF NOT EXISTS projects (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                resume_id INTEGER NOT NULL,
                name TEXT, link TEXT, description TEXT,
                FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
            )""",
            """
            CREATE TABLE IF NOT EXISTS certifications (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                resume_id INTEGER NOT NULL,
                name TEXT, issuer TEXT, year TEXT,
                FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
            )""",
            """
            CREATE TABLE IF NOT EXISTS skills (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                resume_id INTEGER NOT NULL,
                name TEXT,
                FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
            )""",
            """
            CREATE TABLE IF NOT EXISTS applications (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                company TEXT NOT NULL,
                role_title TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'Applied',
                applied_date TEXT,
                notes TEXT,
                created_at TEXT,
                updated_at TEXT,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )"""
    };

    private DatabaseSetup() {
    }

    public static void initialize() throws DatabaseException {
        try (Connection c = Database.getConnection(); Statement st = c.createStatement()) {
            for (String sql : DDL) {
                st.execute(sql);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not create database tables: " + e.getMessage(), e);
        }
    }
}