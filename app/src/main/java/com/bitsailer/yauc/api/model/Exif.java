
package com.bitsailer.yauc.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

//@Generated("org.jsonschema2pojo")
public class Exif {

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
        return make;
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
        return model;
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
        return exposureTime;
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
        return aperture;
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
        return focalLength;
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
        if ((other instanceof Exif) == false) {
            return false;
        }
        Exif rhs = ((Exif) other);
        return new EqualsBuilder().append(make, rhs.make).append(model, rhs.model).append(exposureTime, rhs.exposureTime).append(aperture, rhs.aperture).append(focalLength, rhs.focalLength).append(iso, rhs.iso).isEquals();
    }

}
