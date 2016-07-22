
package com.bitsailer.yauc.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

//@Generated("org.jsonschema2pojo")
public class Category {

    @SerializedName("id")
    @Expose
    private Integer id;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("photo_count")
    @Expose
    private Integer photoCount;

    @SerializedName("links")
    @Expose
    private CategoryLinks categoryLinks;

    /**
     * 
     * @return
     *     The id
     */
    public Integer getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The title
     */
    public String getTitle() {
        return title;
    }

    /**
     * 
     * @param title
     *     The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 
     * @return
     *     The photoCount
     */
    public Integer getPhotoCount() {
        return photoCount;
    }

    /**
     * 
     * @param photoCount
     *     The photo_count
     */
    public void setPhotoCount(Integer photoCount) {
        this.photoCount = photoCount;
    }

    /**
     * 
     * @return
     *     The categoryLinks
     */
    public CategoryLinks getCategoryLinks() {
        return categoryLinks;
    }

    /**
     * 
     * @param categoryLinks
     *     The categoryLinks
     */
    public void setCategoryLinks(CategoryLinks categoryLinks) {
        this.categoryLinks = categoryLinks;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(title).append(photoCount).append(categoryLinks).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Category) == false) {
            return false;
        }
        Category rhs = ((Category) other);
        return new EqualsBuilder().append(id, rhs.id).append(title, rhs.title).append(photoCount, rhs.photoCount).append(categoryLinks, rhs.categoryLinks).isEquals();
    }

}
