package com.bitsailer.yauc.data;

import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.REAL;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Defines columns needed to store photo data.
 * This almost reflects the available data from response "Get a photo":
 * https://unsplash.com/documentation#photos
 */
public interface PhotoColumns {
    @DataType(TEXT)
    @PrimaryKey
    String PHOTO_ID = "id";

    /**
     * Unixtime in UTC
     */
    @DataType(INTEGER)
    String PHOTO_CREATED_AT = "created_at";

    @DataType(INTEGER)
    String PHOTO_WIDTH = "width";

    @DataType(INTEGER)
    String PHOTO_HEIGHT = "height";

    @DataType(TEXT)
    String PHOTO_COLOR = "color";

    @DataType(INTEGER)
    String PHOTO_DOWNLOADS = "downloads";

    @DataType(INTEGER)
    String PHOTO_LIKES = "likes";

    @DataType(INTEGER)
    String PHOTO_LIKED_BY_USER = "liked_by_user";

    @DataType(TEXT)
    String EXIF_MAKE = "exif_make";

    @DataType(TEXT)
    String EXIF_MODEL = "exif_model";

    @DataType(REAL)
    String EXIF_EXPOSURE_TIME = "exif_exposure_time";

    @DataType(REAL)
    String EXIF_APERTURE = "exif_aperture";

    @DataType(REAL)
    String EXIF_FOCAL_LENGTH = "exif_focal_length";

    @DataType(INTEGER)
    String EXIF_ISO = "exif_iso";

    @DataType(TEXT)
    String LOCATION_CITY = "location_city";

    @DataType(TEXT)
    String LOCATION_COUNTRY = "location_country";

    @DataType(REAL)
    String LOCATION_LATITUDE = "location_latitude";

    @DataType(REAL)
    String LOCATION_LONGITUDE = "location_longitude";

    @DataType(TEXT)
    String URLS_RAW = "urls_raw";

    @DataType(TEXT)
    String URLS_FULL = "urls_full";

    @DataType(TEXT)
    String URLS_REGULAR = "urls_regular";

    @DataType(TEXT)
    String URLS_SMALL = "urls_small";

    @DataType(TEXT)
    String URLS_THUMB = "urls_thumb";

    @DataType(TEXT)
    String LINKS_SELF = "links_self";

    @DataType(TEXT)
    String LINKS_HTML = "links_html";

    @DataType(TEXT)
    String LINKS_DOWNLOAD = "links_download";

    @DataType(TEXT)
    String USER_ID = "user_id";

    @DataType(TEXT)
    String USER_USERNAME = "user_username";

    @DataType(TEXT)
    String USER_NAME = "user_name";

    @DataType(TEXT)
    String USER_PORTFOLIO_URL = "user_portfolio_url";

    @DataType(TEXT)
    String USER_PROFILE_IMAGE_SMALL = "user_profile_image_small";

    @DataType(TEXT)
    String USER_PROFILE_IMAGE_MEDIUM = "user_profile_image_medium";

    @DataType(TEXT)
    String USER_PROFILE_IMAGE_LARGE = "user_profile_image_large";

    @DataType(TEXT)
    String USER_LINKS_SELF = "user_links_self";

    @DataType(TEXT)
    String USER_LINKS_HTML = "user_links_html";

    @DataType(TEXT)
    String USER_LINKS_PHOTOS = "user_links_photos";

    @DataType(TEXT)
    String USER_LINKS_LIKES = "user_links_likes";
}
