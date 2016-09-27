package com.bitsailer.yauc;

import com.bitsailer.yauc.api.model.SimplePhoto;
import com.bitsailer.yauc.sync.PhotoArrayList;

import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Unit test for PhotoArrayList.
 */

public class PhotoArrayListTest {
    @Test
    public void containsIdentifier() {
        SimplePhoto photo1 = new SimplePhoto();
        photo1.setId("1");
        SimplePhoto photo2 = new SimplePhoto();
        photo2.setId("2");
        SimplePhoto photo3 = new SimplePhoto();
        photo3.setId("3");
        PhotoArrayList<SimplePhoto> list = new PhotoArrayList<>();
        assertFalse("Empty list does not contain identifier '1'",
                list.containsIdentifier("1"));
        list.add(photo1);
        list.add(photo2);
        assertEquals(2, list.size());
        assertFalse("List does not contain identifier '3'",
                list.containsIdentifier("3"));
        assertTrue("List contains identifier '1'",
                list.containsIdentifier("1"));
        assertTrue("List contains identifier '2'",
                list.containsIdentifier("2"));
    }

    @Test
    public void getByIdentifier() {
        SimplePhoto photo1 = new SimplePhoto();
        photo1.setId("1");
        PhotoArrayList<SimplePhoto> list = new PhotoArrayList<>();
        assertNull("getByIdentifier returns null for identifier '1'",
                list.getByIdentifier("1"));
        list.add(photo1);
        assertEquals("getByIdentifier for identifier '1' and is equal to photo1",
                photo1, list.getByIdentifier("1"));
        assertNull("getByIdentifier returns null for unknown identifier '2'",
                list.getByIdentifier("2"));
    }

    @Test
    public void removeByIdentifier() {
        SimplePhoto photo1 = new SimplePhoto();
        photo1.setId("1");
        SimplePhoto photo2 = new SimplePhoto();
        photo2.setId("2");
        PhotoArrayList<SimplePhoto> list = new PhotoArrayList<>();
        assertFalse("removeByIdentifier returns false for identifier '1' with empty list",
                list.removeByIdentifier("1"));
        list.add(photo1);
        list.add(photo2);
        assertEquals("List contains identifier '1' and is equal to photo1",
                photo1, list.getByIdentifier("1"));
        assertEquals("List contains identifier '2' and is equal to photo2",
                photo1, list.getByIdentifier("1"));
        assertFalse("removeByIdentifier returns false for unknown identifier '3'",
                list.removeByIdentifier("3"));
        assertTrue("removeByIdentifier returns true for identifier '1'",
                list.removeByIdentifier("1"));
        assertFalse("List does not contain identifier '1' after removing",
                list.containsIdentifier("1"));
    }
}
