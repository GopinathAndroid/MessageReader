package com.lazywhatsapreader.fragments;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.lazywhatsapreader.R;

/**
 * Created by gopinaths on 3/16/2016.
 */
public class SettingsFragment extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_general);
    }
}
