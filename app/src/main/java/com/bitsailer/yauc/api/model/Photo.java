
package com.bitsailer.yauc.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

//@Generated("org.jsonschema2pojo")
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
    public void setExif(Exif exif) {
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
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
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
        if ((other instanceof Photo) == false) {
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

}
