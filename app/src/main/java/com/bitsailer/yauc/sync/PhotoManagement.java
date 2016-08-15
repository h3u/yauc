package com.bitsailer.yauc.sync;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.bitsailer.yauc.Util;
import com.bitsailer.yauc.api.UnsplashAPI;
import com.bitsailer.yauc.api.UnsplashService;
import com.bitsailer.yauc.api.model.Photo;
import com.bitsailer.yauc.api.model.SimplePhoto;
import com.bitsailer.yauc.data.ContentValuesBuilder;
import com.bitsailer.yauc.data.PhotoColumns;
import com.bitsailer.yauc.data.PhotoProvider;
import com.bitsailer.yauc.event.PhotoDataLoadedEvent;
import com.bitsailer.yauc.event.PhotoLikedEvent;
import com.bitsailer.yauc.event.PhotoUnlikedEvent;
import com.bitsailer.yauc.event.UserDataLoadedEvent;
import com.bitsailer.yauc.event.UserDataRemovedEvent;
import com.bitsailer.yauc.provider.values.PhotosValuesBuilder;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous actions that
 * fetch data or do crud database operations mainly to keep local database and
 * api.unsplash.com in sync.
 */
public class PhotoManagement extends IntentService {
    private static final String ACTION_UPDATE_USERS_PHOTOS = "com.bitsailer.yauc.sync.action.update_users_photos";
    private static final String ACTION_CLEANUP_USERS_PHOTOS = "com.bitsailer.yauc.sync.action.cleanup_users_photos";
    private static final String ACTION_AMEND_PHOTO = "com.bitsailer.yauc.sync.action.amend_photo";
    private static final String ACTION_LIKE_PHOTO = "com.bitsailer.yauc.sync.action.like_photo";
    private static final String ACTION_UNLIKE_PHOTO = "com.bitsailer.yauc.sync.action.unlike_photo";
    private static final String ACTION_ADD_PHOTO = "com.bitsailer.yauc.sync.action.add_photo";

    private static final String EXTRA_USERNAME = "com.bitsailer.yauc.sync.extra.username";
    private static final String EXTRA_PHOTO_ID = "com.bitsailer.yauc.sync.extra.photo_id";
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
     * @see IntentService
     */
    public static void updateUsersPhotos(Context context, String username) {
        Intent intent = new Intent(context, PhotoManagement.class);
        intent.setAction(ACTION_UPDATE_USERS_PHOTOS);
        intent.putExtra(EXTRA_USERNAME, username);
        context.startService(intent);
    }

    /**
     * Removes all photos related to user (favorite/own)
     *
     * @param context calling context
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
     * Starts this service to fetch full data for a given photo.
     *
     * @param context calling context
     * @param photoId id of photo to amend data
     * @see IntentService
     */
    public static void amendPhoto(Context context, String photoId) {
        Intent intent = new Intent(context, PhotoManagement.class);
        intent.setAction(ACTION_AMEND_PHOTO);
        intent.putExtra(EXTRA_PHOTO_ID, photoId);
        context.startService(intent);
    }

    /**
     * Like a given photo. This marks the photo as favorite in database and
     * sends a post to unsplash api.
     *
     * @param context calling context
     * @param photoId id of photo to like
     * @see IntentService
     */
    public static void likePhoto(Context context, String photoId) {
        Intent intent = new Intent(context, PhotoManagement.class);
        intent.setAction(ACTION_LIKE_PHOTO);
        intent.putExtra(EXTRA_PHOTO_ID, photoId);
        context.startService(intent);
    }

    /**
     * Unlike a given photo. This removes the photo as favorite in database and
     * sends a post to unsplash api.
     *
     * @param context calling context
     * @param photoId id of photo to unlike
     * @see IntentService
     */
    public static void unlikePhoto(Context context, String photoId) {
        Intent intent = new Intent(context, PhotoManagement.class);
        intent.setAction(ACTION_UNLIKE_PHOTO);
        intent.putExtra(EXTRA_PHOTO_ID, photoId);
        context.startService(intent);
    }

    /**
     * Add photo to users own photos.
     *
     * @param context calling context
     * @param photo the photo to add
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
                handleUpdateUsersPhotos(username);
            } else if (ACTION_CLEANUP_USERS_PHOTOS.equals(action)) {
                final String username = intent.getStringExtra(EXTRA_USERNAME);
                handleCleanupUsersPhotos(username);
            } else if (ACTION_AMEND_PHOTO.equals(action)) {
                final String photoId = intent.getStringExtra(EXTRA_PHOTO_ID);
                handleAmendPhoto(photoId);
            } else if (ACTION_LIKE_PHOTO.equals(action)) {
                final String photoId = intent.getStringExtra(EXTRA_PHOTO_ID);
                handleLikePhoto(photoId);
            } else if (ACTION_UNLIKE_PHOTO.equals(action)) {
                final String photoId = intent.getStringExtra(EXTRA_PHOTO_ID);
                handleUnlikePhoto(photoId);
            } else if (ACTION_ADD_PHOTO.equals(action)) {
                // TODO: get extra bundle with photo
                //handleAddPhoto(photo);
            }
        }
    }

    /**
     * Iterate the list and check if item exists, insert if not
     * and amend data if yes.
     *
     * @param list list of photos
     * @return number of inserted items
     */
    private int insertList(List<SimplePhoto> list) {
        int inserted = 0;
        if (list != null && !list.isEmpty()) {

            for (SimplePhoto item : list) {
                String log = item.getLinks().getHtml();
                // check if exists
                if (!hasPhoto(item.getId())) {
                    ContentValues values = ContentValuesBuilder.from(item);
                    Uri uri = getContentResolver().insert(PhotoProvider.Uri.BASE, values);
                    if (uri != null) {
                        inserted++;
                    }
                } else {
                    // update existing
                    handleAmendPhoto(item.getId());
                }
            }
        }
         return inserted;
    }

    /**
     * Fetch favorites and own photos of given user and save them
     * to the database.
     * @param username given username of user
     */
    private void handleUpdateUsersPhotos(String username) {

        // get api service
        UnsplashAPI api = UnsplashService.create(UnsplashAPI.class, this);
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
        Logger.i("%d favorites inserted", favorites);

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
        Logger.i("%d own photos inserted", own);
        EventBus.getDefault().post(new UserDataLoadedEvent(favorites, own));
    }

    /**
     * Delete favorites and own photos of given user (he/she signed out).
     *
     * @param username username of user to cleanup
     */
    private void handleCleanupUsersPhotos(String username) {
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
        Logger.i("deleted %d favorites and %d own photos", deletedFavorites, deletedOwn);
        EventBus.getDefault().post(new UserDataRemovedEvent(deletedFavorites, deletedOwn));
    }

    /**
     * Handle action to amend photo.
     * This only fetches and updates if the photo data is incomplete.
     *
     * @param photoId id of photo to amend data
     */
    private void handleAmendPhoto(String photoId) {
        Photo photo = getById(photoId);
        if (photo != null && photo.isIncomplete()) {
            // get api service
            UnsplashAPI api = UnsplashService.create(UnsplashAPI.class, this);
            Call<Photo> call = api.getPhoto(photoId);
            try {
                Photo photoUpdate = call.execute().body();
                if (photoUpdate != null) {
                    ContentValues values = ContentValuesBuilder.from(photoUpdate);
                    int num = getContentResolver().update(PhotoProvider.Uri.withId(photoId), values, null, null);
                    Logger.d("%d photos updated", num);
                    EventBus.getDefault().post(new PhotoDataLoadedEvent(photoId));
                }
            } catch (IOException e) {
                Logger.e(e.getMessage());
            }
        }
    }

    /**
     * Handle action like photo.
     * Post the "like" to unsplash api and save it database.
     *
     * @param photoId id of photo to like
     */
    private void handleLikePhoto(String photoId) {
        // get api service
        UnsplashAPI api = UnsplashService.create(UnsplashAPI.class, this);
        Call<ResponseBody> call = api.likePhoto(photoId);
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                Logger.d("photo %s liked", photoId);
                Photo photo = getById(photoId);
                if (photo != null) {
                    PhotosValuesBuilder builder = new PhotosValuesBuilder();
                    ContentValues values = builder
                            .photoLikedByUser(1)
                            .photoLikes(photo.getLikes() + 1L)
                            .values();
                    getContentResolver().update(PhotoProvider.Uri.withId(photoId), values, null, null);
                }
                EventBus.getDefault().post(new PhotoLikedEvent(photoId));
            }
        } catch (IOException e) {
            Logger.e(e.getMessage());
        }
    }

    /**
     * Handle action unlike photo.
     * Delete the "like" at unsplash api and remove it from database.
     *
     * @param photoId id of photo to unlike
     */
    private void handleUnlikePhoto(String photoId) {
        // get api service
        UnsplashAPI api = UnsplashService.create(UnsplashAPI.class, this);
        Call<ResponseBody> call = api.unlikePhoto(photoId);
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                Logger.i("photo %s unliked", photoId);
                Photo photo = getById(photoId);
                if (photo != null) {
                    PhotosValuesBuilder builder = new PhotosValuesBuilder();
                    ContentValues values = builder
                            .photoLikedByUser(0)
                            .photoLikes(photo.getLikes() - 1L)
                            .values();
                    getContentResolver().update(PhotoProvider.Uri.withId(photoId), values, null, null);
                }
                EventBus.getDefault().post(new PhotoUnlikedEvent(photoId));
            }
        } catch (IOException e) {
            Logger.e(e.getMessage());
        }
    }

    /**
     * Does given photo exist in local database?
     *
     * @param photoId id of photo to check
     * @return true if exist, false otherwise
     */
    private boolean hasPhoto(String photoId) {
        String[] projection = new String[]{ PhotoColumns.PHOTO_ID};
        boolean have = false;
        try {
            Cursor cursor = getContentResolver().query(
                    PhotoProvider.Uri.withId(photoId), projection, null, null, null);
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
     * Get photo from database.
     *
     * @param photoId id of photo to fetch
     * @return the fetched photo
     */
    private Photo getById(String photoId) {
        Cursor cursor = getContentResolver()
                .query(PhotoProvider.Uri.withId(photoId), Util.getAllPhotoColumns(), null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            return Photo.fromCursor(cursor);
        }

        return null;
    }

    /**
     * Handle action add photo.
     * // TODO: implement add of new photo
     * @param photo the new photo
     */
    private void handleAddPhoto(Photo photo) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
