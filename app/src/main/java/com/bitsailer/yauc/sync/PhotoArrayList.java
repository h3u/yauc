package com.bitsailer.yauc.sync;

import com.bitsailer.yauc.api.model.SimplePhoto;

import java.util.ArrayList;

/**
 * Supports a simple comparison, a getter and a remove for SimplePhoto
 * using the identifier only.
 */

public class PhotoArrayList<E> extends ArrayList<E> {

    public boolean containsIdentifier(String identifier) {
        Object[] items = this.toArray();
        if (identifier == null || identifier.length() == 0) {
            return contains(identifier);
        } else {
            for (Object item : items) {
                if (item instanceof SimplePhoto
                        && identifier.equals(((SimplePhoto)item).getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public SimplePhoto getByIdentifier(String identifier) {
        Object[] items = this.toArray();
        if (identifier == null || identifier.length() == 0) {
            return null;
        } else {
            for (Object item : items) {
                if (item instanceof SimplePhoto
                        && identifier.equals(((SimplePhoto)item).getId())) {
                    return (SimplePhoto) item;
                }
            }
        }
        return null;
    }

    public boolean removeByIdentifier(String identifier) {
        Object[] items = this.toArray();
        if (identifier == null || identifier.length() == 0) {
            return false;
        } else {
            for (Object item : items) {
                if (item instanceof SimplePhoto
                        && identifier.equals(((SimplePhoto)item).getId())) {
                    return this.remove(item);
                }
            }
        }
        return false;
    }
}
