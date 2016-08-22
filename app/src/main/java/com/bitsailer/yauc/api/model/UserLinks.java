
package com.bitsailer.yauc.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

//@Generated("org.jsonschema2pojo")
public class UserLinks implements Parcelable {

    @SerializedName("self")
    @Expose
    private String self;

    @SerializedName("html")
    @Expose
    private String html;

    @SerializedName("photos")
    @Expose
    private String photos;

    @SerializedName("likes")
    @Expose
    private String likes;

    /**
     * 
     * @return
     *     The self
     */
    public String getSelf() {
        return self;
    }

    /**
     * 
     * @param self
     *     The self
     */
    public void setSelf(String self) {
        this.self = self;
    }

    /**
     * 
     * @return
     *     The html
     */
    public String getHtml() {
        return html;
    }

    /**
     * 
     * @param html
     *     The html
     */
    public void setHtml(String html) {
        this.html = html;
    }

    /**
     * 
     * @return
     *     The photos
     */
    public String getPhotos() {
        return photos;
    }

    /**
     * 
     * @param photos
     *     The photos
     */
    public void setPhotos(String photos) {
        this.photos = photos;
    }

    /**
     * 
     * @return
     *     The likes
     */
    public String getLikes() {
        return likes;
    }

    /**
     * 
     * @param likes
     *     The likes
     */
    public void setLikes(String likes) {
        this.likes = likes;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(self).append(html).append(photos).append(likes).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UserLinks) == false) {
            return false;
        }
        UserLinks rhs = ((UserLinks) other);
        return new EqualsBuilder().append(self, rhs.self).append(html, rhs.html).append(photos, rhs.photos).append(likes, rhs.likes).isEquals();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.self);
        dest.writeString(this.html);
        dest.writeString(this.photos);
        dest.writeString(this.likes);
    }

    public UserLinks() {
    }

    protected UserLinks(Parcel in) {
        this.self = in.readString();
        this.html = in.readString();
        this.photos = in.readString();
        this.likes = in.readString();
    }

    public static final Parcelable.Creator<UserLinks> CREATOR = new Parcelable.Creator<UserLinks>() {
        @Override
        public UserLinks createFromParcel(Parcel source) {
            return new UserLinks(source);
        }

        @Override
        public UserLinks[] newArray(int size) {
            return new UserLinks[size];
        }
    };
}
