package com.bitsailer.yauc.sync;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.bitsailer.yauc.Preferences;
import com.bitsailer.yauc.Util;
import com.bitsailer.yauc.YaucApplication;
import com.bitsailer.yauc.api.UnsplashAPI;
import com.bitsailer.yauc.api.UnsplashService;
import com.bitsailer.yauc.api.model.Photo;
import com.bitsailer.yauc.api.model.SimplePhoto;
import com.bitsailer.yauc.api.model.User;
import com.bitsailer.yauc.data.ContentValuesBuilder;
import com.bitsailer.yauc.data.PhotoColumns;
import com.bitsailer.yauc.data.PhotoProvider;
import com.bitsailer.yauc.event.NetworkErrorEvent;
import com.bitsailer.yauc.event.PhotoDataLoadedEvent;
import com.bitsailer.yauc.event.PhotoLikedEvent;
import com.bitsailer.yauc.event.PhotoUnlikedEvent;
import com.bitsailer.yauc.event.UserDataLoadedEvent;
import com.bitsailer.yauc.event.UserDataRemovedEvent;
import com.bitsailer.yauc.event.UserLoadedEvent;
import com.bitsailer.yauc.provider.values.PhotosValuesBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous actions that
 * fetch data or do crud database operations mainly to keep local database and
 * api.unsplash.com in sync.
 */
public class PhotoManagement extends IntentService {

    private static final int PHOTO_AMOUNT_TO_KEEP = 200;

    private static final String ACTION_UPDATE_USERS_PHOTOS = "com.bitsailer.yauc.sync.action.update_users_photos";
    private static final String ACTION_CLEANUP_NEW_PHOTOS = "com.bitsailer.yauc.sync.action.cleanup_new_photos";
    private static final String ACTION_CLEANUP_USERS_PHOTOS = "com.bitsailer.yauc.sync.action.cleanup_users_photos";
    private static final String ACTION_COMPLETE_PHOTO = "com.bitsailer.yauc.sync.action.complete_photo";
    private static final String ACTION_LIKE_PHOTO = "com.bitsailer.yauc.sync.action.like_photo";
    private static final String ACTION_UNLIKE_PHOTO = "com.bitsailer.yauc.sync.action.unlike_photo";
    private static final String ACTION_EDIT_PHOTO = "com.bitsailer.yauc.sync.action.edit_photo";
    private static final String ACTION_GET_USER = "com.bitsailer.yauc.sync.action.get_user";

    private static final String EXTRA_USERNAME = "com.bitsailer.yauc.sync.extra.username";
    private static final String EXTRA_PHOTO_ID = "com.bitsailer.yauc.sync.extra.photo_id";
    private static final String EXTRA_PHOTO = "com.bitsailer.yauc.sync.extra.photo";
    private static final String EXTRA_FORCE = "com.bitsailer.yauc.sync.extra.force";

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
     * Removes new photos up to a limit to keep
     *
     * @param context calling context
     * @see IntentService
     */
    public static void cleanupNewPhotos(Context context) {
        Intent intent = new Intent(context, PhotoManagement.class);
        intent.setAction(ACTION_CLEANUP_NEW_PHOTOS);
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
     * @param force do a forced fetch/update
     * @see IntentService
     */
    public static void completePhoto(Context context, String photoId, boolean force) {
        Intent intent = new Intent(context, PhotoManagement.class);
        intent.setAction(ACTION_COMPLETE_PHOTO);
        intent.putExtra(EXTRA_PHOTO_ID, photoId);
        intent.putExtra(EXTRA_FORCE, force);
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
     * Edit a photo
     *
     * @param context calling context
     * @param photo the photo updates
     * @see IntentService
     */
    public static void editPhoto(Context context, Photo photo) {
        Intent intent = new Intent(context, PhotoManagement.class);
        intent.setAction(ACTION_EDIT_PHOTO);
        intent.putExtra(EXTRA_PHOTO, photo);
        context.startService(intent);
    }

    /**
     * Get user data, e.g. name, username, photo-url, ...
     *
     * @param context calling context
     * @see IntentService
     */
    public static void getUser(Context context) {
        Intent intent = new Intent(context, PhotoManagement.class);
        intent.setAction(ACTION_GET_USER);
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
            } else if (ACTION_CLEANUP_NEW_PHOTOS.equals(action)) {
                handleCleanupNewPhotos();
            } else if (ACTION_COMPLETE_PHOTO.equals(action)) {
                final String photoId = intent.getStringExtra(EXTRA_PHOTO_ID);
                final boolean force = intent.getBooleanExtra(EXTRA_FORCE, false);
                handleCompletePhoto(photoId, force);
            } else if (ACTION_LIKE_PHOTO.equals(action)) {
                final String photoId = intent.getStringExtra(EXTRA_PHOTO_ID);
                handleLikePhoto(photoId);
            } else if (ACTION_UNLIKE_PHOTO.equals(action)) {
                final String photoId = intent.getStringExtra(EXTRA_PHOTO_ID);
                handleUnlikePhoto(photoId);
            } else if (ACTION_EDIT_PHOTO.equals(action)) {
                final Photo photo = intent.getParcelableExtra(EXTRA_PHOTO);
                handleEditPhoto(photo);
            } else if (ACTION_GET_USER.equals(action)) {
                handleGetUser();
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
                // check if exists
                if (!hasPhoto(item.getId())) {
                    ContentValues values = ContentValuesBuilder.from(item);
                    Uri uri = getContentResolver().insert(PhotoProvider.Uri.BASE, values);
                    if (uri != null) {
                        inserted++;
                    }
                } else {
                    // update existing
                    handleCompletePhoto(item.getId(), false);
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
                Logger.e(e, e.getMessage());
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
                Logger.e(e, e.getMessage());
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
        int deletedFavorites;
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
     * Delete some "old" photos but keep {@link #PHOTO_AMOUNT_TO_KEEP} and
     * users favorite and own photos if authenticated.
     * The cleanup is done in reverse order: older photos are removed first.
     */
    private void handleCleanupNewPhotos() {
        int photoCount;
        int deleted = 0;
        String username = Preferences.get(getApplicationContext()).getUserUsername();
        Boolean signedIn = Preferences.get(getApplicationContext()).isAuthenticated();

        if (username != null || !signedIn) {
            photoCount = countNew(username);

            if (photoCount > PHOTO_AMOUNT_TO_KEEP) {
                String selection = null;
                String selectionArgs[] = null;

                if (username != null) {
                    selection = PhotoColumns.PHOTO_LIKED_BY_USER + " = ?"
                            + " AND " + PhotoColumns.USER_USERNAME + " <> ?";
                    selectionArgs = new String[] { "0", username };
                }

                Cursor cursor = getContentResolver().query(
                        PhotoProvider.Uri.BASE,
                        new String[] {
                                PhotoColumns.PHOTO_ID,
                                PhotoColumns.PHOTO_CREATED_AT,
                                PhotoColumns.LINKS_HTML,
                        },
                        selection, selectionArgs,
                        PhotoColumns.PHOTO_CREATED_AT + " ASC");

                if (cursor != null && cursor.getCount() > 0) {
                    int toDelete = photoCount - PHOTO_AMOUNT_TO_KEEP;
                    while (toDelete > 0 && cursor.moveToNext()) {

                        deleted += getContentResolver().delete(
                                PhotoProvider.Uri.BASE,
                                PhotoColumns.PHOTO_ID + " = ?",
                                new String[] {cursor.getString(cursor.getColumnIndex(PhotoColumns.PHOTO_ID))});
                        toDelete--;

                    }
                    cursor.close();
                    Logger.i("deleted %d photos from %d during cleanup", deleted, photoCount);
                }
            }
        }
    }

    private int countNew(String username) {
        String selection = null;
        String selectionArgs[] = null;
        int countNew = 0;

        if (username != null) {
            selection = PhotoColumns.PHOTO_LIKED_BY_USER + " = ?"
                    + " AND " + PhotoColumns.USER_USERNAME + " <> ?";
            selectionArgs = new String[]{"0", username};
        }

        Cursor cursor = getContentResolver().query(
                PhotoProvider.Uri.BASE,
                new String[] {PhotoColumns.PHOTO_ID},
                selection,
                selectionArgs,
                null);

        if (cursor != null) {
            countNew = cursor.getCount();
            cursor.close();
        }

        return countNew;
    }

    /**
     * Handle action to complete photo data.
     * This only fetches and updates if the photo data is incompleteor
     * parameter force is set to true.
     *
     * @param photoId id of photo to complete the data
     * @param force do a forced fetch/update
     */
    private void handleCompletePhoto(String photoId, boolean force) {
        Photo photo = getById(photoId);
        if (photo != null && (!photo.isComplete() || force)) {
            // get api service
            UnsplashAPI api = UnsplashService.create(UnsplashAPI.class, this);
            Call<Photo> call = api.getPhoto(photoId);
            try {
                Photo photoUpdate = call.execute().body();
                if (photoUpdate != null) {
                    int num = updatePhoto(photoUpdate);
                    Logger.d("%d photos updated", num);
                }
            } catch (IOException e) {
                Logger.e(e, e.getMessage());
                YaucApplication.reportException(e);
                EventBus.getDefault().post(new NetworkErrorEvent());
            }
        }
    }

    private int updatePhoto(Photo photo) {
        ContentValues values = ContentValuesBuilder.from(photo);
        int num = getContentResolver().update(PhotoProvider.Uri.withId(photo.getId()), values, null, null);
        EventBus.getDefault().post(new PhotoDataLoadedEvent(photo.getId()));
        return num;
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
            Logger.e(e, e.getMessage());
            EventBus.getDefault().post(new NetworkErrorEvent());
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
            Logger.e(e, e.getMessage());
            EventBus.getDefault().post(new NetworkErrorEvent());
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
            Logger.e(e, e.getMessage());
            YaucApplication.reportException(e);
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
        Photo photo = null;
        Cursor cursor = getContentResolver()
                .query(PhotoProvider.Uri.withId(photoId), Util.getAllPhotoColumns(), null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            photo = Photo.fromCursor(cursor);
            cursor.close();
        }

        return photo;
    }

    /**
     * Handle action edit photo.
     * @param update the photo updates
     */
    private void handleEditPhoto(Photo update) {
        if (update != null) {
            Logger.d(update.getExif().getIso());
            // get api service
            UnsplashAPI api = UnsplashService.create(UnsplashAPI.class, this);
            Call<Photo> call = api.updatePhoto(
                    update.getId(), update.getId(),
                    null, null,
                    update.getLocation().getCity(),
                    update.getLocation().getCountry(),
                    update.getExif().getMake(),
                    update.getExif().getModel(),
                    update.getExif().getExposureTime(),
                    update.getExif().getAperture(),
                    update.getExif().getFocalLength(),
                    (update.getExif().getIso() != null) ?
                            Integer.toString(update.getExif().getIso()) : null
            );
            try {
                Photo photo = call.execute().body();
                if (photo != null) {
                    updatePhoto(photo);
                }
            } catch (IOException e) {
                Logger.e(e, e.getMessage());
            }
        }
    }

    private void handleGetUser() {
        UnsplashAPI api = UnsplashService.create(UnsplashAPI.class, this);
        Call<User> call = api.getMe();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user = response.body();
                Preferences.get(getApplicationContext()).setUser(user);
                String username = user.getUsername();
                if (!TextUtils.isEmpty(username)) {
                    PhotoManagement.updateUsersPhotos(getApplicationContext(), username);
                }
                EventBus.getDefault().post(new UserLoadedEvent());
                trackSignIn();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Logger.e(t, t.getMessage());
            }
        });
    }

    private void trackSignIn() {
        FirebaseAnalytics tracker = ((YaucApplication) getApplication()).getDefaultTracker();
        tracker.logEvent(FirebaseAnalytics.Event.LOGIN, new Bundle());
    }
}
