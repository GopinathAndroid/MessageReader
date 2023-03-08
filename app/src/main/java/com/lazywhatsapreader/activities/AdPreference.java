package com.lazywhatsapreader.activities;

/*
  Created by gopinaths on 6/27/2018.
 */


import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.lazywhatsapreader.R;

public class AdPreference extends Preference {
    Activity activity;
    FrameLayout adContainerView;
    public AdPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AdPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        // this will create the linear layout defined in ads_layout.xml
        View view = super.onCreateView(parent);

        // the context is a PreferenceActivity
         activity = (Activity) getContext();

       // ((FrameLayout) view).addView(adView);
        adContainerView=((FrameLayout) view);
        loadBanner();
        return view;
    }

    private void loadBanner() {

        AdView adView = new AdView(activity);
        adView.setAdUnitId(activity.getResources().getString(R.string.banner_settingspage_ad_unit_id));
        adContainerView.addView(adView);

        AdRequest adRequest =
                new AdRequest.Builder()
                        //.addTestDevice("B16275C051562E8FBF875C10D04C629E")
                        .build();

        AdSize adSize = getAdSize();
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);

        // Step 5 - Start loading the ad in the background.
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adContainerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int i) {
                adContainerView.setVisibility(View.GONE);
            }
        });
    }
    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }
}
