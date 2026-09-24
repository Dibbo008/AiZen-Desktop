package com.aizen.exception;

/** Unchecked exception thrown when user input or model state fails validation. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
