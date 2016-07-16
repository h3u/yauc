package com.bitsailer.yauc.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;

import com.bitsailer.yauc.provider.PhotoDatabase;
import com.bitsailer.yauc.provider.values.PhotosValuesBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Vector;

/**
 * Test for generated PhotoProvider: query, insert, update, bulkinsert and delete.
 */
@RunWith(AndroidJUnit4.class)
public class PhotoProviderTest extends ProviderTestCase2<com.bitsailer.yauc.provider.PhotoProvider> {

    public PhotoProviderTest() {
        super(com.bitsailer.yauc.provider.PhotoProvider.class, PhotoProvider.AUTHORITY);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        setContext(InstrumentationRegistry.getTargetContext());
        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        deleteAll();
    }

    private void deleteAll() {
        PhotoDatabase provider = PhotoDatabase.getInstance(getMockContext());
        SQLiteDatabase db = provider.getWritableDatabase();
        db.execSQL("DELETE FROM " + com.bitsailer.yauc.data.PhotoDatabase.Tables.PHOTOS);
    }

    @Test
    public void getTypeBaseTest() {
        String uriDir = "vnd.android.cursor.dir/photos";
        assertEquals(uriDir,
                getProvider().getType(PhotoProvider.Uri.BASE));
        assertEquals(uriDir,
                getProvider().getType(PhotoProvider.Uri.FAVORITE));
        assertEquals(uriDir,
                getProvider().getType(PhotoProvider.Uri.withUsername("uli")));
        assertEquals("vnd.android.cursor.item/photos",
                getProvider().getType(PhotoProvider.Uri.withId("abc1234")));
    }

    private void insertPhoto(String id, String username, Boolean favorite) {
        PhotosValuesBuilder builder = new PhotosValuesBuilder();
        builder.photoId(id).userUsername(username).photoLikedByUser(favorite ? 1 : 0);
        getMockContentResolver().insert(PhotoProvider.Uri.BASE, builder.values());
    }

    @Test
    public void queryListTest() {
        insertPhoto("abc1", "user1", false);
        insertPhoto("abc2", "user2", false);

        Cursor cursor = getMockContentResolver().query(
                PhotoProvider.Uri.BASE, new String[] { PhotoColumns.PHOTO_ID, PhotoColumns.USER_USERNAME }, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                assertEquals("abc1", cursor.getString(0));
                assertTrue(cursor.moveToNext());
                assertEquals("abc2", cursor.getString(0));
            } else {
                fail("no records found");
            }
            cursor.close();
        }
    }

    @Test
    public void queryFavoritesTest() {
        insertPhoto("abc1", "user1", false);
        insertPhoto("abc2", "user2", true);
        insertPhoto("abc3", "user2", false);

        Cursor cursor = getMockContentResolver().query(
                PhotoProvider.Uri.FAVORITE, new String[] { PhotoColumns.PHOTO_ID, PhotoColumns.USER_USERNAME }, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                assertEquals("abc2", cursor.getString(0));
                assertFalse(cursor.moveToNext());
            } else {
                fail("no records found");
            }
            cursor.close();
        }
    }

    @Test
    public void queryOwnTest() {
        insertPhoto("abc1", "user1", false);
        insertPhoto("abc2", "user2", true);
        insertPhoto("abc3", "user2", false);

        Cursor cursor = getMockContentResolver().query(
                PhotoProvider.Uri.withUsername("user2"),
                new String[] { PhotoColumns.PHOTO_ID, PhotoColumns.USER_USERNAME },
                null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                assertEquals("abc2", cursor.getString(0));
                assertTrue(cursor.moveToNext());
                assertEquals("abc3", cursor.getString(0));
            } else {
                fail("no records found");
            }
            cursor.close();
        }
    }

    @Test
    public void insertTest() {
        // insert entry
        insertPhoto("abc1234", "hello", false);

        // read and compare
        Cursor cursor = getMockContentResolver()
                .query(PhotoProvider.Uri.withId("abc123"), new String[]{PhotoColumns.USER_USERNAME}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            assertEquals("hello", cursor.getString(cursor.getColumnIndex(PhotoColumns.USER_USERNAME)));
            cursor.close();
        }
    }

    @Test
    public void updateTest() {
        // insert entry
        insertPhoto("abc123", "hello", false);

        // update entry
        PhotosValuesBuilder builder = new PhotosValuesBuilder();
        builder.userUsername("hallo");
        getMockContentResolver().update(PhotoProvider.Uri.withId("abc1234"), builder.values(), null, null);

        // read item
        Cursor cursor = getMockContentResolver()
                .query(PhotoProvider.Uri.withId("abc1234"), new String[]{PhotoColumns.USER_USERNAME}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            assertEquals("hallo", cursor.getString(cursor.getColumnIndex(PhotoColumns.USER_USERNAME)));
            cursor.close();
        }
    }

    @Test
    public void deleteTest() {
        // insert entry
        insertPhoto("abc123", "hello", false);

        getMockContentResolver().delete(PhotoProvider.Uri.withId("abc1234"), null, null);

        // read item
        Cursor cursor = getMockContentResolver()
                .query(PhotoProvider.Uri.withId("abc1234"), new String[]{PhotoColumns.USER_USERNAME}, null, null, null);
        if (cursor != null) {
            assertFalse(cursor.moveToFirst());
            cursor.close();
        }
    }

    @Test
    public void bulkInsertTest() {
        Vector<ContentValues> vector = new Vector<ContentValues>(2);

        for (int i=1;i <= 2; i++) {
            PhotosValuesBuilder builder = new PhotosValuesBuilder();
            builder.photoId("abc" + i).userUsername("user" + i);
            vector.add(builder.values());
        }

        ContentValues[] cvArray = new ContentValues[vector.size()];
        vector.toArray(cvArray);

        getMockContentResolver().bulkInsert(PhotoProvider.Uri.BASE, cvArray);

        // read items
        Cursor cursor = getMockContentResolver()
                .query(PhotoProvider.Uri.BASE, new String[]{PhotoColumns.PHOTO_ID}, null, null, null);
        if (cursor != null) {
            assertTrue(cursor.moveToFirst());
            assertEquals("abc1", cursor.getString(0));
            assertTrue(cursor.moveToNext());
            assertEquals("abc2", cursor.getString(0));
            cursor.close();
        }
    }
}
