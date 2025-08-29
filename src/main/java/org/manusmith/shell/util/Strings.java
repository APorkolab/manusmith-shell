package org.manusmith.shell.util;

public final class Strings {
    private Strings() {} // Private constructor for utility class

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
