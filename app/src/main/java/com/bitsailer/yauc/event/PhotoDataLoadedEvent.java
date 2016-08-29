package com.bitsailer.yauc.event;

/**
 * Event that occurs each time a photo has been fetched from
 * unsplash api and updated in the database.
 */
public class PhotoDataLoadedEvent {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String photoId;

    public PhotoDataLoadedEvent(String photoId) {
        this.photoId = photoId;
    }
}
