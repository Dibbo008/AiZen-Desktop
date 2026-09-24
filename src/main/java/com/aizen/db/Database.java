package com.aizen.db;

import com.aizen.exception.DatabaseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Hands out SQLite connections. The DB file lives in ~/.aizen/aizen.db. */
public final class Database {
    private static final Path DB_DIR = Paths.get(System.getProperty("user.home"), ".aizen");
    private static final String URL = "jdbc:sqlite:" + DB_DIR.resolve("aizen.db").toAbsolutePath();

    private Database() {
    }

    public static Connection getConnection() throws DatabaseException {
        try {
            Files.createDirectories(DB_DIR);
            Connection connection = DriverManager.getConnection(URL);
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
                st.execute("PRAGMA busy_timeout = 5000");
            }
            return connection;
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Unable to open the database: " + e.getMessage(), e);
        }
    }
}
