package com.lazywhatsapreader;

/*
  Created by gopinaths on 11/30/2016.
 */


import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;


public class App extends Application {
    private static FirebaseAnalytics firebaseAnalytics;
    public static App app;
    private static Application sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }

    public static App getInstance(Context context) {
        if (app == null) {
            app = new App();
        }
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        return app;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    public static Application getApplication() {
        return sApplication;
    }


    public void setPageName(String name) {
        if (firebaseAnalytics != null) {

            firebaseAnalytics.setUserProperty(name, " Screen Name");

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Page Name");
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        }

    }

    public void setClickedButton(String Category, String clickedButton) {
        if (firebaseAnalytics != null) {

            firebaseAnalytics.setUserProperty(Category, clickedButton);


            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "2");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, clickedButton);
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Button");
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    }

    public void setClickedButton(String Category, String clickedButton, String Label) {
        if (firebaseAnalytics != null) {

            firebaseAnalytics.setUserProperty(Category+" "+ clickedButton, Label);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, Category);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, Label);
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, clickedButton);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    }


}
