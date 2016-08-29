package com.bitsailer.yauc.event;

/**
 * Event that occurs when a user request to like a photo
 * has been transmitted to api and saved to database.
 */
public class PhotoLikedEvent {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String photoId;

    public PhotoLikedEvent(String photoId) {
        this.photoId = photoId;
    }
}
