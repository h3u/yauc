
package com.bitsailer.yauc.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

//@Generated("org.jsonschema2pojo")
@SuppressWarnings("ALL")
public class Urls implements Parcelable {

    @SerializedName("raw")
    @Expose
    private String raw;

    @SerializedName("full")
    @Expose
    private String full;

    @SerializedName("regular")
    @Expose
    private String regular;

    @SerializedName("small")
    @Expose
    private String small;

    @SerializedName("thumb")
    @Expose
    private String thumb;

    /**
     * 
     * @return
     *     The raw
     */
    public String getRaw() {
        return raw;
    }

    /**
     * 
     * @param raw
     *     The raw
     */
    public void setRaw(String raw) {
        this.raw = raw;
    }

    /**
     * 
     * @return
     *     The full
     */
    public String getFull() {
        return full;
    }

    /**
     * 
     * @param full
     *     The full
     */
    public void setFull(String full) {
        this.full = full;
    }

    /**
     * 
     * @return
     *     The regular
     */
    public String getRegular() {
        return regular;
    }

    /**
     * 
     * @param regular
     *     The regular
     */
    public void setRegular(String regular) {
        this.regular = regular;
    }

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
     *     The thumb
     */
    public String getThumb() {
        return thumb;
    }

    /**
     * 
     * @param thumb
     *     The thumb
     */
    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(raw).append(full).append(regular).append(small).append(thumb).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Urls)) {
            return false;
        }
        Urls rhs = ((Urls) other);
        return new EqualsBuilder().append(raw, rhs.raw).append(full, rhs.full).append(regular, rhs.regular).append(small, rhs.small).append(thumb, rhs.thumb).isEquals();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.raw);
        dest.writeString(this.full);
        dest.writeString(this.regular);
        dest.writeString(this.small);
        dest.writeString(this.thumb);
    }

    public Urls() {
    }

    Urls(Parcel in) {
        this.raw = in.readString();
        this.full = in.readString();
        this.regular = in.readString();
        this.small = in.readString();
        this.thumb = in.readString();
    }

    public static final Parcelable.Creator<Urls> CREATOR = new Parcelable.Creator<Urls>() {
        @Override
        public Urls createFromParcel(Parcel source) {
            return new Urls(source);
        }

        @Override
        public Urls[] newArray(int size) {
            return new Urls[size];
        }
    };
}
