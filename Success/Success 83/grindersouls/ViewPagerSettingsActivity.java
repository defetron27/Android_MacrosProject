package com.deffe.macros.grindersouls;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.appthemeengine.ATE;

public class ViewPagerSettingsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager_settings);

        Fragment fragment = new SettingsScreen();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        if (savedInstanceState == null)
        {
            fragmentTransaction.add(R.id.relative_layout, fragment, "settings_fragment");
            fragmentTransaction.commit();
        }
        else
        {
            fragment = getFragmentManager().findFragmentByTag("settings_fragment");
        }
    }

    public static class SettingsScreen extends PreferenceFragment
    {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);
        }
    }

    public static String getTransformers(Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        return  preferences.getString("view_pager_transformation_preference","DefaultTransformer");
    }
}
