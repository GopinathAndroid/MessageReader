package com.lazywhatsapreader.utils;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.lazywhatsapreader.R;

public class AdContent {
    public static InterstitialAd mInterstitialAd;

    public static void newInterstitialAd(final Context context) {
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();

        InterstitialAd.load(context,context.getString(R.string.interstitial_settings_page_ad_unit_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                //  Log.i(TAG, loadAdError.getMessage());
                mInterstitialAd = null;
            }
        });
        /*InterstitialAd interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(context.getString(R.string.interstitial_settings_page_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {

            }

            @Override
            public void onAdFailedToLoad(int errorCode) {

            }

            @Override
            public void onAdClosed() {
                // Proceed to the next level.
                // Show the next level and reload the ad to prepare for the level after.

                mInterstitialAd = newInterstitialAd(context);
                loadInterstitial();
            }
        });*/
        //return interstitialAd;
    }

    public static void showInterstitial(Context context) {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
       /* if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            // Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();

            // Show the next level and reload the ad to prepare for the level after.

            mInterstitialAd = newInterstitialAd(context);
            loadInterstitial();
        }*/

        if (mInterstitialAd != null) {
            mInterstitialAd.show((Activity) context);
        } else {

            newInterstitialAd(context);
        }
    }

    /*public static void loadInterstitial() {
        // Disable the next level button and load the ad.

        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        mInterstitialAd.loadAd(adRequest);
    }*/


}
