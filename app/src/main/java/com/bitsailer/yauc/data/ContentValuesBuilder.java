package com.bitsailer.yauc.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;

import com.bitsailer.yauc.api.model.Photo;
import com.bitsailer.yauc.api.model.SimplePhoto;
import com.bitsailer.yauc.provider.values.PhotosValuesBuilder;
import com.orhanobut.logger.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Builder for ContentValues for a Photo or SimplePhoto.
 * Created by Uli Wucherer (u.wucherer@gmail.com) on 27/07/16.
 */

public class ContentValuesBuilder {
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    private static PhotosValuesBuilder buildBase(PhotosValuesBuilder builder, SimplePhoto photo) {
        Date createdAt = new Date();
        try {
            createdAt = mDateFormat.parse(photo.getCreatedAt());
        } catch (ParseException e) {
            Logger.e(e.getMessage());
        }
        builder
                .photoId(photo.getId())
                .photoColor(photo.getColor())
                .photoCreatedAt(createdAt.getTime())
                .photoLikedByUser(photo.getLikedByUser() ? 1 : 0)
                .photoHeight(photo.getHeight())
                .photoLikes(photo.getLikes())
                .photoWidth(photo.getWidth())
                .linksDownload(photo.getLinks().getDownload())
                .linksHtml(photo.getLinks().getHtml())
                .linksSelf(photo.getLinks().getSelf())
                .urlsFull(photo.getUrls().getFull())
                .urlsRaw(photo.getUrls().getRaw())
                .urlsRegular(photo.getUrls().getRaw())
                .urlsSmall(photo.getUrls().getSmall())
                .urlsThumb(photo.getUrls().getThumb())
                .userId(photo.getUser().getId())
                .userUsername(photo.getUser().getUsername())
                .userName(photo.getUser().getName());
        return builder;
    }

    public static ContentValues from(Photo photo) {
        PhotosValuesBuilder builder = new PhotosValuesBuilder();
        builder = buildBase(builder, photo);
        builder
                .photoDownloads(photo.getDownloads())
                .exifAperture(photo.getExif().getAperture())
                .exifExposureTime(photo.getExif().getExposureTime())
                .exifFocalLength(photo.getExif().getFocalLength())
                .exifIso(photo.getExif().getIso())
                .exifMake(photo.getExif().getMake())
                .exifModel(photo.getExif().getModel());
        if (photo.getLocation() != null) {
            builder
                    .locationCity(photo.getLocation().getCity())
                    .locationCountry(photo.getLocation().getCountry());
                    if (photo.getLocation().getPosition() != null) {
                        builder
                            .locationLatitude(photo.getLocation().getPosition().getLatitude())
                            .locationLongitude(photo.getLocation().getPosition().getLongitude());
                    }
        }
        builder
                .userPortfolioUrl(photo.getUser().getPortfolioUrl())
                .userProfileImageLarge(photo.getUser().getProfileImage().getLarge())
                .userProfileImageMedium(photo.getUser().getProfileImage().getMedium())
                .userProfileImageSmall(photo.getUser().getProfileImage().getSmall())
                .userLinksHtml(photo.getUser().getLinks().getHtml())
                .userLinksLikes(photo.getUser().getLinks().getLikes())
                .userLinksPhotos(photo.getUser().getLinks().getPhotos())
                .userLinksSelf(photo.getUser().getLinks().getSelf());
        return builder.values();
    }

    public static ContentValues from(SimplePhoto photo) {

        PhotosValuesBuilder builder = new PhotosValuesBuilder();
        builder = buildBase(builder, photo);

        if (photo.getUser().getPortfolioUrl() != null) {
            builder.userPortfolioUrl(photo.getUser().getPortfolioUrl());
        }

        builder
                .userProfileImageLarge(photo.getUser().getProfileImage().getLarge())
                .userProfileImageMedium(photo.getUser().getProfileImage().getMedium())
                .userProfileImageSmall(photo.getUser().getProfileImage().getSmall())
                .userLinksHtml(photo.getUser().getLinks().getHtml())
                .userLinksLikes(photo.getUser().getLinks().getLikes())
                .userLinksPhotos(photo.getUser().getLinks().getPhotos())
                .userLinksSelf(photo.getUser().getLinks().getSelf());
        return builder.values();
    }
}
