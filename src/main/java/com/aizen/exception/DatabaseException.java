package com.aizen.exception;

/** Checked exception thrown by the persistence layer (DAO / Database). */
public class DatabaseException extends Exception {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
