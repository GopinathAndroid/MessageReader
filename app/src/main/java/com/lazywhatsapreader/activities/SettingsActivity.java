package com.lazywhatsapreader.activities;


import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.core.app.NavUtils;

import com.lazywhatsapreader.R;
import com.lazywhatsapreader.common.CommonUtilities;
import com.lazywhatsapreader.session.SharedPreference;
import com.lazywhatsapreader.utils.Dialogs;
import com.lazywhatsapreader.utils.Utility;



/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private SharedPreference sh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        sh = SharedPreference.getInstance(this);
        // Create the InterstitialAd and set the adUnitId (defined in values/strings.xml).
        Preference sharePref = findPreference("key_share");
        Preference resetPref = findPreference("key_resetapp");
        Preference ratePref = findPreference("key_rateapp");
        Preference openOtherApp = findPreference("key_openOtherApp");
        sharePref.setOnPreferenceClickListener(this);
        resetPref.setOnPreferenceClickListener(this);
        ratePref.setOnPreferenceClickListener(this);
        openOtherApp.setOnPreferenceClickListener(this);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case "key_share":
                openShareWhatsapp();
                break;
            case "key_resetapp":

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.commit();
                recreate();

                break;
            case "key_rateapp":
                openRateIt();
                break;
            case "key_openOtherApp":
                Dialogs obj = new Dialogs(getApplicationContext());
                obj.appListDialog(this);
                break;
        }


        return false;
    }

    private void openRateIt() {

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + CommonUtilities.PACKAGE_NAME)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + CommonUtilities.PACKAGE_NAME)));
        } catch (Exception e) {

        }
    }

    private void openShareWhatsapp() {
        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.setType("text/plain");
        whatsappIntent.setPackage("com.whatsapp");

        whatsappIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_content));
        try {
            this.startActivity(whatsappIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            //  Toast.MakeShortText("Whatsapp have not been installed.");
            createShareIntent();
        }
    }

    private void createShareIntent() {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getResources().getString(R.string.share_content));
        startActivity(shareIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();


        sh.setCount(sh.getCount() + 1);
        Utility.checkThreeDaysAdvertisement(sh,this);

    }
}
