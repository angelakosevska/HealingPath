package com.example.healingpath.activities;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healingpath.utils.LocaleHelper;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String lang = prefs.getString("app_language", "en");
        super.attachBaseContext(LocaleHelper.updateLocale(newBase, lang));
    }
}
