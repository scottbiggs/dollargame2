package com.sleepfuriously.dollargame2.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

import androidx.annotation.Nullable;

import java.util.Map;

import com.sleepfuriously.dollargame2.R;

/**
 * Does the prefs
 */
public class PrefsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    //------------------------
    //  constants
    //------------------------

    public static final boolean SHOW_HINTS_DEFAULT = true;

//    public static final RandomizeButtonModes DEFAULT_RANDOMIZE_BUTTON_MODE
//            = RandomizeButtonModes.ENTIRE_RANGE;

//    /** All the different ways that the randomize button can work */
//    enum RandomizeButtonModes {
//        ENTIRE_RANGE,
//        SOLVABLE_AND_ABOVE,
//        EXACTLY_SOLVABLE,
//        SOLVABLE_PLUS_ZERO_TO_ONE
//    }

    public static final int DEFAULT_DIFFICULTY = 1;


    //------------------------
    //  data
    //------------------------

    SharedPreferences mPrefs;

    //------------------------
    //  methods
    //------------------------

    // todo: change max and min dollar amount
    // todo: change color scheme???
    // todo: change what the randomize button does

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This does it directly instead of using a Fragment
        addPreferencesFromResource(R.xml.preference_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPrefs = getPreferences(MODE_PRIVATE);
        mPrefs.registerOnSharedPreferenceChangeListener(this);

        Map<String, ?> prefsMap = mPrefs.getAll();
        for (Map.Entry<String, ?> prefEntry : prefsMap.entrySet()) {
            // iterate through the preference entries and update their
            // summary if they are an instance of EditTextPreference
            if (prefEntry instanceof EditTextPreference) {
                updateSummary((EditTextPreference) prefEntry);
            }
        }
    }


    @Override
    protected void onPause() {
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Map<String, ?> prefsMap = mPrefs.getAll();

        // get the preference that has been changed
        Object changedPref = prefsMap.get(key);

        if (key.equals(getString(R.string.pref_hints_cb_key))) {
            updateSummary((EditTextPreference) changedPref);
        }

        else if (key.equals(getString(R.string.pref_gameplay_difficulty_key))) {
            updateDifficulty((ListPreference) changedPref);
        }

    }


    private void updateSummary(EditTextPreference pref) {
        // set the EditTextPreference's summary value to
        // its current text
        pref.setSummary(pref.getText());
    }

    private void updateDifficulty(ListPreference pref) {
        // todo
    }
}
