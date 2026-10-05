package com.sujeet.spaceshooter.fighter.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.sujeet.spaceshooter.fighter.entities.WweSuperstar;

import java.util.ArrayList;
import java.util.List;

public class WweRosterStorage {

    private static final String PREF_NAME = "WweRosterPrefs";
    private static final String KEY_COINS = "wwe_coins";
    private static final String KEY_SELECTED = "wwe_selected";

    public static int getCoins(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_COINS, 300);
    }

    public static void addCoins(Context context, int amount) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int current = getCoins(context);
        prefs.edit().putInt(KEY_COINS, current + amount).apply();
    }

    public static String getSelectedHeroId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SELECTED, "roman");
    }

    public static void setSelectedHeroId(Context context, String heroId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_SELECTED, heroId).apply();
    }

    public static List<WweSuperstar> loadRoster(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        List<WweSuperstar> roster = new ArrayList<>();

        // 1. ROMAN REIGNS (Default Unlocked)
        boolean romanUnlocked = prefs.getBoolean("unlocked_roman", true);
        int romanLevel = prefs.getInt("level_roman", 1);
        roster.add(new WweSuperstar("roman", "ROMAN REIGNS", 0, romanUnlocked, romanLevel, Color.rgb(0, 200, 255), Color.rgb(255, 215, 0)));

        // 2. CHRIS HERO (300 Coins)
        boolean heroUnlocked = prefs.getBoolean("unlocked_chris_hero", false);
        int heroLevel = prefs.getInt("level_chris_hero", 1);
        roster.add(new WweSuperstar("chris_hero", "CHRIS HERO", 300, heroUnlocked, heroLevel, Color.rgb(0, 200, 150), Color.rgb(255, 255, 0)));

        // 3. JOHN CENA (500 Coins)
        boolean cenaUnlocked = prefs.getBoolean("unlocked_cena", false);
        int cenaLevel = prefs.getInt("level_cena", 1);
        roster.add(new WweSuperstar("cena", "JOHN CENA", 500, cenaUnlocked, cenaLevel, Color.rgb(230, 40, 40), Color.rgb(0, 180, 255)));

        // 4. REY MYSTERIO (800 Coins)
        boolean reyUnlocked = prefs.getBoolean("unlocked_rey", false);
        int reyLevel = prefs.getInt("level_rey", 1);
        roster.add(new WweSuperstar("rey", "REY MYSTERIO", 800, reyUnlocked, reyLevel, Color.rgb(0, 220, 255), Color.rgb(255, 100, 0)));

        // 5. THE ROCK (1200 Coins)
        boolean rockUnlocked = prefs.getBoolean("unlocked_rock", false);
        int rockLevel = prefs.getInt("level_rock", 1);
        roster.add(new WweSuperstar("rock", "THE ROCK", 1200, rockUnlocked, rockLevel, Color.rgb(255, 180, 0), Color.rgb(30, 30, 30)));

        // 6. BROCK LESNAR (1800 Coins)
        boolean brockUnlocked = prefs.getBoolean("unlocked_brock", false);
        int brockLevel = prefs.getInt("level_brock", 1);
        roster.add(new WweSuperstar("brock", "BROCK LESNAR", 1800, brockUnlocked, brockLevel, Color.rgb(220, 100, 0), Color.rgb(40, 40, 40)));

        // 7. UNDERTAKER (2500 Coins)
        boolean undertakerUnlocked = prefs.getBoolean("unlocked_undertaker", false);
        int undertakerLevel = prefs.getInt("level_undertaker", 1);
        roster.add(new WweSuperstar("undertaker", "UNDERTAKER", 2500, undertakerUnlocked, undertakerLevel, Color.rgb(140, 30, 200), Color.rgb(10, 10, 10)));

        return roster;
    }

    public static WweSuperstar getSelectedSuperstar(Context context) {
        String selectedId = getSelectedHeroId(context);
        for (WweSuperstar s : loadRoster(context)) {
            if (s.getId().equals(selectedId)) {
                return s;
            }
        }
        return loadRoster(context).get(0);
    }

    public static void saveSuperstarState(Context context, String heroId, boolean unlocked, int level) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("unlocked_" + heroId, unlocked)
                .putInt("level_" + heroId, level)
                .apply();
    }
}
