
package com.bitsailer.yauc.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

//@Generated("org.jsonschema2pojo")
public class SimplePhoto {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("created_at")
    @Expose
    private String createdAt;

    @SerializedName("width")
    @Expose
    private Integer width;

    @SerializedName("height")
    @Expose
    private Integer height;

    @SerializedName("color")
    @Expose
    private String color;

    @SerializedName("likes")
    @Expose
    private Integer likes;

    @SerializedName("liked_by_user")
    @Expose
    private Boolean likedByUser;

    @SerializedName("user")
    @Expose
    private User user;

    @SerializedName("current_user_collections")
    @Expose
    private List<Object> currentUserCollections = new ArrayList<Object>();

    @SerializedName("urls")
    @Expose
    private Urls urls;

    @SerializedName("categories")
    @Expose
    private List<Category> categories = new ArrayList<Category>();

    @SerializedName("links")
    @Expose
    private PhotoLinks links;

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
        if ((other instanceof SimplePhoto) == false) {
            return false;
        }
        SimplePhoto rhs = ((SimplePhoto) other);
        return new EqualsBuilder().append(id, rhs.id).append(createdAt, rhs.createdAt).append(width, rhs.width).append(height, rhs.height).append(color, rhs.color).append(likes, rhs.likes).append(likedByUser, rhs.likedByUser).append(user, rhs.user).append(currentUserCollections, rhs.currentUserCollections).append(urls, rhs.urls).append(categories, rhs.categories).append(links, rhs.links).isEquals();
    }

}
