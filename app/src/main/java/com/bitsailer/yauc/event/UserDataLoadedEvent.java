package com.bitsailer.yauc.event;

/**
 * Event that occurs when a user has signed in and his
 * content (favorites/own photos) are fetched and stored
 * in database.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class UserDataLoadedEvent {
    private final int countFavorites;
    private final int countOwn;

    public UserDataLoadedEvent(int countFavorites, int countOwn) {
        this.countFavorites = countFavorites;
        this.countOwn = countOwn;
    }
}
