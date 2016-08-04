package com.bitsailer.yauc.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * SyncService
 * See: https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 */

public class SyncService extends Service {

    // Storage for an instance of the sync adapter
    private static SyncAdapter sSyncSyncAdapter = null;

    // Object to use as a thread-safe lock
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncSyncAdapter == null) {
                sSyncSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncSyncAdapter.getSyncAdapterBinder();
    }
}
