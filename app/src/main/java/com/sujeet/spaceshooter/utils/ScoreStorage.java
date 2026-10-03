package com.sujeet.spaceshooter.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ScoreStorage {

    private static final String PREF_NAME =
            "SpaceShooterPrefs";

    private static final String KEY_HIGH_SCORE =
            "high_score";

    public static void save(
            Context context,
            int score) {

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        prefs.edit()
                .putInt(
                        KEY_HIGH_SCORE,
                        score
                )
                .apply();
    }

    public static int load(
            Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        return prefs.getInt(
                KEY_HIGH_SCORE,
                0
        );
    }
}
