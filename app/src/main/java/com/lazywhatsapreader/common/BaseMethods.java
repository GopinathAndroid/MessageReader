package com.lazywhatsapreader.common;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

public class BaseMethods {
    public static boolean isMyServiceRunning(Class<?> serviceClass, Context a) {
        ActivityManager manager = (ActivityManager) a.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
