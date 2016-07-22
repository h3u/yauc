
package com.bitsailer.yauc.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

//@Generated("org.jsonschema2pojo")
public class User {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("portfolio_url")
    @Expose
    private Object portfolioUrl;

    @SerializedName("profile_image")
    @Expose
    private ProfileImage profileImage;

    @SerializedName("links")
    @Expose
    private UserLinks links;

    @SerializedName("first_name")
    @Expose
    private String firstName;

    @SerializedName("last_name")
    @Expose
    private String lastName;

    @SerializedName("bio")
    @Expose
    private Object bio;

    @SerializedName("location")
    @Expose
    private Object location;

    @SerializedName("total_likes")
    @Expose
    private Integer totalLikes;

    @SerializedName("total_photos")
    @Expose
    private Integer totalPhotos;

    @SerializedName("total_collections")
    @Expose
    private Integer totalCollections;

    @SerializedName("downloads")
    @Expose
    private Integer downloads;

    @SerializedName("uid")
    @Expose
    private String uid;

    @SerializedName("uploads_remaining")
    @Expose
    private Integer uploadsRemaining;

    @SerializedName("instagram_username")
    @Expose
    private Object instagramUsername;

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("badge")
    @Expose
    private Object badge;

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
     *     The username
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @param username
     *     The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * @return
     *     The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     *     The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     *     The portfolioUrl
     */
    public Object getPortfolioUrl() {
        return portfolioUrl;
    }

    /**
     *
     * @param portfolioUrl
     *     The portfolio_url
     */
    public void setPortfolioUrl(Object portfolioUrl) {
        this.portfolioUrl = portfolioUrl;
    }

    /**
     *
     * @return
     *     The profileImage
     */
    public ProfileImage getProfileImage() {
        return profileImage;
    }

    /**
     *
     * @param profileImage
     *     The profile_image
     */
    public void setProfileImage(ProfileImage profileImage) {
        this.profileImage = profileImage;
    }

    /**
     *
     * @return
     *     The links
     */
    public UserLinks getLinks() {
        return links;
    }

    /**
     *
     * @param links
     *     The links
     */
    public void setLinks(UserLinks links) {
        this.links = links;
    }

    /**
     * 
     * @return
     *     The firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * 
     * @param firstName
     *     The first_name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * 
     * @return
     *     The lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * 
     * @param lastName
     *     The last_name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * 
     * @return
     *     The bio
     */
    public Object getBio() {
        return bio;
    }

    /**
     * 
     * @param bio
     *     The bio
     */
    public void setBio(Object bio) {
        this.bio = bio;
    }

    /**
     * 
     * @return
     *     The location
     */
    public Object getLocation() {
        return location;
    }

    /**
     * 
     * @param location
     *     The location
     */
    public void setLocation(Object location) {
        this.location = location;
    }

    /**
     * 
     * @return
     *     The totalLikes
     */
    public Integer getTotalLikes() {
        return totalLikes;
    }

    /**
     * 
     * @param totalLikes
     *     The total_likes
     */
    public void setTotalLikes(Integer totalLikes) {
        this.totalLikes = totalLikes;
    }

    /**
     * 
     * @return
     *     The totalPhotos
     */
    public Integer getTotalPhotos() {
        return totalPhotos;
    }

    /**
     * 
     * @param totalPhotos
     *     The total_photos
     */
    public void setTotalPhotos(Integer totalPhotos) {
        this.totalPhotos = totalPhotos;
    }

    /**
     * 
     * @return
     *     The totalCollections
     */
    public Integer getTotalCollections() {
        return totalCollections;
    }

    /**
     * 
     * @param totalCollections
     *     The total_collections
     */
    public void setTotalCollections(Integer totalCollections) {
        this.totalCollections = totalCollections;
    }

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
     *     The uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * 
     * @param uid
     *     The uid
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * 
     * @return
     *     The uploadsRemaining
     */
    public Integer getUploadsRemaining() {
        return uploadsRemaining;
    }

    /**
     * 
     * @param uploadsRemaining
     *     The uploads_remaining
     */
    public void setUploadsRemaining(Integer uploadsRemaining) {
        this.uploadsRemaining = uploadsRemaining;
    }

    /**
     * 
     * @return
     *     The instagramUsername
     */
    public Object getInstagramUsername() {
        return instagramUsername;
    }

    /**
     * 
     * @param instagramUsername
     *     The instagram_username
     */
    public void setInstagramUsername(Object instagramUsername) {
        this.instagramUsername = instagramUsername;
    }

    /**
     * 
     * @return
     *     The email
     */
    public String getEmail() {
        return email;
    }

    /**
     * 
     * @param email
     *     The email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 
     * @return
     *     The badge
     */
    public Object getBadge() {
        return badge;
    }

    /**
     * 
     * @param badge
     *     The badge
     */
    public void setBadge(Object badge) {
        this.badge = badge;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getUsername()).append(getName()).append(firstName).append(lastName).append(getPortfolioUrl()).append(bio).append(location).append(totalLikes).append(totalPhotos).append(totalCollections).append(downloads).append(getProfileImage()).append(uid).append(uploadsRemaining).append(instagramUsername).append(email).append(badge).append(getLinks()).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof User) == false) {
            return false;
        }
        User rhs = ((User) other);
        return new EqualsBuilder().append(getUsername(), rhs.getUsername()).append(getName(), rhs.getName()).append(firstName, rhs.firstName).append(lastName, rhs.lastName).append(getPortfolioUrl(), rhs.getPortfolioUrl()).append(bio, rhs.bio).append(location, rhs.location).append(totalLikes, rhs.totalLikes).append(totalPhotos, rhs.totalPhotos).append(totalCollections, rhs.totalCollections).append(downloads, rhs.downloads).append(getProfileImage(), rhs.getProfileImage()).append(uid, rhs.uid).append(uploadsRemaining, rhs.uploadsRemaining).append(instagramUsername, rhs.instagramUsername).append(email, rhs.email).append(badge, rhs.badge).append(getLinks(), rhs.getLinks()).isEquals();
    }

}
