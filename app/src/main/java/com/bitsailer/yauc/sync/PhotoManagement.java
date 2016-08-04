package com.bitsailer.yauc.sync;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Pair;

import com.bitsailer.yauc.api.UnsplashAPI;
import com.bitsailer.yauc.api.UnsplashService;
import com.bitsailer.yauc.api.model.Photo;
import com.bitsailer.yauc.api.model.SimplePhoto;
import com.bitsailer.yauc.data.ContentValuesBuilder;
import com.bitsailer.yauc.data.PhotoColumns;
import com.bitsailer.yauc.data.PhotoProvider;
import com.bitsailer.yauc.event.UserDataLoadedEvent;
import com.bitsailer.yauc.event.UserDataRemovedEvent;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import retrofit2.Call;

/**
 * An {@link IntentService} subclass for handling asynchronous actions that
 * fetch data or do crud database operations.
 */
public class PhotoManagement extends IntentService {
    private static final String ACTION_UPDATE_USERS_PHOTOS = "com.bitsailer.yauc.sync.action.update_users_photos";
    private static final String ACTION_CLEANUP_USERS_PHOTOS = "com.bitsailer.yauc.sync.action.cleanup_users_photos";
    private static final String ACTION_ADD_PHOTO = "com.bitsailer.yauc.sync.action.add_photo";

    private static final String EXTRA_USERNAME = "com.bitsailer.yauc.sync.extra.username";
    private static final String EXTRA_ACCESS_TOKEN = "com.bitsailer.yauc.sync.extra.access_token";
    private static final String EXTRA_PHOTO = "com.bitsailer.yauc.sync.extra.photo";

    public PhotoManagement() {
        super("PhotoManagement");
    }

    /**
     * Starts this service to perform update of users photos with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @param context The context
     * @param username username whose photos should be updated
     * @param accessToken the token we got after sign in
     * @see IntentService
     */
    public static void updateUsersPhotos(Context context, String username, String accessToken) {
        Intent intent = new Intent(context, PhotoManagement.class);
        intent.setAction(ACTION_UPDATE_USERS_PHOTOS);
        intent.putExtra(EXTRA_USERNAME, username);
        intent.putExtra(EXTRA_ACCESS_TOKEN, accessToken);
        context.startService(intent);
    }

    /**
     * Starts this service to perform cleanup of users photos with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @param context The context
     * @param username username whose photos should be updated
     * @see IntentService
     */
    public static void cleanupUsersPhotos(Context context, String username) {
        Intent intent = new Intent(context, PhotoManagement.class);
        intent.setAction(ACTION_CLEANUP_USERS_PHOTOS);
        intent.putExtra(EXTRA_USERNAME, username);
        context.startService(intent);
    }

    /**
     * Starts this service to perform handling of new photo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: create extra bundle with photo
    public static void addPhoto(Context context, Photo photo) {
        Intent intent = new Intent(context, PhotoManagement.class);
        intent.setAction(ACTION_ADD_PHOTO);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_USERS_PHOTOS.equals(action)) {
                final String username = intent.getStringExtra(EXTRA_USERNAME);
                final String accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN);
                handleUpdateUsersPhotos(username, accessToken);
            } else if (ACTION_CLEANUP_USERS_PHOTOS.equals(action)) {
                final String username = intent.getStringExtra(EXTRA_USERNAME);
                handleCleanupUsersPhotos(username);
            } else if (ACTION_ADD_PHOTO.equals(action)) {
                // TODO: get extra bundle with photo
                //handleAddPhoto(photo);
            }
        }
    }

    private int insertList(List<SimplePhoto> list) {
        if (list != null && !list.isEmpty()) {
            Vector<ContentValues> values = new Vector<ContentValues>();
            for (SimplePhoto photo : list) {
                values.add(ContentValuesBuilder.from(photo));
            }

            if (values.size() > 0) {
                ContentValues[] contentValues = new ContentValues[values.size()];
                values.toArray(contentValues);
                return getContentResolver().bulkInsert(PhotoProvider.Uri.BASE, contentValues);
            }
        }
        return 0;
    }

    /**
     * Fetch favorites and own photos of given user and save them
     * to the database.
     * @param username given username of user
     * @param accessToken the access token from sign in
     */
    private void handleUpdateUsersPhotos(String username, String accessToken) {

        // to be sure delete existing personal photos
        cleanupUsersPhotos(username);

        // get api service
        UnsplashAPI api = UnsplashService.create(UnsplashAPI.class, accessToken);
        List<SimplePhoto> list = new ArrayList<>();

        int fetched = UnsplashAPI.MAX_PER_PAGE;

        // get list of favorites
        for (int page = 1; fetched == UnsplashAPI.MAX_PER_PAGE; page++) {

            Call<List<SimplePhoto>> listFavoritesCall = api
                    .listFavoritePhotos(username, page, UnsplashAPI.MAX_PER_PAGE, null);
            try {
                List<SimplePhoto> part = listFavoritesCall.execute().body();
                fetched = part.size();
                list.addAll(part);
            } catch (IOException e) {
                Logger.e(e.getMessage());
            }
        }
        int favorites = insertList(list);
        Logger.d("%d favorites inserted", favorites);

        // get list of own photos
        fetched = UnsplashAPI.MAX_PER_PAGE;
        list.clear();

        // get list of favorites
        for (int page = 1; fetched == UnsplashAPI.MAX_PER_PAGE; page++) {

            Call<List<SimplePhoto>> listOwnPhotosCall = api
                    .listUsersPhotos(username, page, UnsplashAPI.MAX_PER_PAGE, null);
            try {
                List<SimplePhoto> part = listOwnPhotosCall.execute().body();
                fetched = part.size();
                list.addAll(part);
            } catch (IOException e) {
                Logger.e(e.getMessage());
            }
        }
        int own = insertList(list);
        Logger.d("%d own photos inserted", own);
        EventBus.getDefault().post(new UserDataLoadedEvent(favorites, own));
    }

    /**
     * Delete favorites and own photos of given user (he/she signed out).
     */
    private void handleCleanupUsersPhotos(String username) {
        Pair<Integer, Integer> deleted = cleanupUsersPhotos(username);
        EventBus.getDefault().post(new UserDataRemovedEvent(deleted.first, deleted.second));
    }

    private Pair<Integer, Integer> cleanupUsersPhotos(String username) {
        int deletedFavorites = 0;
        int deletedOwn = 0;
        // delete favorites
        deletedFavorites = getContentResolver()
                .delete(PhotoProvider.Uri.BASE,
                        PhotoColumns.PHOTO_LIKED_BY_USER + " = ?",
                        new String[] {"1"});

        if (username != null && !TextUtils.isEmpty(username)) {
            // delete own photos
            deletedOwn = getContentResolver()
                    .delete(PhotoProvider.Uri.withUsername(username),
                            null, null);
        }
        Logger.d("deleted %d favorites and %d own photos", deletedFavorites, deletedOwn);
        return new Pair<Integer, Integer>(deletedFavorites, deletedOwn);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleAddPhoto(Photo photo) {
        // TODO: Handle action
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
