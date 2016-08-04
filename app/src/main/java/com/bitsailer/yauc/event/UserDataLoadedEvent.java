package com.bitsailer.yauc.event;

/**
 * Created by Uli Wucherer (u.wucherer@gmail.com) on 03/08/16.
 */

public class UserDataLoadedEvent {
    public final int countFavorites;
    public final int countOwn;

    public UserDataLoadedEvent(int countFavorites, int countOwn) {
        this.countFavorites = countFavorites;
        this.countOwn = countOwn;
    }
}
