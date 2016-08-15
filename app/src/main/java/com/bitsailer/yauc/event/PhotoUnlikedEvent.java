package com.bitsailer.yauc.event;

/**
 * Created by Uli Wucherer (u.wucherer@gmail.com) on 03/08/16.
 */

public class PhotoUnlikedEvent {
    public final String photoId;

    public PhotoUnlikedEvent(String photoId) {
        this.photoId = photoId;
    }
}
