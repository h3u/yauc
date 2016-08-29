
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
public class PhotoLinks implements Parcelable {

    @SerializedName("self")
    @Expose
    private String self;

    @SerializedName("html")
    @Expose
    private String html;

    @SerializedName("download")
    @Expose
    private String download;

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
     *     The download
     */
    public String getDownload() {
        return download;
    }

    /**
     * 
     * @param download
     *     The download
     */
    public void setDownload(String download) {
        this.download = download;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(self).append(html).append(download).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PhotoLinks)) {
            return false;
        }
        PhotoLinks rhs = ((PhotoLinks) other);
        return new EqualsBuilder().append(self, rhs.self).append(html, rhs.html).append(download, rhs.download).isEquals();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.self);
        dest.writeString(this.html);
        dest.writeString(this.download);
    }

    public PhotoLinks() {
    }

    PhotoLinks(Parcel in) {
        this.self = in.readString();
        this.html = in.readString();
        this.download = in.readString();
    }

    public static final Parcelable.Creator<PhotoLinks> CREATOR = new Parcelable.Creator<PhotoLinks>() {
        @Override
        public PhotoLinks createFromParcel(Parcel source) {
            return new PhotoLinks(source);
        }

        @Override
        public PhotoLinks[] newArray(int size) {
            return new PhotoLinks[size];
        }
    };
}
