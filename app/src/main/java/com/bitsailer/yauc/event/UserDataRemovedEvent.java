package com.bitsailer.yauc.event;

/**
 * Event that occurs when a user has signed out and his
 * content (favorites/own photos) has been removed.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class UserDataRemovedEvent {
    private final int countFavorites;
    private final int countOwn;

    public UserDataRemovedEvent(int countFavorites, int countOwn) {
        this.countFavorites = countFavorites;
        this.countOwn = countOwn;
    }
}
