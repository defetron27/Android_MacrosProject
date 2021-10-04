package com.deffe.macros.grindersouls;

import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import com.afollestad.appthemeengine.ATEActivity;


public class BaseThemedActivity extends ATEActivity
{

    @NonNull
    @Override
    public final String getATEKey()
    {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ?
                "dark_theme" : "light_theme";
    }
}
