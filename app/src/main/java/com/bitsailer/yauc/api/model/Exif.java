
package com.bitsailer.yauc.api.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

//@Generated("org.jsonschema2pojo")
public class Exif implements Parcelable {

    @SerializedName("make")
    @Expose
    private String make;

    @SerializedName("model")
    @Expose
    private String model;

    @SerializedName("exposure_time")
    @Expose
    private String exposureTime;

    @SerializedName("aperture")
    @Expose
    private String aperture;

    @SerializedName("focal_length")
    @Expose
    private String focalLength;

    @SerializedName("iso")
    @Expose
    private Integer iso;

    /**
     * 
     * @return
     *     The make
     */
    public String getMake() {
        return make != null ? make : "";
    }

    /**
     * 
     * @param make
     *     The make
     */
    public void setMake(String make) {
        this.make = make;
    }

    /**
     * 
     * @return
     *     The model
     */
    public String getModel() {
        return model != null ? model : "";
    }

    /**
     * 
     * @param model
     *     The model
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * 
     * @return
     *     The exposureTime
     */
    public String getExposureTime() {
        return exposureTime != null ? exposureTime : "";
    }

    /**
     * 
     * @param exposureTime
     *     The exposure_time
     */
    public void setExposureTime(String exposureTime) {
        this.exposureTime = exposureTime;
    }

    /**
     * 
     * @return
     *     The aperture
     */
    public String getAperture() {
        return aperture != null ? aperture : "";
    }

    /**
     * 
     * @param aperture
     *     The aperture
     */
    public void setAperture(String aperture) {
        this.aperture = aperture;
    }

    /**
     * 
     * @return
     *     The focalLength
     */
    public String getFocalLength() {
        return focalLength != null ? focalLength : "";
    }

    /**
     * 
     * @param focalLength
     *     The focal_length
     */
    public void setFocalLength(String focalLength) {
        this.focalLength = focalLength;
    }

    /**
     * 
     * @return
     *     The iso
     */
    public Integer getIso() {
        return iso;
    }

    /**
     * 
     * @param iso
     *     The iso
     */
    public void setIso(Integer iso) {
        this.iso = iso;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(make).append(model).append(exposureTime).append(aperture).append(focalLength).append(iso).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Exif)) {
            return false;
        }
        Exif rhs = ((Exif) other);
        return new EqualsBuilder().append(make, rhs.make).append(model, rhs.model).append(exposureTime, rhs.exposureTime).append(aperture, rhs.aperture).append(focalLength, rhs.focalLength).append(iso, rhs.iso).isEquals();
    }

    public boolean isCameraEmpty() {
        return TextUtils.isEmpty(make) && TextUtils.isEmpty(model);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.make);
        dest.writeString(this.model);
        dest.writeString(this.exposureTime);
        dest.writeString(this.aperture);
        dest.writeString(this.focalLength);
        dest.writeValue(this.iso);
    }

    public Exif() {
    }

    Exif(Parcel in) {
        this.make = in.readString();
        this.model = in.readString();
        this.exposureTime = in.readString();
        this.aperture = in.readString();
        this.focalLength = in.readString();
        this.iso = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    public static final Parcelable.Creator<Exif> CREATOR = new Parcelable.Creator<Exif>() {
        @Override
        public Exif createFromParcel(Parcel source) {
            return new Exif(source);
        }

        @Override
        public Exif[] newArray(int size) {
            return new Exif[size];
        }
    };
}
