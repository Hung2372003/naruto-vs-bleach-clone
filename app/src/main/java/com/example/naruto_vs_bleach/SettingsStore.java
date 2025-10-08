package com.example.naruto_vs_bleach;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsStore {
    private static final String PREF = "game_settings";

    // Difficulty
    public static final int DIFF_EASY   = 0;
    public static final int DIFF_NORMAL = 1;
    public static final int DIFF_HARD   = 2;

    // Time
    public static final int TIME_INFINITY = -1;

    private final SharedPreferences sp;

    public SettingsStore(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    // --- Difficulty ---
    public void setDifficulty(int level) { sp.edit().putInt("difficulty", level).apply(); }
    public int getDifficulty() { return sp.getInt("difficulty", DIFF_NORMAL); }

    // --- HP Multiplier (1 = 100%, 2 = 200%) ---
    public void setHpMultiplier(int mul) { sp.edit().putInt("hp_mul", mul).apply(); }
    public int getHpMultiplier() { return sp.getInt("hp_mul", 1); }

    // --- Time Limit (seconds) ---
    public void setTimeLimitSeconds(int seconds) { sp.edit().putInt("time_limit", seconds).apply(); }
    public int getTimeLimitSeconds() { return sp.getInt("time_limit", TIME_INFINITY); }

    // --- Volumes (0.0f .. 1.0f) ---
    public void setSfxVolume(float v) { sp.edit().putFloat("sfx_volume", clamp01(v)).apply(); }
    public float getSfxVolume() { return sp.getFloat("sfx_volume", 1.0f); }

    public void setMusicVolume(float v) { sp.edit().putFloat("music_volume", clamp01(v)).apply(); }
    public float getMusicVolume() { return sp.getFloat("music_volume", 1.0f); }

    private float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }
}
