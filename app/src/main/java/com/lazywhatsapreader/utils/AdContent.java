package com.lazywhatsapreader.utils;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.lazywhatsapreader.R;

public class AdContent {
    public static InterstitialAd mInterstitialAd;

    public static InterstitialAd newInterstitialAd(final Context context) {
        InterstitialAd interstitialAd = new InterstitialAd(context);
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
        });
        return interstitialAd;
    }

    public static void showInterstitial(Context context) {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            // Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();

            // Show the next level and reload the ad to prepare for the level after.

            mInterstitialAd = newInterstitialAd(context);
            loadInterstitial();
        }
    }

    public static void loadInterstitial() {
        // Disable the next level button and load the ad.

        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        mInterstitialAd.loadAd(adRequest);
    }


}
