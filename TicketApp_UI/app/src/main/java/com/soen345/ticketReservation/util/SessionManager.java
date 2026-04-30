package com.soen345.ticketReservation.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.soen345.ticketReservation.model.User;
import com.soen345.ticketReservation.ui.admin.AdminEventListActivity;
import com.soen345.ticketReservation.ui.eventlist.EventListActivity;
import com.soen345.ticketReservation.ui.login.LoginActivity;

/** Handles the app's local user session stored in SharedPreferences. */
public final class SessionManager {

    public static final String PREFS_NAME = "TicketAppPrefs";

    private SessionManager() {}

    public static void saveUser(Context context, User user) {
        if (user == null) return;

        String role = isBlank(user.getRole()) ? "CUSTOMER" : user.getRole();

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString("userId", user.getUserId())
                .putString("userName", user.getName())
                .putString("email", user.getEmail())
                .putString("phoneNumber", user.getPhoneNumber())
                .putString("role", role)
                .apply();
    }

    public static boolean isLoggedIn(Context context) {
        return getPrefs(context).getString("userId", null) != null;
    }

    public static String getRole(Context context) {
        return getPrefs(context).getString("role", "CUSTOMER");
    }

    public static String getUserId(Context context) {
        return getPrefs(context).getString("userId", null);
    }

    public static String getEmail(Context context) {
        return getPrefs(context).getString("email", null);
    }

    public static String getPhoneNumber(Context context) {
        return getPrefs(context).getString("phoneNumber", null);
    }

    public static void clearSession(Context context) {
        getPrefs(context).edit().clear().apply();
    }

    public static void openHomeForSavedRole(Activity activity) {
        Class<?> destination = "ADMIN".equalsIgnoreCase(getRole(activity))
                ? AdminEventListActivity.class
                : EventListActivity.class;

        Intent intent = new Intent(activity, destination);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void logoutAndOpenLogin(Activity activity) {
        clearSession(activity);
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
