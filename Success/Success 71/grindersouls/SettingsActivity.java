package com.deffe.macros.grindersouls;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.View;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.afollestad.appthemeengine.prefs.ATEColorPreference;
import com.afollestad.appthemeengine.prefs.ATESwitchPreference;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.afollestad.materialdialogs.prefs.MaterialListPreference;

public class SettingsActivity extends BaseThemedActivity implements ColorChooserDialog.ColorCallback, ATEActivityThemeCustomizer
{


    @StyleRes
    @Override
    public int getActivityTheme()
    {
        // Overrides what's set in the current ATE Config
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ?
                R.style.AppThemeDark_ActionBar : R.style.AppTheme_ActionBar;
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        final Config config = ATE.config(this, getATEKey());
        switch (dialog.getTitle()) {
            case R.string.primary_color:
                config.primaryColor(selectedColor);
                break;
            case R.string.accent_color:
                config.accentColor(selectedColor);
                break;
            case R.string.primary_text_color:
                config.textColorPrimary(selectedColor);
                break;
            case R.string.secondary_text_color:
                config.textColorSecondary(selectedColor);
                break;
        }
        config.commit();
        recreate(); // recreation needed to reach the checkboxes in the ate_preferences layout
    }


    public static class SettingsFragment extends PreferenceFragment
    {

        String mAteKey;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            setHasOptionsMenu(true);
            addPreferencesFromResource(R.xml.ate_preferences);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            invalidateSettings();
        }

        public void invalidateSettings() {
            mAteKey = ((SettingsActivity) getActivity()).getATEKey();

            ATEColorPreference primaryColorPref = (ATEColorPreference) findPreference("primary_color");
            primaryColorPref.setColor(Config.primaryColor(getActivity(), mAteKey), Color.BLACK);
            primaryColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.primary_color)
                            .preselect(Config.primaryColor(getActivity(), mAteKey)).show();
                    return true;
                }
            });

            ATEColorPreference accentColorPref = (ATEColorPreference) findPreference("accent_color");
            accentColorPref.setColor(Config.accentColor(getActivity(), mAteKey), Color.BLACK);
            accentColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.accent_color)
                            .preselect(Config.accentColor(getActivity(), mAteKey))
                            .show();
                    return true;
                }
            });




            ATEColorPreference textColorPrimaryPref = (ATEColorPreference) findPreference("text_primary");
            textColorPrimaryPref.setColor(Config.textColorPrimary(getActivity(), mAteKey), Color.BLACK);
            textColorPrimaryPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.primary_text_color)
                            .preselect(Config.textColorPrimary(getActivity(), mAteKey))
                            .show();
                    return true;
                }
            });

            ATEColorPreference textColorSecondaryPref = (ATEColorPreference) findPreference("text_secondary");
            textColorSecondaryPref.setColor(Config.textColorSecondary(getActivity(), mAteKey), Color.BLACK);
            textColorSecondaryPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.secondary_text_color)
                            .preselect(Config.textColorSecondary(getActivity(), mAteKey))
                            .show();
                    return true;
                }
            });

            findPreference("dark_theme").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // Marks both theme configs as changed so MainActivity restarts itself on return
                    Config.markChanged(getActivity(), "light_theme");
                    Config.markChanged(getActivity(), "dark_theme");
                    // The dark_theme preference value gets saved by Android in the default PreferenceManager.
                    // It's used in getATEKey() of both the Activities.
                    getActivity().recreate();
                    return true;
                }
            });

            final MaterialListPreference lightStatusMode = (MaterialListPreference) findPreference("light_status_bar_mode");
            final MaterialListPreference lightToolbarMode = (MaterialListPreference) findPreference("light_toolbar_mode");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                lightStatusMode.setEnabled(true);
                lightStatusMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        @Config.LightStatusBarMode
                        int constant = Integer.parseInt((String) newValue);
                        ATE.config(getActivity(), mAteKey)
                                .lightStatusBarMode(constant)
                                .apply(getActivity());
                        return true;
                    }
                });
            } else {
                lightStatusMode.setEnabled(false);
                lightStatusMode.setSummary(R.string.not_available_below_m);
            }

            lightToolbarMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    @Config.LightToolbarMode
                    int constant = Integer.parseInt((String) newValue);
                    ATE.config(getActivity(), mAteKey)
                            .lightToolbarMode(constant)
                            .apply(getActivity());
                    return true;
                }
            });

            final ATESwitchPreference statusBarPref = (ATESwitchPreference) findPreference("colored_status_bar");
            final ATESwitchPreference navBarPref = (ATESwitchPreference) findPreference("colored_nav_bar");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                statusBarPref.setChecked(Config.coloredStatusBar(getActivity(), mAteKey));
                statusBarPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        ATE.config(getActivity(), mAteKey)
                                .coloredStatusBar((Boolean) newValue)
                                .apply(getActivity());
                        return true;
                    }
                });


                navBarPref.setChecked(Config.coloredNavigationBar(getActivity(), mAteKey));
                navBarPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        ATE.config(getActivity(), mAteKey)
                                .coloredNavigationBar((Boolean) newValue)
                                .apply(getActivity());
                        return true;
                    }
                });
            } else {
                statusBarPref.setEnabled(false);
                statusBarPref.setSummary(R.string.not_available_below_lollipop);
                navBarPref.setEnabled(false);
                navBarPref.setSummary(R.string.not_available_below_lollipop);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference_activity_custom);

        if (savedInstanceState == null)
        {
            getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
        }
        else
        {
            SettingsFragment frag = (SettingsFragment) getFragmentManager().findFragmentById(R.id.content_frame);
            if (frag != null) frag.invalidateSettings();
        }
    }

}
