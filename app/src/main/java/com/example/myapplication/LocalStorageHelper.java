package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalStorageHelper {
    private static final String SHARED_PREFS_FILE = "myAppPrefs";
    private SharedPreferences sharedPreferences;

    public LocalStorageHelper(Context context) {
        this.sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
    }

    public void saveData(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getData(String key) {
        return sharedPreferences.getString(key, null);
    }

    // Add more methods as needed for different data types or operations
}
