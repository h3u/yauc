package com.bitsailer.yauc;

import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

/**
 * YaucApplication Application
 * Created by Uli Wucherer (u.wucherer@gmail.com) on 22/08/16.
 */

public class YaucApplication extends Application {

    public static final String FB_EVENT_LIKE_ITEM = "item_liked";
    public static final String FB_EVENT_UNLIKE_ITEM = "item_unliked";
    public static final String FB_EVENT_PHOTO_LIST_TAB_VISITED = "photo_list_tab_visited";
    public static final String FB_EVENT_APP_FIRST_OPEN = "app_first_open";
    public static final String FB_PARAM_TAB_NAME = "tab_name";
    public static final String FB_PARAM_DEVICE_SCREEN_WIDTH = "device_screen_width";
    public static final String FB_PARAM_DEVICE_SCREEN_HEIGHT = "device_screen_height";
    public static final String FB_PARAM_ORIENTATION = "orientation";
    public static final String FB_PARAM_ORIENTATION_PORTRAIT = "portrait";
    public static final String FB_PARAM_ORIENTATION_LANDSCAPE = "landscape";

    public FirebaseAnalytics getDefaultTracker() {
        return FirebaseAnalytics.getInstance(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // disable logging for release builds
        if (!BuildConfig.DEBUG) {
            Logger.init().logLevel(LogLevel.NONE);
        }
    }

    public static void reportException(Throwable throwable) {
        if (BuildConfig.REPORT_CRASH) {
            FirebaseCrash.report(throwable);
        }
    }
}
