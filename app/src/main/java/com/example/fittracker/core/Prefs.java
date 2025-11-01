package com.example.fittracker.core;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String PREFS_NAME = "fittracker_prefs";
    private static final String KEY_REMEMBER_ME = "remember_me";

    public static void setRememberMe(Context ctx, boolean value) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(KEY_REMEMBER_ME, value).apply();
    }

    public static boolean isRememberMe(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_REMEMBER_ME, false);
    }

    public static void clearAll(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }
}