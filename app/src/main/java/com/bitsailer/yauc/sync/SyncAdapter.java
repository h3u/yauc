package com.bitsailer.yauc.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;

import com.bitsailer.yauc.R;
import com.bitsailer.yauc.api.UnsplashAPI;
import com.bitsailer.yauc.api.UnsplashService;
import com.bitsailer.yauc.api.model.SimplePhoto;
import com.bitsailer.yauc.data.ContentValuesBuilder;
import com.bitsailer.yauc.data.PhotoColumns;
import com.bitsailer.yauc.data.PhotoProvider;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;

import static android.content.Context.ACCOUNT_SERVICE;

/**
 * SyncAdapter that fetches new photos from unsplash api periodically in
 * four hour intervals.
 * Created by Uli Wucherer (u.wucherer@gmail.com) on 26/07/16.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    // Interval: 60 seconds * 60 minutes * 4 = 4 hours
    private static final long SYNC_INTERVAL = 60 *  60 * 4;
    // Flextime: 60 seconds * 60 minutes = 1 hour
    private static final long SYNC_FLEXTIME = 60 *  60;

    private static final int MAX_PAGES = 4;

    public static final String ACTION_DATA_UPDATED =
            "com.bitsailer.yauc.sync.ACTION_DATA_UPDATED";
    public static final String EXRTA_NUM_INSERTED = "extra_num_inserted";
    private static final String KEY_INITIAL_SYNC = "key_initial_sync";

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize, false);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        int inserted = 0;
        int perPage = UnsplashAPI.MAX_PER_PAGE;
        boolean firstSync = extras.getBoolean(KEY_INITIAL_SYNC, false);
        int sumInsertedLastSync = 0;

        // get api service
        UnsplashAPI api = UnsplashService.create(UnsplashAPI.class);

        /**
         * fetch new photos and insert them until the number of
         * inserted differ from items per page.
         * This occurs when the insert method find already stored
         * photos (from last/first sync). Except from this the sync
         * should stop after two pages (first sync) or max pages (user
         * killed data, or inserted has accidentally same size when hitting
         * the last photo in database.
        */
        for (int page = 1; page == 1 || inserted == perPage; page++) {
            if ((firstSync && page > 2) || (page >= MAX_PAGES)) {
                // for the first sync stop after second page
                break;
            }

            // use default order by (using "null") to get latest photos
            Call<List<SimplePhoto>> listCall = api.listPhotos(page, perPage, null);
            try {
                List<SimplePhoto> list = listCall.execute().body();
                inserted = insert(contentProviderClient, list);
                sumInsertedLastSync += inserted;
            } catch (IOException e) {
                Logger.e(e.getMessage());
            }
        }

        // todo: start widget update
        Logger.d("sum inserted %d", sumInsertedLastSync);
        updateWidgets(sumInsertedLastSync);

        // todo: add clean up of photos (not favorites/own)
        // yauc should not run into issues with thousands of photos ...
    }

    /**
     * Insert and count list of photos.
     *
     * @param provider the content provider
     * @param list of photos or simple photos (favorites, own photos)
     * @return number of inserted items
     */
    private int insert(ContentProviderClient provider, List<SimplePhoto> list) {
        int inserted = 0;

        if (null != list && !list.isEmpty()) {
            for (SimplePhoto item : list) {
                // check exists
                if (!exists(provider, item)) {
                    try {
                        ContentValues cv = ContentValuesBuilder.from(item);
                        provider.insert(PhotoProvider.Uri.BASE, cv);
                    } catch (Exception e) {
                        Logger.e(e, "insert failed");
                    }
                    inserted++;
                }
            }
        }
        return inserted;
    }

    private boolean exists(ContentProviderClient contentProviderClient, SimplePhoto photo) {
        String[] projection = new String[]{ PhotoColumns.PHOTO_ID};
        boolean have = false;
        try {
            Cursor cursor = contentProviderClient.query(
                    PhotoProvider.Uri.withId(photo.getId()), projection, null, null, null);
            if (null != cursor) {
                have = cursor.moveToNext();
                cursor.close();
            }
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
        return have;
    }

    /**
     * Run synchronisation immediately, e.g. when pressing refresh
     *
     * @param context activity context
     */
    public static void syncNow(Context context) {
        sync(context, new Bundle());
    }

    private static void sync(Context context, Bundle bundle) {
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(AuthenticatorService.getAccount(),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static void CreateSyncAccount(Context context) {

        Account account = AuthenticatorService.getAccount();

        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(account, null, null)) {
            onAccountCreated(account, context);
        }
    }

    private static void onAccountCreated(Account account, Context context) {

        // add periodic synchronisation
        SyncRequest request = new SyncRequest.Builder()
                .syncPeriodic(SYNC_INTERVAL, SYNC_FLEXTIME)
                .setSyncAdapter(account, context.getString(R.string.content_authority))
                .setExtras(new Bundle()).build();
        ContentResolver.requestSync(request);

        ContentResolver.setSyncAutomatically(account,
                context.getString(R.string.content_authority), true);

        // initial sync
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_INITIAL_SYNC, true);
        sync(context, bundle);
    }

    private void updateWidgets(int insertedPhotos) {
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName())
                .putExtra(EXRTA_NUM_INSERTED, insertedPhotos);
        context.sendBroadcast(dataUpdatedIntent);
    }
}
