package com.lazywhatsapreader.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.lazywhatsapreader.App;
import com.lazywhatsapreader.R;

public class AppRater {
    private final static String APP_TITLE = "Message Reader";// App Name
    private final static int DAYS_UNTIL_PROMPT = 5;//Min number of minutes
    private final static int LAUNCHES_UNTIL_PROMPT = 3;//Min number of launches

    public static boolean app_launched(Context mContext) {

        SharedPreferences prefs = mContext.getSharedPreferences("apprater", Context.MODE_PRIVATE);
        App.getInstance(mContext).setPageName("launch_count "+prefs.getLong("launch_count", 0));
        if (prefs.getBoolean("dontshowagain", false)) { return false; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter 2
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }
        editor.apply();
        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT && launch_count%3==0) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 60 * 1000)) {
               /* if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // only for gingerbread and newer versions
                    return true;
                }
                else {*/
                    showRateDialog(mContext, editor);

                App.getInstance(mContext).setClickedButton(Constants.Action_rating, "Rating Alert called");
               // }

            }
        }


        editor.apply();

        return false;
    }



    private static void showRateDialog(final Context context, final SharedPreferences.Editor editor) {
        if (context == null) return;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

            alertDialogBuilder.setTitle("Rate "+APP_TITLE);

        alertDialogBuilder
                .setMessage("If you enjoy using this app, Please spend few sec to rate it. Thank you for your support!")
                .setCancelable(false)
                .setPositiveButton("Rate Now" , (dialog, id) -> {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Constants.PACKAGE_NAME)));

                    if (editor != null) {
                        editor.putBoolean("dontshowagain", true);
                        editor.commit();
                        App.getInstance(context).setClickedButton(Constants.Action_rating, "Rating app");
                    }
                    dialog.dismiss();
                });
        alertDialogBuilder.setNegativeButton("Later", (dialog, id) -> {

            dialog.dismiss();
            App.getInstance(context).setClickedButton(Constants.Action_rating, "Later");
        });

        alertDialogBuilder.setIcon(R.mipmap.ic_launcher);

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

    }
}
