package com.bitsailer.yauc.ui;

/**
 * Type of photos displayed in list/grid fragments of ui.
 */

public enum PhotoType {

    NEW(0),
    FAVORITES(1),
    OWN(2);

    // Position of photo list in SectionsPager
    private final int tabPosition;

    /**
     * Constructor
     * @param tabPosition position of this type when arranged in tabs
     */
    PhotoType(int tabPosition) {
        this.tabPosition = tabPosition;
    }

    public int getTabPosition() {
        return tabPosition;
    }
}