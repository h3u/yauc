package com.bitsailer.yauc;

import android.app.Application;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
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

    private static final long REMOTE_CONFIG_EXPIRE = 3600L;

    private FirebaseRemoteConfig firebaseRemoteConfig;

    public FirebaseAnalytics getDefaultTracker() {
        return FirebaseAnalytics.getInstance(this);
    }

    public FirebaseRemoteConfig getFirebaseRemoteConfig() {
        return firebaseRemoteConfig;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setupRemoteConfig();

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

    private void setupRemoteConfig() {
        if (!FirebaseApp.getApps(this).isEmpty()) {
            firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build();
            firebaseRemoteConfig.setConfigSettings(configSettings);

            firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
            firebaseRemoteConfig.fetch(REMOTE_CONFIG_EXPIRE).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        firebaseRemoteConfig.activateFetched();
                    }
                }
            });
        }
    }
}
