
package com.bitsailer.yauc.api.model;

import android.database.Cursor;
import android.os.Parcel;

import com.bitsailer.yauc.data.PhotoColumns;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

//@Generated("org.jsonschema2pojo")
@SuppressWarnings("ALL")
public class Photo extends SimplePhoto {

    @SerializedName("downloads")
    @Expose
    private Integer downloads;

    @SerializedName("exif")
    @Expose
    private Exif exif;

    @SerializedName("location")
    @Expose
    private Location location;

    /**
     * 
     * @return
     *     The downloads
     */
    public Integer getDownloads() {
        return downloads;
    }

    /**
     * 
     * @param downloads
     *     The downloads
     */
    public void setDownloads(Integer downloads) {
        this.downloads = downloads;
    }

    /**
     * 
     * @return
     *     The exif
     */
    public Exif getExif() {
        return exif;
    }

    /**
     * 
     * @param exif
     *     The exif
     */
    private void setExif(Exif exif) {
        this.exif = exif;
    }

    /**
     * 
     * @return
     *     The location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * 
     * @param location
     *     The location
     */
    private void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }


    /**
     * Returns true if completed at has been set which is done when fetching
     * full photo data.
     * @return
     */
    public boolean isComplete() {
        return getCompletedAt() != 0;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getId()).append(getCreatedAt()).append(getWidth()).append(getHeight()).append(getColor())
                .append(downloads).append(getLikes()).append(getLikedByUser()).append(exif).append(location)
                .append(getCurrentUserCollections()).append(getUrls()).append(getCategories()).append(getLinks())
                .append(getUser()).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Photo)) {
            return false;
        }
        Photo rhs = ((Photo) other);
        return new EqualsBuilder().append(getId(), rhs.getId()).append(getCreatedAt(), rhs.getCreatedAt())
                .append(getWidth(), rhs.getWidth()).append(getHeight(), rhs.getHeight()).append(getColor(), rhs.getColor())
                .append(downloads, rhs.downloads).append(getLikes(), rhs.getLikes())
                .append(getLikedByUser(), rhs.getLikedByUser()).append(exif, rhs.exif)
                .append(location, rhs.location)
                .append(getCurrentUserCollections(), rhs.getCurrentUserCollections())
                .append(getUrls(), rhs.getUrls())
                .append(getCategories(), rhs.getCategories()).append(getLinks(), rhs.getLinks())
                .append(getUser(), rhs.getUser()).isEquals();
    }

    public static Photo fromCursor(Cursor cursor) {
        Photo photo = new Photo();
        if (cursor != null) {
            photo.setId(cursor.getString(cursor.getColumnIndex(PhotoColumns.PHOTO_ID)));
            photo.setCompletedAt(cursor.getLong(cursor.getColumnIndex(PhotoColumns.PHOTO_COMPLETED_AT)));
            photo.setColor(cursor.getString(cursor.getColumnIndex(PhotoColumns.PHOTO_COLOR)));
            photo.setWidth(cursor.getInt(cursor.getColumnIndex(PhotoColumns.PHOTO_WIDTH)));
            photo.setHeight(cursor.getInt(cursor.getColumnIndex(PhotoColumns.PHOTO_HEIGHT)));
            photo.setLikes(cursor.getInt(cursor.getColumnIndex(PhotoColumns.PHOTO_LIKES)));
            photo.setLikedByUser(
                    1 == cursor.getInt(cursor.getColumnIndex(PhotoColumns.PHOTO_LIKED_BY_USER)));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d = new Date();
            d.setTime(cursor.getLong(cursor.getColumnIndex(PhotoColumns.PHOTO_CREATED_AT)));
            photo.setCreatedAt(sdf.format(d));
            Urls urls = new Urls();
            urls.setSmall(cursor.getString(cursor.getColumnIndex(PhotoColumns.URLS_SMALL)));
            urls.setRegular(cursor.getString(cursor.getColumnIndex(PhotoColumns.URLS_REGULAR)));
            urls.setFull(cursor.getString(cursor.getColumnIndex(PhotoColumns.URLS_FULL)));
            photo.setUrls(urls);
            PhotoLinks links = new PhotoLinks();
            links.setHtml(cursor.getString(cursor.getColumnIndex(PhotoColumns.LINKS_HTML)));
            photo.setLinks(links);
            User user = new User();
            user.setUsername(cursor.getString(cursor.getColumnIndex(PhotoColumns.USER_USERNAME)));
            user.setName(cursor.getString(cursor.getColumnIndex(PhotoColumns.USER_NAME)));
            user.setPortfolioUrl(cursor.getString(cursor.getColumnIndex(PhotoColumns.USER_PORTFOLIO_URL)));
            ProfileImage profileImage = new ProfileImage();
            profileImage.setLarge(cursor.getString(cursor.getColumnIndex(PhotoColumns.USER_PROFILE_IMAGE_LARGE)));
            user.setProfileImage(profileImage);
            UserLinks userLinks = new UserLinks();
            userLinks.setHtml(cursor.getString(cursor.getColumnIndex(PhotoColumns.USER_LINKS_HTML)));
            user.setLinks(userLinks);
            photo.setUser(user);
            Exif exif = new Exif();
            exif.setMake(cursor.getString(cursor.getColumnIndex(PhotoColumns.EXIF_MAKE)));
            exif.setModel(cursor.getString(cursor.getColumnIndex(PhotoColumns.EXIF_MODEL)));
            exif.setAperture(cursor.getString(cursor.getColumnIndex(PhotoColumns.EXIF_APERTURE)));
            exif.setExposureTime(cursor.getString(cursor.getColumnIndex(PhotoColumns.EXIF_EXPOSURE_TIME)));
            exif.setFocalLength(cursor.getString(cursor.getColumnIndex(PhotoColumns.EXIF_FOCAL_LENGTH)));
            exif.setIso(cursor.getInt(cursor.getColumnIndex(PhotoColumns.EXIF_ISO)));
            photo.setExif(exif);
            Location location = new Location();
            location.setCountry(cursor.getString(cursor.getColumnIndex(PhotoColumns.LOCATION_COUNTRY)));
            location.setCity(cursor.getString(cursor.getColumnIndex(PhotoColumns.LOCATION_CITY)));
            Double latitude = cursor.getDouble(cursor.getColumnIndex(PhotoColumns.LOCATION_LATITUDE));
            Double longitude = cursor.getDouble(cursor.getColumnIndex(PhotoColumns.LOCATION_LONGITUDE));
            if (latitude != 0.0 && longitude != 0.0) {
                Position position = new Position();
                position.setLatitude(latitude);
                position.setLongitude(longitude);
                location.setPosition(position);
            }
            photo.setLocation(location);
        }

        return photo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeValue(this.downloads);
        dest.writeParcelable(this.exif, flags);
        dest.writeParcelable(this.location, flags);
        dest.writeString(this.id);
        dest.writeString(this.createdAt);
        dest.writeValue(this.width);
        dest.writeValue(this.height);
        dest.writeString(this.color);
        dest.writeValue(this.likes);
        dest.writeValue(this.likedByUser);
        dest.writeParcelable(this.user, flags);
        dest.writeList(this.currentUserCollections);
        dest.writeParcelable(this.urls, flags);
        dest.writeList(this.categories);
        dest.writeParcelable(this.links, flags);
    }

    private Photo() {
    }

    private Photo(Parcel in) {
        super(in);
        this.downloads = (Integer) in.readValue(Integer.class.getClassLoader());
        this.exif = in.readParcelable(Exif.class.getClassLoader());
        this.location = in.readParcelable(Location.class.getClassLoader());
        this.id = in.readString();
        this.createdAt = in.readString();
        this.width = (Integer) in.readValue(Integer.class.getClassLoader());
        this.height = (Integer) in.readValue(Integer.class.getClassLoader());
        this.color = in.readString();
        this.likes = (Integer) in.readValue(Integer.class.getClassLoader());
        this.likedByUser = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.user = in.readParcelable(User.class.getClassLoader());
        this.currentUserCollections = new ArrayList<>();
        in.readList(this.currentUserCollections, Object.class.getClassLoader());
        this.urls = in.readParcelable(Urls.class.getClassLoader());
        this.categories = new ArrayList<>();
        in.readList(this.categories, Category.class.getClassLoader());
        this.links = in.readParcelable(PhotoLinks.class.getClassLoader());
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel source) {
            return new Photo(source);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };
}
