package com.example.islandlyrics;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WhitelistHelper {

    private static final String PREFS_NAME = "IslandLyricsPrefs";
    private static final String PREF_WHITELIST_OLD = "whitelist_packages";
    private static final String PREF_WHITELIST_JSON = "whitelist_json";

    private static final Set<String> DEFAULTS = new HashSet<>();
    static {
        DEFAULTS.add("com.tencent.qqmusic");
        DEFAULTS.add("com.miui.player");
        DEFAULTS.add("com.netease.cloudmusic");
    }

    public static List<WhitelistItem> loadWhitelist(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        List<WhitelistItem> items = new ArrayList<>();

        if (prefs.contains(PREF_WHITELIST_JSON)) {
            // New Format
            String json = prefs.getString(PREF_WHITELIST_JSON, "[]");
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    items.add(new WhitelistItem(
                        obj.getString("pkg"),
                        obj.optBoolean("enabled", true)
                    ));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (prefs.contains(PREF_WHITELIST_OLD)) {
            // Migrate
            Set<String> oldSet = prefs.getStringSet(PREF_WHITELIST_OLD, new HashSet<>());
            for (String pkg : oldSet) {
                items.add(new WhitelistItem(pkg, true));
            }
            saveWhitelist(context, items); // Verify migration
            prefs.edit().remove(PREF_WHITELIST_OLD).apply(); // Clean up
        } else {
            // Defaults
            for (String pkg : DEFAULTS) {
                items.add(new WhitelistItem(pkg, true));
            }
            saveWhitelist(context, items);
        }
        
        Collections.sort(items);
        return items;
    }

    public static void saveWhitelist(Context context, List<WhitelistItem> items) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray array = new JSONArray();
        for (WhitelistItem item : items) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("pkg", item.getPackageName());
                obj.put("enabled", item.isEnabled());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString(PREF_WHITELIST_JSON, array.toString()).apply();
    }
    
    // Helper for Service: Get only Enabled packages
    public static Set<String> getEnabledPackages(Context context) {
        List<WhitelistItem> items = loadWhitelist(context);
        Set<String> enabled = new HashSet<>();
        for (WhitelistItem item : items) {
            if (item.isEnabled()) {
                enabled.add(item.getPackageName());
            }
        }
        return enabled;
    }
}
