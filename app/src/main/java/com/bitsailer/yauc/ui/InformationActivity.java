package com.bitsailer.yauc.ui;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bitsailer.yauc.R;
import com.bitsailer.yauc.Util;
import com.bitsailer.yauc.api.model.Photo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InformationActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, OnMapReadyCallback {

    private static final int LOADER_ID = 0;
    private Uri mUri;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private LatLng mLocation;
    @BindView(R.id.imageViewProfile)
    ImageView avatar;
    @BindView(R.id.textViewAuthorName)
    TextView authorName;
    @BindView(R.id.textViewAuthorProfileLink)
    TextView profileLink;
    @BindView(R.id.textViewPublishedAt)
    TextView datePublished;
    @BindView(R.id.layoutCamera)
    LinearLayout cameraLayout;
    @BindView(R.id.textViewCameraModel)
    TextView cameraModel;
    @BindView(R.id.textViewPhotoSize)
    TextView photoSize;
    @BindView(R.id.textViewPhotoIso)
    TextView photoIso;
    @BindView(R.id.textViewPhotoAperture)
    TextView photoAperture;
    @BindView(R.id.textViewPhotoExposureTime)
    TextView photoExposureTime;
    @BindView(R.id.textViewPhotoFocalLength)
    TextView photoFocalLength;
    @BindView(R.id.layoutLocation)
    LinearLayout locationLayout;
    @BindView(R.id.textViewLocation)
    TextView location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        ButterKnife.bind(this);

        // get content uri from intent
        mUri = getIntent().getData();

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.locationMap);
        mMapFragment.getMapAsync(this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                mUri,
                Util.getAllPhotoColumns(),
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 1 && data.moveToFirst()) {

            Photo photo = Photo.fromCursor(data);

            Glide.with(this)
                    .load(photo.getUser().getProfileImage().getLarge()).asBitmap()
                    .centerCrop()
                    .into(new BitmapImageViewTarget(avatar) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularAvatar = RoundedBitmapDrawableFactory
                                    .create(InformationActivity.this.getResources(), resource);
                            circularAvatar.setCircular(true);
                            avatar.setImageDrawable(circularAvatar);
                        }
                    });

            authorName.setText(photo.getUser().getName());
            if (photo.getUser().getPortfolioUrl() != null) {
                profileLink.setText(photo.getUser().getPortfolioUrl());
            } else {
                profileLink.setText(photo.getUser().getLinks().getHtml());
            }
            datePublished.setText(photo.getCreatedAt());
            if (photo.getExif().isCameraEmpty()) {
                cameraLayout.setVisibility(View.GONE);
            } else {
                cameraModel.setText(getString(R.string.information_make_model,
                        photo.getExif().getMake(), photo.getExif().getModel()));
            }
            photoSize.setText(getString(R.string.information_photo_size,
                    photo.getWidth(), photo.getHeight()));
            if (photo.getExif() != null) {
                if (photo.getExif().getIso() != 0) {
                    photoIso.setText(getString(R.string.information_photo_iso, photo.getExif().getIso()));
                } else {
                    photoIso.setVisibility(View.GONE);
                }
                Double aperture = 0.0;
                Double exposureTime = 0.0;
                Double focalLength = 0.0;
                try {
                    aperture = Double.parseDouble(photo.getExif().getAperture());
                    exposureTime = Double.parseDouble(photo.getExif().getExposureTime());
                    focalLength = Double.parseDouble(photo.getExif().getFocalLength());
                } catch (NumberFormatException e) {
                    Logger.e(e.getMessage());
                }
                if (aperture != 0.0) {
                    photoAperture
                            .setText(getString(R.string.information_photo_aperture, aperture));
                } else {
                    photoAperture.setVisibility(View.GONE);
                }
                if (exposureTime != 0.0) {
                    if (exposureTime < 1) {
                        photoExposureTime
                                .setText(getString(R.string.information_photo_exposure_time_short,
                                        1 / exposureTime));
                    } else {
                        photoExposureTime
                                .setText(getString(R.string.information_photo_exposure_time_long,
                                        exposureTime));
                    }
                } else {
                    photoExposureTime.setVisibility(View.GONE);
                }
                if (focalLength != 0.0) {
                    photoFocalLength
                            .setText(getString(R.string.information_photo_focal_length, focalLength));
                } else {
                    photoFocalLength.setVisibility(View.GONE);
                }
            }
            if (!photo.getLocation().isEmpty()) {
                location.setText(getString(R.string.information_location_data,
                        photo.getLocation().getCountry(),
                        photo.getLocation().getCity()));
                if (photo.getLocation().getPosition() != null) {
                    // add location and marker to map
                    mLocation = new LatLng(photo.getLocation().getPosition().getLatitude(),
                            photo.getLocation().getPosition().getLongitude());
                    setLocationAndMarker();
                } else {
                    hideMap();
                }
            } else {
                locationLayout.setVisibility(View.GONE);
                hideMap();
            }

        } else {
            // photo not found
            // todo: activate empty view
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void setLocationAndMarker() {
        if (mMap != null && mLocation != null) {
            mMap.addMarker(new MarkerOptions().position(mLocation));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mLocation));
        }
    }

    private void hideMap() {
        try {
            mMapFragment.getView().setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
    }
}
