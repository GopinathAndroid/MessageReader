package com.lazywhatsapreader.utils;

import android.content.Context;
import android.os.Handler;

import com.google.gson.Gson;
import com.lazywhatsapreader.session.SharedPreference;

import java.util.Calendar;

public class Utility {

    public static void checkThreeDaysAdvertisement(SharedPreference sharedpref, Context context) {

        new Handler().postDelayed(() -> {

            Gson gson = new Gson();
            Calendar today = Calendar.getInstance();
            if (sharedpref.getDate().equalsIgnoreCase("")) {
                sharedpref.setDate(gson.toJson(today));
            }
            Calendar installedDay = gson.fromJson(sharedpref.getDate(), Calendar.class);

            long diff = today.getTimeInMillis() - installedDay.getTimeInMillis();
            long days = diff / (24 * 60 * 60 * 1000);
            if (days < 2) {
                if (sharedpref.getCount() > 5) {
                    if (sharedpref.getCount() % 5 == 0) {
                        AdContent.showInterstitial(context);
                    }

                }
            } else if (days <= 5) {

                if (sharedpref.getCount() % 3 == 0) {
                    AdContent.showInterstitial(context);
                }

            } else {
                if (sharedpref.getCount() % 2 == 0) {
                    AdContent.showInterstitial(context);
                }

            }


        }, 3000);
    }
}
