package com.bitsailer.yauc.sync;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.bitsailer.yauc.Preferences;
import com.bitsailer.yauc.R;
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
import com.bitsailer.yauc.event.PhotoSavedEvent;
import com.bitsailer.yauc.event.PhotoUnlikedEvent;
import com.bitsailer.yauc.event.UserDataLoadedEvent;
import com.bitsailer.yauc.event.UserDataRemovedEvent;
import com.bitsailer.yauc.event.UserLoadedEvent;
import com.bitsailer.yauc.provider.values.PhotosValuesBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.bitsailer.yauc.sync.PhotoManagement.ItemType.FAVORITES;

/**
 * An {@link IntentService} subclass for handling asynchronous actions that
 * fetch data or do crud database operations mainly to keep local database and
 * api.unsplash.com in sync.
 */
public class PhotoManagement extends IntentService {

    private static final int PHOTO_AMOUNT_TO_KEEP = 200;

    private static final String ACTION_INIT_USERS_PHOTOS = "com.bitsailer.yauc.sync.action.init_users_photos";
    private static final String ACTION_SYNC_USERS_PHOTOS = "com.bitsailer.yauc.sync.action.sync_users_photos";
    private static final String ACTION_CLEANUP_NEW_PHOTOS = "com.bitsailer.yauc.sync.action.cleanup_new_photos";
    private static final String ACTION_CLEANUP_USERS_PHOTOS = "com.bitsailer.yauc.sync.action.cleanup_users_photos";
    private static final String ACTION_COMPLETE_PHOTO = "com.bitsailer.yauc.sync.action.complete_photo";
    private static final String ACTION_LIKE_PHOTO = "com.bitsailer.yauc.sync.action.like_photo";
    private static final String ACTION_UNLIKE_PHOTO = "com.bitsailer.yauc.sync.action.unlike_photo";
    private static final String ACTION_EDIT_PHOTO = "com.bitsailer.yauc.sync.action.edit_photo";
    private static final String ACTION_GET_USER = "com.bitsailer.yauc.sync.action.get_user";
    private static final String ACTION_DOWNLOAD_PHOTO = "com.bitsailer.yauc.sync.action.download_photo";

    private static final String EXTRA_USERNAME = "com.bitsailer.yauc.sync.extra.username";
    private static final String EXTRA_PHOTO_ID = "com.bitsailer.yauc.sync.extra.photo_id";
    private static final String EXTRA_PHOTO = "com.bitsailer.yauc.sync.extra.photo";
    private static final String EXTRA_FORCE = "com.bitsailer.yauc.sync.extra.force";

    enum ItemType {
        FAVORITES, OWN
    }

    public PhotoManagement() {
        super("PhotoManagement");
    }

    /**
     * Starts this service to perform initialization of users photos with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @param context The context
     * @param username username whose photos should be updated
     * @see IntentService
     */
    public static void initUsersPhotos(Context context, String username) {
        Intent intent = new Intent(context, PhotoManagement.class);
        intent.setAction(ACTION_INIT_USERS_PHOTOS);
        intent.putExtra(EXTRA_USERNAME, username);
        context.startService(intent);
    }

    /**
     * Starts this service to perform synchronization of users photos with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @param context The context
     * @param username username whose photos should be updated
     * @see IntentService
     */
    public static void syncUsersPhotos(Context context, String username) {
        Intent intent = new Intent(context, PhotoManagement.class);
        intent.setAction(ACTION_SYNC_USERS_PHOTOS);
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

    /**
     * Download a given photo to the appropriate location.
     *
     * @param context calling context
     * @param photoId id of photo to like
     * @see IntentService
     */
    public static void downloadPhoto(Context context, String photoId) {
        Intent intent = new Intent(context, PhotoManagement.class);
        intent.setAction(ACTION_DOWNLOAD_PHOTO);
        intent.putExtra(EXTRA_PHOTO_ID, photoId);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INIT_USERS_PHOTOS.equals(action)) {
                final String username = intent.getStringExtra(EXTRA_USERNAME);
                initUsersPhotos(username);
            } else if (ACTION_SYNC_USERS_PHOTOS.equals(action)) {
                final String username = intent.getStringExtra(EXTRA_USERNAME);
                handleSyncUsersPhotos(username);
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
            } else if (ACTION_DOWNLOAD_PHOTO.equals(action)) {
                final String photoId = intent.getStringExtra(EXTRA_PHOTO_ID);
                handleDownload(photoId);
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
     * Iterate the list and update items.
     *
     * @param list list of photos
     * @return number of inserted items
     */
    private int updateList(List<SimplePhoto> list) {
        int updated = 0;
        if (list != null && !list.isEmpty()) {

            for (SimplePhoto item : list) {
                ContentValues values = ContentValuesBuilder.from(item);
                updated += getContentResolver()
                        .update(PhotoProvider.Uri.withId(item.getId()), values, null, null);
            }
        }
        return updated;
    }

    /**
     * Iterate the list and delete these items.
     *
     * @param list list of photos
     * @return number of inserted items
     */
    private int deleteList(List<SimplePhoto> list) {
        int deleted = 0;
        if (list != null && !list.isEmpty()) {

            for (SimplePhoto item : list) {
                deleted += getContentResolver()
                        .delete(PhotoProvider.Uri.withId(item.getId()), null, null);
            }
        }
        return deleted;
    }

    /**
     * Init local database and fetch favorites and own photos of given user and save them.
     *
     * @param username given username of user
     */
    private void initUsersPhotos(String username) {
        List<SimplePhoto> list;
        // get list of favorites
        list = fetchUserItems(FAVORITES, username);
        int favorites = insertList(list);
        Logger.i("%d favorites inserted", favorites);

        // get list of own photos
        list.clear();
        list = fetchUserItems(ItemType.OWN, username);
        int own = insertList(list);
        Logger.i("%d own photos inserted", own);
        EventBus.getDefault().post(new UserDataLoadedEvent());
    }

    /**
     * Sync local database of favorites and own photos of given user
     * with freshly fetched items from Unsplash API.
     *
     * @param username given username of user
     */
    private void handleSyncUsersPhotos(String username) {
        if (!TextUtils.isEmpty(username)) {
            List<SimplePhoto> itemsToDelete = new ArrayList<>();
            // process favorites
            itemsToDelete.addAll(syncLocalItems(ItemType.FAVORITES, username));

            // process own photos
            itemsToDelete.addAll(syncLocalItems(ItemType.OWN, username));

            int deleted = deleteList(itemsToDelete);
            Logger.i("%d photos deleted during sync", deleted);
            EventBus.getDefault().post(new UserDataLoadedEvent());
        }
    }

    /**
     * Sync local stored items (favorite/own photos) with api for
     * given user.
     *
     * @param type Favorite or own photos
     * @param username username of given user
     * @return list of photos that should be deleted
     */
    private List<SimplePhoto> syncLocalItems(ItemType type, String username) {
        PhotoArrayList<SimplePhoto> remoteItems;
        PhotoArrayList<SimplePhoto> localItems;

        // process favorites
        remoteItems = fetchUserItems(type, username);
        localItems = readUserItems(type, username);
        for (SimplePhoto photo : remoteItems) {
            SimplePhoto localItem = localItems.getByIdentifier(photo.getId());
            if (localItem != null) {
                if (type == FAVORITES) {
                    like(photo.getId());
                }
                localItems.removeByIdentifier(photo.getId());
            } else {
                ContentValues values = ContentValuesBuilder.from(photo);
                getContentResolver().insert(PhotoProvider.Uri.BASE, values);
                Logger.i("add photo %s", photo.getId());
            }
        }
        return localItems;
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
                like(photoId);
            }
        } catch (IOException e) {
            Logger.e(e, e.getMessage());
            EventBus.getDefault().post(new NetworkErrorEvent());
        }
    }

    /**
     * Sign local stored photo as favorite.
     *
     * @param photoId identifier or photo
     * @return number of updated items
     */
    private int like(String photoId) {
        int updated = 0;
        Photo photo = getById(photoId);
        if (photo != null) {
            PhotosValuesBuilder builder = new PhotosValuesBuilder();
            ContentValues values = builder
                    .photoLikedByUser(1)
                    .photoLikes(photo.getLikes() + 1L)
                    .values();
            updated = getContentResolver()
                    .update(PhotoProvider.Uri.withId(photoId), values, null, null);
        }
        EventBus.getDefault().post(new PhotoLikedEvent(photoId));
        return updated;
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
                unlike(photoId);
            }
        } catch (IOException e) {
            Logger.e(e, e.getMessage());
            EventBus.getDefault().post(new NetworkErrorEvent());
        }
    }

    /**
     * Remove local stored photo from favorites.
     *
     * @param photoId identifier or photo
     * @return number of updated items
     */
    private int unlike(String photoId) {
        int updated = 0;
        Photo photo = getById(photoId);
        if (photo != null) {
            PhotosValuesBuilder builder = new PhotosValuesBuilder();
            ContentValues values = builder
                    .photoLikedByUser(0)
                    .photoLikes(photo.getLikes() - 1L)
                    .values();
            updated = getContentResolver()
                    .update(PhotoProvider.Uri.withId(photoId), values, null, null);
        }
        EventBus.getDefault().post(new PhotoUnlikedEvent(photoId));
        return updated;
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
                    PhotoManagement.initUsersPhotos(getApplicationContext(), username);
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

    /**
     * Fetch list of photos for given type and user.
     *
     * @param type the item type needed, e.g. favorites, own photos
     * @param username username of user
     * @return list of requested photos
     */
    private PhotoArrayList<SimplePhoto> fetchUserItems(ItemType type, String username) {
        UnsplashAPI api = UnsplashService.create(UnsplashAPI.class, this);
        PhotoArrayList<SimplePhoto> list = new PhotoArrayList<>();
        int fetched = UnsplashAPI.MAX_PER_PAGE;
        Call<List<SimplePhoto>> listPhotosCall;

        // get list of photos
        for (int page = 1; fetched == UnsplashAPI.MAX_PER_PAGE; page++) {

            if (FAVORITES == type) {
                listPhotosCall = api
                        .listFavoritePhotos(username, page, UnsplashAPI.MAX_PER_PAGE, null);
            } else {
                listPhotosCall = api
                        .listUsersPhotos(username, page, UnsplashAPI.MAX_PER_PAGE, null);
            }

            try {
                List<SimplePhoto> part = listPhotosCall.execute().body();
                fetched = part.size();
                list.addAll(part);
            } catch (IOException e) {
                Logger.e(e, e.getMessage());
            }
        }
        return list;
    }

    /**
     * Read list of photos from Content provider for given type and username.
     *
     * @param type the item type needed, e.g. favorites, own photos
     * @param username username of user
     * @return list of read photos
     */
    private PhotoArrayList<SimplePhoto> readUserItems(ItemType type, String username) {
        PhotoArrayList<SimplePhoto> list = new PhotoArrayList<>();
        Cursor cursor;

        if (username != null) {
            if (type == FAVORITES) {
                cursor = getContentResolver().query(
                        PhotoProvider.Uri.BASE,
                        Util.getAllPhotoColumns(),
                        PhotoColumns.PHOTO_LIKED_BY_USER + " = ?"
                                + " AND " + PhotoColumns.USER_USERNAME + " <> ?",
                        new String[]{ "1", username }, null);
            } else {
                cursor = getContentResolver().query(
                        PhotoProvider.Uri.withUsername(username),
                        Util.getAllPhotoColumns(), null, null, null);
            }

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    list.add(SimplePhoto.fromCursor(cursor));
                }
                cursor.close();
            }
        }
        return list;
    }

    /**
     * Download given photo to local file in
     * {@link android.os.Environment.DIRECTORY_PICTURES}
     *
     * @param photoId id of photo to download
     */
    private void handleDownload(String photoId) {
        Photo photo = getById(photoId);
        if (photo != null) {
            String url = photo.getUrls().getFull();
            Uri target = Util.getOutputMediaFileUri(this, photo);
            DownloadManager.Request request =
                    new DownloadManager.Request(Uri.parse(url));
            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)
                    .setAllowedOverRoaming(false)
                    .setTitle(getString(R.string.downloaded_title))
                    .setMimeType("image/jpeg")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setDestinationUri(target)
                    .allowScanningByMediaScanner();
            DownloadManager downloadManager = (DownloadManager)getApplicationContext()
                    .getSystemService(FragmentActivity.DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);
            EventBus.getDefault().post(new PhotoSavedEvent(target.getLastPathSegment()));
        }
    }
}
