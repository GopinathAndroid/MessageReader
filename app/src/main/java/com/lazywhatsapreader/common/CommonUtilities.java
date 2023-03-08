package com.lazywhatsapreader.common;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.lazywhatsapreader.activities.SpeechMainActivity;
import com.lazywhatsapreader.session.SharedPreference;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by gopinaths on 2/13/2016.
 */
public class CommonUtilities {

    public static int playposition = 2000;
    public static boolean nightmode = false;
    SharedPreference sh;
    public static String speakingText = "";

    final public static String PACKAGE_NAME = "com.lazywhatsapreader";
    final public static String DEVELOPER_ACCOUNT = "G-Technology";


    public CommonUtilities() {

    }

    public CommonUtilities(Context context) {
        sh = new SharedPreference(context);
    }

    public boolean checkNextMessageAvailable() {
        return sh.getLastPlayedPosition() + 1 < SpeechMainActivity.cursor.getCount();
    }

    public boolean checkPreviousMessageAvailable() {
        return sh.getLastPlayedPosition() - 1 >= 0;
    }

    public void setupPreviousMessage() {
        sh.setLastPlayedPosition(sh.getLastPlayedPosition() - 1);
        SpeechMainActivity.cursor.moveToPosition(sh.getLastPlayedPosition());
        speakingText = SpeechMainActivity.cursor.getString(2);
    }

    public void setupNextMessage() {
        sh.setLastPlayedPosition(sh.getLastPlayedPosition() + 1);
        SpeechMainActivity.cursor.moveToPosition(sh.getLastPlayedPosition());
        speakingText = SpeechMainActivity.cursor.getString(2);
    }



    public boolean appInstalledOrNot(String uri, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            ApplicationInfo ai = pm.getApplicationInfo(uri, 0);

            return ai.enabled;

        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    public static List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list, PackageManager packageManager) {
        ArrayList<ApplicationInfo> applist = new ArrayList<>();

        for (ApplicationInfo info : list) {
            try {
                if (null != packageManager.getLaunchIntentForPackage(info.packageName)) {
                    applist.add(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return applist;
    }

    public static String removeUrl(String commentstr)
    {
        String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern p = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(commentstr);
        int i = 0;
        while (m.find()) {
            commentstr = commentstr.replaceAll(m.group(i),"").trim();
            i++;
        }
        return commentstr;
    }
}
