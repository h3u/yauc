package com.bitsailer.yauc.data;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

import static com.bitsailer.yauc.data.PhotoDatabase.Tables.PHOTOS;


/**
 * Definition of Content Provider for photos.
 */

@ContentProvider(authority = PhotoProvider.AUTHORITY,
        database = PhotoDatabase.class,
        packageName = PhotoProvider.PACKAGE_NAME)
public final class PhotoProvider {

    public static final String AUTHORITY = "com.bitsailer.yauc.provider";
    public static final String PACKAGE_NAME = "com.bitsailer.yauc.provider";

    static final android.net.Uri BASE_CONTENT_URI = android.net.Uri.parse("content://" + AUTHORITY);

    interface Path {
        String BASE = "photos";
        String FAVORITE = "favorite"; // liked photos
        String OWN = "own"; // users own photos
    }

    private static android.net.Uri buildUri(String... paths) {
        android.net.Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = PHOTOS)
    public static class Uri {

        @ContentUri(
                path = Path.BASE,
                type = "vnd.android.cursor.dir/photos",
                defaultSort = PhotoColumns.PHOTO_CREATED_AT + " DESC")
        public static final android.net.Uri BASE = buildUri(Path.BASE);

        @ContentUri(
                path = Path.BASE + "/" + Path.FAVORITE,
                type = "vnd.android.cursor.dir/photos",
                where = PhotoColumns.PHOTO_LIKED_BY_USER + " = 1",
                defaultSort = PhotoColumns.PHOTO_CREATED_AT + " DESC")
        public static final android.net.Uri FAVORITE = buildUri(Path.BASE, Path.FAVORITE);

        @InexactContentUri(
                path = Path.BASE + "/" + Path.OWN + "/*",
                name = "OWN",
                type = "vnd.android.cursor.dir/photos",
                whereColumn = PhotoColumns.USER_USERNAME,
                pathSegment = 2)
        public static android.net.Uri withUsername(String username) {
            return buildUri(Path.BASE, Path.OWN, username);
        }

        @InexactContentUri(
                path = Path.BASE + "/*",
                name = "ITEM",
                type = "vnd.android.cursor.item/photos",
                whereColumn = PhotoColumns.PHOTO_ID,
                pathSegment = 1)
        public static android.net.Uri withId(String id) {
            return buildUri(Path.BASE, id);
        }
    }
}
