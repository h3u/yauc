package com.bitsailer.yauc.event;

/**
 * Event that occurs when a user request to "unlike" a photo
 * has been transmitted to api and saved to database.
 */
public class PhotoUnlikedEvent {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String photoId;

    public PhotoUnlikedEvent(String photoId) {
        this.photoId = photoId;
    }
}
