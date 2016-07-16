package com.bitsailer.yauc.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.IfNotExists;
import net.simonvt.schematic.annotation.Table;

/**
 * Definition of photo database
 */

@Database(
        version = PhotoDatabase.VERSION,
        fileName = PhotoDatabase.FILENAME,
        packageName = PhotoProvider.PACKAGE_NAME)
final class PhotoDatabase {

    public static final String FILENAME = "PhotoDatabase.db";

    public PhotoDatabase() {
    }

    static final int VERSION = 1;

    static class Tables {

        @Table(PhotoColumns.class)
        @IfNotExists
        static final String PHOTOS = "photos";
    }
}
