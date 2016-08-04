package com.bitsailer.yauc.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.bitsailer.yauc.BuildConfig;
import com.bitsailer.yauc.R;
import com.bitsailer.yauc.api.UnsplashAPI;
import com.bitsailer.yauc.api.UnsplashService;
import com.bitsailer.yauc.api.model.AccessToken;
import com.bitsailer.yauc.Preferences;
import com.orhanobut.logger.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manage user sign in via implicit intent (Intent.ACTION_VIEW)
 */
public class LoginActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_SUCCESS = "intent_extra_success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startUnsplashAuthorizationActivity();
    }

    /**
     * Catch the returning authorization code from startUnsplashAuthorizationActivity()
     * and when it's successful request the the auth token.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // the intent filter defined in AndroidManifest will handle the return from ACTION_VIEW intent
        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith(getString(R.string.unsplash_redirect_uri_scheme))) {
            String code = uri.getQueryParameter("code");
            if (code != null && !TextUtils.isEmpty(code)) {
                // save code - not sure if it's needed
                Preferences.get(this).saveAuthorizationCode(code);
                // get access token
                Call<AccessToken> call = UnsplashService
                        .createAuth(UnsplashAPI.class, UnsplashAPI.OAUTH_URL)
                        .getAccessToken(
                                BuildConfig.UNSPLASH_CLIENT_ID,
                                BuildConfig.UNSPLASH_CLIENT_SECRET,
                                getString(R.string.unsplash_authorization_redirect_uri),
                                code, UnsplashAPI.AUTHORIZATION_GRANT_TYPE
                        );

                call.enqueue(new Callback<AccessToken>() {
                    @Override
                    public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                        // save access token
                        if (response != null) {
                            Preferences.get(LoginActivity.this).setAccessToken(response.body());
                            startMainActivity(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<AccessToken> call, Throwable t) {
                        startMainActivity(false);
                        Logger.e(t.getMessage());
                    }
                });
            } else if (uri.getQueryParameter("error") != null) {
                // show error message
                startMainActivity(false);
                Logger.e(uri.getQueryParameter("error"));
            }
        }
    }

    private void startMainActivity(boolean success) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(INTENT_EXTRA_SUCCESS, success);
        startActivity(intent);
    }

    /**
     * Start browser with ACTION_VIEW and authorization url
     */
    private void startUnsplashAuthorizationActivity() {
        Uri site = Uri.parse(String.format(
                getString(R.string.unsplash_authorization_url),
                BuildConfig.UNSPLASH_CLIENT_ID,
                UnsplashAPI.PERMISSION_SCOPE));
        Intent getAuthCode = new Intent(Intent.ACTION_VIEW, site);
        startActivity(getAuthCode);
    }
}
