package com.aizen.util;

import com.aizen.exception.ValidationException;

import java.util.regex.Pattern;

/** Static validation helpers shared by models, services and controllers. */
public final class ValidationUtil {
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9 ()\\-]{7,20}$");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,30}$");

    private ValidationUtil() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    public static boolean isValidEmail(String s) {
        return s != null && EMAIL.matcher(s.trim()).matches();
    }

    public static boolean isValidPhone(String s) {
        return s != null && PHONE.matcher(s.trim()).matches();
    }

    public static String requireNonBlank(String field, String value) {
        if (isBlank(value)) {
            throw new ValidationException(field + " is required.");
        }
        return value.trim();
    }

    public static String requireEmail(String field, String value) {
        String v = requireNonBlank(field, value);
        if (!isValidEmail(v)) {
            throw new ValidationException(field + " is not a valid e-mail address.");
        }
        return v;
    }

    public static String optionalPhone(String field, String value) {
        if (isBlank(value)) {
            return "";
        }
        if (!isValidPhone(value)) {
            throw new ValidationException(field + " is not a valid phone number.");
        }
        return value.trim();
    }

    public static String requireUsername(String value) {
        String v = requireNonBlank("Username", value);
        if (!USERNAME.matcher(v).matches()) {
            throw new ValidationException("Username must be 3-30 characters (letters, digits, underscore).");
        }
        return v;
    }

    public static String requireMaxLength(String field, String value, int max) {
        String v = trim(value);
        if (v.length() > max) {
            throw new ValidationException(field + " must be at most " + max + " characters.");
        }
        return v;
    }
}
