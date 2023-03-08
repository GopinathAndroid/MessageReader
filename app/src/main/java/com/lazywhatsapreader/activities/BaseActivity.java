package com.lazywhatsapreader.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.lazywhatsapreader.R;
import com.lazywhatsapreader.common.CommonUtilities;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //   MenuItem item = menu.findItem(R.id.action_toggle);
       /* if (mLayout != null) {
            if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                item.setTitle(R.string.action_show);
            } else {
                item.setTitle(R.string.action_hide);
            }
        }*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_rate_it:
                openRateIt();
                break;

            case R.id.youtubelink:
                openYoutubeLink();

                break;
            case R.id.action_otherapps:
                openOtherApps();
                break;
            case R.id.action_activity:
                Intent i = new Intent(BaseActivity.this,
                        MessageDescriptionActivity.class);
                i.putExtra("havePosition", false);
                startActivity(i);
                break;
            case R.id.action_all_share:
                createShareIntent();
                break;
            case R.id.action_settings:
                SettingsPage();
                break;


        }

        return super.onOptionsItemSelected(item);
    }

    private void openOtherApps() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://developer?id=" + CommonUtilities.DEVELOPER_ACCOUNT)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=" + CommonUtilities.DEVELOPER_ACCOUNT)));
        } catch (Exception e) {

        }
    }

    private void openRateIt() {

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + CommonUtilities.PACKAGE_NAME)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + CommonUtilities.PACKAGE_NAME)));
        } catch (Exception e) {

        }
    }

    private void openYoutubeLink() {

        try {
            String id = "0sbfY2XkSwg";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + id));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } catch (ActivityNotFoundException e) {

            try {


            // youtube is not installed.Will be opened in other available apps
            String content = "https://www.youtube.com/watch?v=0sbfY2XkSwg";
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(content));
            startActivity(i);
            }
            catch (Exception exception){

            }
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

    private void SettingsPage() {

        Intent i = new Intent(BaseActivity.this,
                SettingsActivity.class);
        startActivity(i);

    }
}
