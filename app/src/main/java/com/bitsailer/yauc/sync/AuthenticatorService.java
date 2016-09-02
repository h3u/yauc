package com.bitsailer.yauc.sync;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.bitsailer.yauc.BuildConfig;

/**
 * Service to create AccountAuthenticator and Account.
 */
public class AuthenticatorService extends Service {

    private static final String SYNC_ACCOUNT_NAME = "Sync with Unsplash";

    private AccountAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new AccountAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

    public static Account getAccount() {
        return new Account(SYNC_ACCOUNT_NAME, BuildConfig.ACCOUNT_TYPE);
    }
}
