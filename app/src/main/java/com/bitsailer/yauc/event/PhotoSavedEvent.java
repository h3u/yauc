package com.bitsailer.yauc.event;

/**
 * Event that occurs when a downloaded photo has been saved.
 */

public class PhotoSavedEvent {
    private String name;

    public PhotoSavedEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
