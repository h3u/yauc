
package com.bitsailer.yauc.api.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.bitsailer.yauc.data.PhotoColumns;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

//@Generated("org.jsonschema2pojo")
@SuppressWarnings("ALL")
public class SimplePhoto implements Parcelable {

    @SerializedName("id")
    @Expose
    protected String id;

    protected long completedAt;

    @SerializedName("created_at")
    @Expose
    protected String createdAt;

    @SerializedName("width")
    @Expose
    protected Integer width;

    @SerializedName("height")
    @Expose
    protected Integer height;

    @SerializedName("color")
    @Expose
    protected String color;

    @SerializedName("likes")
    @Expose
    protected Integer likes;

    @SerializedName("liked_by_user")
    @Expose
    protected Boolean likedByUser;

    @SerializedName("user")
    @Expose
    protected User user;

    @SerializedName("current_user_collections")
    @Expose
    protected List<Object> currentUserCollections = new ArrayList<>();

    @SerializedName("urls")
    @Expose
    protected Urls urls;

    @SerializedName("categories")
    @Expose
    protected List<Category> categories = new ArrayList<Category>();

    @SerializedName("links")
    @Expose
    protected PhotoLinks links;

    /**
     * 
     * @return
     *     The id
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    public void setId(String id) {
        this.id = id;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }

    /**
     * 
     * @return
     *     The createdAt
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * 
     * @param createdAt
     *     The created_at
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 
     * @return
     *     The width
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * 
     * @param width
     *     The width
     */
    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * 
     * @return
     *     The height
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * 
     * @param height
     *     The height
     */
    public void setHeight(Integer height) {
        this.height = height;
    }

    /**
     * 
     * @return
     *     The color
     */
    public String getColor() {
        return color;
    }

    /**
     * 
     * @param color
     *     The color
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * 
     * @return
     *     The likes
     */
    public Integer getLikes() {
        return likes;
    }

    /**
     * 
     * @param likes
     *     The likes
     */
    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    /**
     * 
     * @return
     *     The likedByUser
     */
    public Boolean getLikedByUser() {
        return likedByUser;
    }

    /**
     * 
     * @param likedByUser
     *     The liked_by_user
     */
    public void setLikedByUser(Boolean likedByUser) {
        this.likedByUser = likedByUser;
    }

    /**
     * 
     * @return
     *     The user
     */
    public User getUser() {
        return user;
    }

    /**
     * 
     * @param user
     *     The user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * 
     * @return
     *     The currentUserCollections
     */
    public List<Object> getCurrentUserCollections() {
        return currentUserCollections;
    }

    /**
     * 
     * @param currentUserCollections
     *     The current_user_collections
     */
    public void setCurrentUserCollections(List<Object> currentUserCollections) {
        this.currentUserCollections = currentUserCollections;
    }

    /**
     * 
     * @return
     *     The urls
     */
    public Urls getUrls() {
        return urls;
    }

    /**
     * 
     * @param urls
     *     The urls
     */
    public void setUrls(Urls urls) {
        this.urls = urls;
    }

    /**
     * 
     * @return
     *     The categories
     */
    public List<Category> getCategories() {
        return categories;
    }

    /**
     * 
     * @param categories
     *     The categories
     */
    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    /**
     * 
     * @return
     *     The links
     */
    public PhotoLinks getLinks() {
        return links;
    }

    /**
     * 
     * @param links
     *     The links
     */
    public void setLinks(PhotoLinks links) {
        this.links = links;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(createdAt).append(width).append(height).append(color).append(likes).append(likedByUser).append(user).append(currentUserCollections).append(urls).append(categories).append(links).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof SimplePhoto)) {
            return false;
        }
        SimplePhoto rhs = ((SimplePhoto) other);
        return new EqualsBuilder().append(id, rhs.id).append(createdAt, rhs.createdAt).append(width, rhs.width).append(height, rhs.height).append(color, rhs.color).append(likes, rhs.likes).append(likedByUser, rhs.likedByUser).append(user, rhs.user).append(currentUserCollections, rhs.currentUserCollections).append(urls, rhs.urls).append(categories, rhs.categories).append(links, rhs.links).isEquals();
    }

    public static SimplePhoto fromCursor(Cursor cursor) {
        SimplePhoto photo = new SimplePhoto();
        if (cursor != null) {
            photo.setId(cursor.getString(cursor.getColumnIndex(PhotoColumns.PHOTO_ID)));
            photo.setColor(cursor.getString(cursor.getColumnIndex(PhotoColumns.PHOTO_COLOR)));
            photo.setWidth(cursor.getInt(cursor.getColumnIndex(PhotoColumns.PHOTO_WIDTH)));
            photo.setHeight(cursor.getInt(cursor.getColumnIndex(PhotoColumns.PHOTO_HEIGHT)));
            Urls urls = new Urls();
            urls.setSmall(cursor.getString(cursor.getColumnIndex(PhotoColumns.URLS_SMALL)));
            photo.setUrls(urls);
        }

        return photo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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

    public SimplePhoto() {
    }

    protected SimplePhoto(Parcel in) {
        this.id = in.readString();
        this.createdAt = in.readString();
        this.width = (Integer) in.readValue(Integer.class.getClassLoader());
        this.height = (Integer) in.readValue(Integer.class.getClassLoader());
        this.color = in.readString();
        this.likes = (Integer) in.readValue(Integer.class.getClassLoader());
        this.likedByUser = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.user = in.readParcelable(User.class.getClassLoader());
        this.currentUserCollections = new ArrayList<Object>();
        in.readList(this.currentUserCollections, Object.class.getClassLoader());
        this.urls = in.readParcelable(Urls.class.getClassLoader());
        this.categories = new ArrayList<Category>();
        in.readList(this.categories, Category.class.getClassLoader());
        this.links = in.readParcelable(PhotoLinks.class.getClassLoader());
    }

    public static final Creator<SimplePhoto> CREATOR = new Creator<SimplePhoto>() {
        @Override
        public SimplePhoto createFromParcel(Parcel source) {
            return new SimplePhoto(source);
        }

        @Override
        public SimplePhoto[] newArray(int size) {
            return new SimplePhoto[size];
        }
    };

    public boolean isOwnedBy(String username) {
        if (user != null && user.getUsername().equals(username)) {
            return true;
        }

        return false;
    }
}
