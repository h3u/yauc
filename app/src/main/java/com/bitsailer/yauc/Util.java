package com.bitsailer.yauc;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.text.TextUtils;

import com.bitsailer.yauc.data.PhotoColumns;
import com.orhanobut.logger.Logger;

/**
 * Utilities
 * Created by Uli Wucherer (u.wucherer@gmail.com) on 08/08/16.
 */

public class Util {

    /**
     * Distinguishes different kinds of app starts: <li>
     * <ul>
     * First start ever ({@link #FIRST_TIME})
     * </ul>
     * <ul>
     * First start in this version ({@link #FIRST_TIME_VERSION})
     * </ul>
     * <ul>
     * Normal app start ({@link #NORMAL})
     * </ul>
     *
     * @author williscool
     * @link https://gist.github.com/williscool/2a57bcd47a206e980eee
     * inspired by
     * @author schnatterer
     * @link http://stackoverflow.com/questions/4636141/determine-if-android-app-is-the-first-time-used
     *
     */
    public enum AppStart {
        FIRST_TIME, FIRST_TIME_VERSION, NORMAL;
    }

    /**
     * The app version code (not the version name!) that was used on the last
     * start of the app.
     */
    private static final String LAST_APP_VERSION = "1";

    /**
     * Caches the result of {@link #checkAppStart(Context context, Preferences preferences)}. To allow idempotent method
     * calls.
     */
    private static AppStart appStart = null;

    /**
     * Finds out started for the first time (ever or in the current version).
     *
     * @return the type of app start
     */
    public static AppStart checkAppStart(Context context, Preferences preferences) {
        PackageInfo pInfo;

        try {
            pInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
            int lastVersionCode = preferences.getAppVersion();
            // String versionName = pInfo.versionName;
            int currentVersionCode = pInfo.versionCode;
            appStart = checkAppStart(currentVersionCode, lastVersionCode);

            // Update version in preferences
            preferences.setAppVersion(currentVersionCode);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.w("Unable to determine current app version from package manager. Defensively assuming normal app start.");
        }
        return appStart;
    }

    private static AppStart checkAppStart(int currentVersionCode, int lastVersionCode) {
        if (lastVersionCode == -1) {
            return AppStart.FIRST_TIME;
        } else if (lastVersionCode < currentVersionCode) {
            return AppStart.FIRST_TIME_VERSION;
        } else if (lastVersionCode > currentVersionCode) {
            Logger.w("Current version code (" + currentVersionCode
                    + ") is less then the one recognized on last startup ("
                    + lastVersionCode
                    + "). Defensively assuming normal app start.");
            return AppStart.NORMAL;
        } else {
            return AppStart.NORMAL;
        }
    }

    public static int getBackgroundColor(String color) {
        int intColor = Color.TRANSPARENT;
        if (color != null && !TextUtils.isEmpty(color)) {
            try {
                intColor = Color.parseColor(color);
            } catch (IllegalArgumentException e) {
                Logger.e("background color <%s> failed", color, e);
            }
        }
        return intColor;
    }

    public static String[] getAllPhotoColumns() {
        return new String[] {
                PhotoColumns.PHOTO_ID,
                PhotoColumns.PHOTO_CREATED_AT,
                PhotoColumns.PHOTO_WIDTH,
                PhotoColumns.PHOTO_HEIGHT,
                PhotoColumns.PHOTO_COLOR,
                PhotoColumns.PHOTO_DOWNLOADS,
                PhotoColumns.PHOTO_LIKES,
                PhotoColumns.PHOTO_LIKED_BY_USER,
                PhotoColumns.EXIF_MAKE,
                PhotoColumns.EXIF_MODEL,
                PhotoColumns.EXIF_APERTURE,
                PhotoColumns.EXIF_EXPOSURE_TIME,
                PhotoColumns.EXIF_FOCAL_LENGTH,
                PhotoColumns.EXIF_ISO,
                PhotoColumns.LOCATION_COUNTRY,
                PhotoColumns.LOCATION_CITY,
                PhotoColumns.LOCATION_LATITUDE,
                PhotoColumns.LOCATION_LONGITUDE,
                PhotoColumns.URLS_RAW,
                PhotoColumns.URLS_FULL,
                PhotoColumns.URLS_REGULAR,
                PhotoColumns.URLS_SMALL,
                PhotoColumns.URLS_THUMB,
                PhotoColumns.LINKS_SELF,
                PhotoColumns.LINKS_HTML,
                PhotoColumns.LINKS_DOWNLOAD,
                PhotoColumns.USER_ID,
                PhotoColumns.USER_USERNAME,
                PhotoColumns.USER_NAME,
                PhotoColumns.USER_PORTFOLIO_URL,
                PhotoColumns.USER_PROFILE_IMAGE_SMALL,
                PhotoColumns.USER_PROFILE_IMAGE_MEDIUM,
                PhotoColumns.USER_PROFILE_IMAGE_LARGE,
                PhotoColumns.USER_LINKS_SELF,
                PhotoColumns.USER_LINKS_HTML,
                PhotoColumns.USER_LINKS_PHOTOS,
                PhotoColumns.USER_LINKS_LIKES
        };
    }
}
