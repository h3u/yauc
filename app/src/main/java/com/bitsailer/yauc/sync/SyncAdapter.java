package com.bitsailer.yauc.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.bitsailer.yauc.R;
import com.bitsailer.yauc.api.UnsplashAPI;
import com.bitsailer.yauc.api.UnsplashService;
import com.bitsailer.yauc.api.model.SimplePhoto;
import com.bitsailer.yauc.data.ContentValuesBuilder;
import com.bitsailer.yauc.data.PhotoColumns;
import com.bitsailer.yauc.data.PhotoProvider;
import com.bitsailer.yauc.ui.MainActivity;
import com.bumptech.glide.Glide;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
    public static final String EXTRA_NUM_INSERTED = "extra_num_inserted";
    private static final String KEY_INITIAL_SYNC = "key_initial_sync";
    private static final String KEY_MANUAL_SYNC = "key_manual_sync";

    private static final int NEW_PHOTOS_NOTIFICATION_ID = 0;

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.bitsailer.yauc.sync.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.bitsailer.yauc.sync.extra.REFRESHING";


    public SyncAdapter(Context context) {
        super(context, true, false);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        int inserted = 0;
        int perPage = UnsplashAPI.MAX_PER_PAGE;
        boolean firstSync = extras.getBoolean(KEY_INITIAL_SYNC, false);
        boolean manualSync = extras.getBoolean(KEY_MANUAL_SYNC, false);
        int sumInsertedLastSync = 0;
        SimplePhoto latestPhoto = null;

        // send broadcast to notify swipe refresh layout
        getContext().sendBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));

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
                if (page == 1) {
                    latestPhoto = list.get(0);
                }
            } catch (IOException e) {
                Logger.e(e.getMessage());
            }
        }

        Logger.d("sum inserted %d", sumInsertedLastSync);
        // start widget update
        updateWidgets(sumInsertedLastSync);
        if (!firstSync && !manualSync) {
            createNotification(sumInsertedLastSync, latestPhoto);
        }

        // send broadcast to notify swipe refresh layout
        getContext().sendBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));

        // clean up of photos (not favorites/own)
        // yauc should not run into issues with thousands of photos ...
        PhotoManagement.cleanupNewPhotos(getContext());
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
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_MANUAL_SYNC, true);
        sync(context, bundle);
    }

    private static void sync(Context context, Bundle bundle) {
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(AuthenticatorService.getAccount(),
                context.getString(R.string.authorities), bundle);
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
                .setSyncAdapter(account, context.getString(R.string.authorities))
                .setExtras(new Bundle()).build();
        ContentResolver.requestSync(request);

        ContentResolver.setSyncAutomatically(account,
                context.getString(R.string.authorities), true);

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
                .putExtra(EXTRA_NUM_INSERTED, insertedPhotos);
        context.sendBroadcast(dataUpdatedIntent);
    }

    private void createNotification(int insertedPhotos, SimplePhoto latestPhoto) {
        if (insertedPhotos > 0) {
            int accentColor;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                accentColor = getContext().getResources()
                        .getColor(R.color.colorAccent, getContext().getTheme());
            } else {
                //noinspection deprecation
                accentColor = getContext().getResources().getColor(R.color.colorAccent);
            }

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getContext())
                            .setAutoCancel(true)
                            .setNumber(insertedPhotos)
                            .setSmallIcon(R.drawable.ic_camera_yauc)
                            .setColor(accentColor)
                            .setLights(accentColor, 1000, 5000)
                            .setContentTitle(getContext().getString(R.string.notification_title))
                            .setContentText(String.format(getContext().getResources()
                                    .getQuantityString(R.plurals.notification_text, insertedPhotos), insertedPhotos));

            // Retrieve photo for the big picture notification style
            if (latestPhoto != null) {
                int largeIconWidth = getContext().getResources()
                        .getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
                int largeIconHeight = getContext().getResources()
                        .getDimensionPixelSize(android.R.dimen.notification_large_icon_height);

                Bitmap largeIcon;
                try {
                    largeIcon = Glide.with(getContext())
                            .load(latestPhoto.getUrls().getThumb())
                            .asBitmap()
                            .error(R.drawable.lens)
                            .fitCenter()
                            .into(largeIconWidth, largeIconHeight).get();
                } catch (InterruptedException | ExecutionException e) {
                    Logger.e("Error retrieving large icon from %s",latestPhoto.getUrls().getThumb() , e);
                    largeIcon = BitmapFactory.decodeResource(
                            getContext().getResources(), R.drawable.lens);
                }

                NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
                bigPictureStyle.bigPicture(largeIcon)
                        .setBigContentTitle(getContext().getString(R.string.notification_title))
                        .setSummaryText(String.format(getContext().getResources()
                                .getQuantityString(R.plurals.notification_text, insertedPhotos), insertedPhotos));
                mBuilder.setStyle(bigPictureStyle);
            }

            // open MainActivity with default tab new photos
            Intent resultIntent = new Intent(getContext(), MainActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager =
                    (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NEW_PHOTOS_NOTIFICATION_ID, mBuilder.build());
        }
    }
}
