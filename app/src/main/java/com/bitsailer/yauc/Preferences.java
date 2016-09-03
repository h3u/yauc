package com.bitsailer.yauc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bitsailer.yauc.api.model.AccessToken;
import com.bitsailer.yauc.api.model.User;

/**
 * Store some user attributes, tokens and auth code to
 * SharedPreferences.
 */
public class Preferences {

    private static final String APP_PREF = "yauc_prefs";
    private static final String KEY_APP_VERSION = "key_app_version";
    private static final String KEY_ACCESS_TOKEN = "key_access_token";
    private static final String KEY_ACCESS_TOKEN_CREATED_AT = "key_access_token_created_at";
    private static final String KEY_REFRESH_TOKEN = "key_refresh_token";
    private static final String KEY_AUTH_CODE = "key_auth_code";
    private static final String KEY_USER_ID = "key_user_id";
    private static final String KEY_USER_NAME = "key_user_name";
    private static final String KEY_USER_USERNAME = "key_user_username";
    private static final String KEY_USER_AVATAR = "key_user_avatar";

    private final SharedPreferences mPreferences;
    private Boolean mAuthenticated = false;

    private static volatile Preferences sInstance;

    public static Preferences get(Context context) {
        if (sInstance == null) {
            synchronized (Preferences.class) {
                sInstance = new Preferences(context);
            }
        }
        return sInstance;
    }

    private Preferences(Context context) {
        mPreferences = context.getApplicationContext()
                .getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        mAuthenticated = !TextUtils.isEmpty(mPreferences.getString(KEY_ACCESS_TOKEN, null));
    }

    @SuppressLint("CommitPrefEdits")
    public void setAppVersion(int version) {
        mPreferences.edit().putInt(KEY_APP_VERSION, version).commit();
    }

    public int getAppVersion() {
        return mPreferences.getInt(KEY_APP_VERSION, -1);
    }

    public String getAccessToken() {
        return mPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    @SuppressWarnings("unused")
    public String getUserAvatar() {
        return mPreferences.getString(KEY_USER_AVATAR, null);
    }

    @SuppressWarnings("unused")
    public String getUserName() {
        return mPreferences.getString(KEY_USER_NAME, null);
    }

    public String getUserId() {
        return mPreferences.getString(KEY_USER_ID, null);
    }

    public String getUserUsername() {
        return mPreferences.getString(KEY_USER_USERNAME, null);
    }

    public Boolean isAuthenticated() {
        return mAuthenticated;
    }

    public void saveAuthorizationCode(String authorizationCode) {
        if (!TextUtils.isEmpty(authorizationCode)) {
            mPreferences.edit().putString(KEY_AUTH_CODE, authorizationCode).apply();
        }
    }

    @SuppressLint("CommitPrefEdits")
    public void setAccessToken(@NonNull AccessToken response) {
        SharedPreferences.Editor editor = mPreferences.edit();
        if (!TextUtils.isEmpty(response.getAccessToken())) {
            mAuthenticated = true;
            editor.putString(KEY_ACCESS_TOKEN, response.getAccessToken());
        }
        if (response.getCreatedAt() != null) {
            editor.putInt(KEY_ACCESS_TOKEN_CREATED_AT, response.getCreatedAt());
        }
        if (!TextUtils.isEmpty(response.getRefreshToken())) {
            editor.putString(KEY_REFRESH_TOKEN, response.getRefreshToken());
        }
        editor.commit();
    }

    public void setUser(User user) {
        if (user != null) {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(KEY_USER_ID, user.getId());
            editor.putString(KEY_USER_NAME, user.getName());
            editor.putString(KEY_USER_USERNAME, user.getUsername());
            editor.putString(KEY_USER_AVATAR, user.getProfileImage().getSmall());
            editor.apply();
        }
    }

    @SuppressLint("CommitPrefEdits")
    public void destroyAuthorization() {
        mAuthenticated = false;
        mPreferences.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_ACCESS_TOKEN_CREATED_AT)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_USER_AVATAR)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_USERNAME).commit();
    }
}
