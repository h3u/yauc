package com.bitsailer.yauc.api;

import com.bitsailer.yauc.api.model.AccessToken;
import com.bitsailer.yauc.api.model.Photo;
import com.bitsailer.yauc.api.model.SimplePhoto;
import com.bitsailer.yauc.api.model.User;

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interface that defines used endpoints of https://api.unsplash.com/
 * See https://unsplash.com/documentation
 */

public interface UnsplashAPI {

    String URL = "https://api.unsplash.com/";
    String OAUTH_URL = "https://unsplash.com/";
    String AUTHORIZATION_GRANT_TYPE = "authorization_code";
    String PERMISSION_SCOPE = "public+read_user+read_photos+write_photos+write_likes";
    int MAX_PER_PAGE = 30;

    /**
     * Get access token with authorization code.
     *
     * @param client_id client id of yauc
     * @param client_secret secret given
     * @param code the given code
     * @param grantType must be “authorization_code”
     * @return access token
     */
    @FormUrlEncoded
    @POST("oauth/token")
    Call<AccessToken> getAccessToken(
            @Field("client_id") String client_id,
            @Field("client_secret") String client_secret,
            @Field("redirect_uri") String redirectUri,
            @Field("code") String code,
            @Field("grant_type") String grantType);

    /**
     * Get list of photos
     *
     * @param page page number (default: 1)
     * @param perPage items per page (default: 10)
     * @param orderBy order of items ("latest", "oldest", "popular", default: "latest")
     * @return list of photos
     */
    @GET("photos")
    Call<List<SimplePhoto>> listPhotos(
            @Query("page") Integer page,
            @Query("per_page") Integer perPage,
            @Query("order_by") String orderBy);

    /**
     * Get a single photo
     *
     * @param id photo identifier
     * @return the requested photo
     */
    @GET("photos/{id}")
    Call<Photo> getPhoto(@Path("id") String id);

    /**
     * Create a new photo. See format at
     * https://unsplash.com/documentation#upload-a-photo
     *
     * @param body form-data with photo, location and exif data
     * @return the new created photo
     */
    @POST("photos")
    Call<Photo> createPhoto(@Body RequestBody body);

    /**
     * Update photo.
     *
     * @param id photo identifier
     * @param latitude location latitude
     * @param longitude location longitude
     * @param city location city
     * @param country location country
     * @param make exif camera maker
     * @param model exif camera model
     * @param exposure_time exif exposure_time
     * @param aperture_value exif aperture_value
     * @param focal_length exif focal_length
     * @param iso_speed_ratings exif iso_speed_ratings
     * @return the updated photo
     */
    @FormUrlEncoded
    @PUT("photos/{id}")
    Call<Photo> updatePhoto(
            @Field("id") String id, // required, rest optional
            @Field("location[latitude]") Double latitude,
            @Field("location[longitude]") Double longitude,
            @Field("location[city]") String city,
            @Field("location[country]") String country,
            @Field("exif[make]") String make,
            @Field("exif[model]") String model,
            @Field("exif[exposure_time]") String exposure_time,
            @Field("exif[aperture_value]") String aperture_value,
            @Field("exif[focal_length]") String focal_length,
            @Field("exif[iso_speed_ratings]") String iso_speed_ratings);

    /**
     * Get user data.
     * User is found through authorization header.
     *
     * @return the requested user
     */
    @GET("me")
    Call<User> getMe();

    /**
     * List photos the user has liked.
     *
     * @param username username that has liked the photos
     * @param page page number (default: 1)
     * @param perPage items per page (default: 10)
     * @param orderBy order of items ("latest", "oldest", "popular", default: "latest")
     * @return the users favorites
     */
    @GET("users/{username}/likes")
    Call<List<SimplePhoto>> listFavoritePhotos(
            @Path("username") String username,
            @Query("page") Integer page,
            @Query("per_page") Integer perPage,
            @Query("order_by") String orderBy);

    /**
     * List photos the user owns.
     *
     * @param username owner of photos
     * @param page page number (default: 1)
     * @param perPage items per page (default: 10)
     * @param orderBy order of items ("latest", "oldest", "popular", default: "latest")
     * @return photos owned by given user
     */
    @GET("users/{username}/photos")
    Call<List<SimplePhoto>> listUsersPhotos(
            @Path("username") String username,
            @Query("page") Integer page,
            @Query("per_page") Integer perPage,
            @Query("order_by") String orderBy);

    /**
     * Mark photo as favorite.
     *
     * @param id photo identifier
     * @return no returning content here
     */
    @POST("photos/{id}/like")
    Call<ResponseBody> likePhoto(@Path("id") String id);

    /**
     * Remove photo from favorites
     *
     * @param id photo identifier
     * @return no returning content here
     */
    @DELETE("photos/{id}/like")
    Call<ResponseBody> unlikePhoto(@Path("id") String id);
}
