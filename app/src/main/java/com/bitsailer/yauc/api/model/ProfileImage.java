
package com.bitsailer.yauc.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

//@Generated("org.jsonschema2pojo")
public class ProfileImage implements Parcelable {

    @SerializedName("small")
    @Expose
    private String small;

    @SerializedName("medium")
    @Expose
    private String medium;

    @SerializedName("large")
    @Expose
    private String large;

    /**
     * 
     * @return
     *     The small
     */
    public String getSmall() {
        return small;
    }

    /**
     * 
     * @param small
     *     The small
     */
    public void setSmall(String small) {
        this.small = small;
    }

    /**
     * 
     * @return
     *     The medium
     */
    public String getMedium() {
        return medium;
    }

    /**
     * 
     * @param medium
     *     The medium
     */
    public void setMedium(String medium) {
        this.medium = medium;
    }

    /**
     * 
     * @return
     *     The large
     */
    public String getLarge() {
        return large;
    }

    /**
     * 
     * @param large
     *     The large
     */
    public void setLarge(String large) {
        this.large = large;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(small).append(medium).append(large).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProfileImage) == false) {
            return false;
        }
        ProfileImage rhs = ((ProfileImage) other);
        return new EqualsBuilder().append(small, rhs.small).append(medium, rhs.medium).append(large, rhs.large).isEquals();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.small);
        dest.writeString(this.medium);
        dest.writeString(this.large);
    }

    public ProfileImage() {
    }

    protected ProfileImage(Parcel in) {
        this.small = in.readString();
        this.medium = in.readString();
        this.large = in.readString();
    }

    public static final Parcelable.Creator<ProfileImage> CREATOR = new Parcelable.Creator<ProfileImage>() {
        @Override
        public ProfileImage createFromParcel(Parcel source) {
            return new ProfileImage(source);
        }

        @Override
        public ProfileImage[] newArray(int size) {
            return new ProfileImage[size];
        }
    };
}
