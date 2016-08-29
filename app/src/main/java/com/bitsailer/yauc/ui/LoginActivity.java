package com.bitsailer.yauc.ui;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bitsailer.yauc.BuildConfig;
import com.bitsailer.yauc.Preferences;
import com.bitsailer.yauc.R;
import com.bitsailer.yauc.api.UnsplashAPI;
import com.bitsailer.yauc.api.UnsplashService;
import com.bitsailer.yauc.api.model.AccessToken;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manage user sign in with a web view that displays authorization
 * and sign in at https://unsplash.com.
 */
@SuppressWarnings("unused")
public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.webView)
    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Uri unsplashAuthorization = Uri.parse(String.format(
                getString(R.string.unsplash_authorization_url),
                BuildConfig.UNSPLASH_CLIENT_ID,
                UnsplashAPI.PERMISSION_SCOPE));

        CookieManager cookieManager = CookieManager.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean aBoolean) {
                }
            });
        } else {
            //noinspection deprecation
            cookieManager.removeAllCookie();
        }

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    Uri uri = Uri.parse(url);
                    if (uri.getScheme().equals("yauc") && uri.getHost().equals("redirect-uri")) {
                        parseAndAuthorize(uri);
                    } else {
                        return false; // continue redirect
                    }
                } catch (Exception e) {
                    Logger.e(e.getMessage());
                }
                return true; // stop redirection
            }
        });
        mWebView.loadUrl(unsplashAuthorization.toString());
    }

    /**
     * Parse the response from unsplash.com for authorization
     * code, request an access token and finish activity.
     *
     * @param uri the redirect uri
     */
    private void parseAndAuthorize(Uri uri) {
        String code = uri.getQueryParameter("code");

        if (code != null && !TextUtils.isEmpty(code)) {
            // save code - not sure if it's needed
            Preferences.get(this).saveAuthorizationCode(code);
            // get access token
            Call<AccessToken> call = UnsplashService
                    .createAuth(UnsplashAPI.class)
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
                        setResult(RESULT_OK);
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<AccessToken> call, Throwable t) {
                    Logger.e(t.getMessage());
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
        } else if (uri.getQueryParameter("error") != null) {
            // log error message
            Logger.e(uri.getQueryParameter("error"));
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}
