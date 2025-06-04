package com.example.healingpath.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {

    public static final String LANGUAGE_KEY = "app_language";

    // Call this in Application or MainActivity on startup
    public static Context applySavedLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String languageCode = prefs.getString(LANGUAGE_KEY, "en"); // Default to English
        return updateLocale(context, languageCode);
    }

    // Call this when user changes language

    public static Context setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        prefs.edit().putString("app_language", languageCode).apply();

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }




    // Call this to get a context with the right locale
    public static Context updateLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }
}
