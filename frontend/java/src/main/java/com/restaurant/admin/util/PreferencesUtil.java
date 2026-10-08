package com.restaurant.admin.util;

import java.util.prefs.Preferences;

public class PreferencesUtil {
    private static final Preferences prefs = Preferences.userNodeForPackage(PreferencesUtil.class);
    private static final String EMAIL_KEY = "saved_email";

    public static void saveEmail(String email) {
        prefs.put(EMAIL_KEY, email);
    }

    public static String getEmail() {
        return prefs.get(EMAIL_KEY, null);
    }

    public static void clearEmail() {
        prefs.remove(EMAIL_KEY);
    }
}