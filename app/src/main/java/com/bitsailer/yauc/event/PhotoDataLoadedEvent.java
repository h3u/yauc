package com.bitsailer.yauc.event;

/**
 * Created by Uli Wucherer (u.wucherer@gmail.com) on 03/08/16.
 */

public class PhotoDataLoadedEvent {
    public final String photoId;

    public PhotoDataLoadedEvent(String photoId) {
        this.photoId = photoId;
    }
}
