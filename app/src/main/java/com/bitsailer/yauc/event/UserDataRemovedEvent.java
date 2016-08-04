package com.bitsailer.yauc.event;

/**
 * Created by Uli Wucherer (u.wucherer@gmail.com) on 03/08/16.
 */

public class UserDataRemovedEvent {
    public final int countFavorites;
    public final int countOwn;

    public UserDataRemovedEvent(int countFavorites, int countOwn) {
        this.countFavorites = countFavorites;
        this.countOwn = countOwn;
    }
}
