package com.bitsailer.yauc.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.Toast;

import com.bitsailer.yauc.R;
import com.bitsailer.yauc.Util;
import com.bitsailer.yauc.YaucApplication;
import com.bitsailer.yauc.api.model.Photo;
import com.bitsailer.yauc.sync.PhotoManagement;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.DecimalMax;
import com.mobsandgeeks.saripaar.annotation.DecimalMin;
import com.mobsandgeeks.saripaar.annotation.Max;
import com.mobsandgeeks.saripaar.annotation.Min;
import com.mobsandgeeks.saripaar.annotation.Optional;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditPhotoActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, Validator.ValidationListener {

    private static final int LOADER_ID = 0;
    private Uri mUri;
    private Photo mPhoto;
    private Validator mValidator;

    @Optional
    @BindView(R.id.exifCameraMaker) TextInputEditText mExifCameraMaker;

    @Optional
    @BindView(R.id.exifCameraModel) TextInputEditText mExifCameraModel;

    @Optional
    @DecimalMin(value = 0.5, messageResId = R.string.edit_text_exif_exposure_aperture_min)
    @DecimalMax(value = 256.0, messageResId = R.string.edit_text_exif_exposure_aperture_max)
    @BindView(R.id.exifAperture) TextInputEditText mExifAperture;

    @Optional
    @DecimalMin(value = 0.00005, messageResId = R.string.edit_text_exif_exposure_time_min)
    @DecimalMax(value = 86400.0, messageResId = R.string.edit_text_exif_exposure_time_max)
    @BindView(R.id.exifExposureTime) TextInputEditText mExifExposureTime;

    @Optional
    @DecimalMin(value = 1.0, messageResId = R.string.edit_text_exif_exposure_focal_length_min)
    @DecimalMax(value = 2000.0, messageResId = R.string.edit_text_exif_exposure_focal_length_max)
    @BindView(R.id.exifFocalLength) TextInputEditText mExifFocalLength;

    @Optional
    @Min(value = 25, messageResId = R.string.edit_text_exif_exposure_iso_min)
    @Max(value = 409600, messageResId = R.string.edit_text_exif_exposure_iso_max)
    @BindView(R.id.exifIso) TextInputEditText mExifIso;

    @Optional
    @BindView(R.id.locationCountry) TextInputEditText mLocationCountry;

    @Optional
    @BindView(R.id.locationCity) TextInputEditText mLocationCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);

        // get content uri from intent
        mUri = getIntent().getData();

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black);

        mValidator = new Validator(this);
        mValidator.setValidationListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Analytics track screen name
        Tracker tracker = ((YaucApplication) getApplication()).getDefaultTracker();
        tracker.setScreenName(getString(R.string.ga_name_edit_photo_activity));
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            mValidator.validate();
            mPhoto.getExif().setAperture(mExifAperture.getText().toString());
            mPhoto.getExif().setExposureTime(mExifExposureTime.getText().toString());
            mPhoto.getExif().setFocalLength(mExifFocalLength.getText().toString());
            mPhoto.getExif().setIso(Integer.parseInt(mExifIso.getText().toString()));
            mPhoto.getExif().setMake(mExifCameraMaker.getText().toString());
            mPhoto.getExif().setModel(mExifCameraModel.getText().toString());
            mPhoto.getLocation().setCountry(mLocationCountry.getText().toString());
            mPhoto.getLocation().setCity(mLocationCity.getText().toString());
            PhotoManagement.editPhoto(this, mPhoto);
            finish();
        }

        return super.onOptionsItemSelected(item);
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
            mPhoto = Photo.fromCursor(data);
            preFillForm();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void preFillForm() {
        if (mPhoto != null) {
            if (mPhoto.getExif().getMake() != null) {
                mExifCameraMaker.setText(mPhoto.getExif().getMake());
            }
            if (mPhoto.getExif().getModel() != null) {
                mExifCameraModel.setText(mPhoto.getExif().getModel());
            }
            if (mPhoto.getExif().getAperture() != null) {
                mExifAperture.setText(mPhoto.getExif().getAperture());
            }
            mExifAperture.addTextChangedListener(new EditPhotoTextWatcher(mExifAperture));
            if (mPhoto.getExif().getExposureTime() != null) {
                mExifExposureTime.setText(String.format(Locale.getDefault(),
                        "%s", mPhoto.getExif().getExposureTime()));
            }
            mExifExposureTime.addTextChangedListener(new EditPhotoTextWatcher(mExifExposureTime));
            if (mPhoto.getExif().getFocalLength() != null) {
                mExifFocalLength.setText(mPhoto.getExif().getFocalLength());
            }
            mExifFocalLength.addTextChangedListener(new EditPhotoTextWatcher(mExifFocalLength));
            if (mPhoto.getExif().getIso() != null) {
                mExifIso.setText(String.format(Locale.getDefault(),
                        "%d", mPhoto.getExif().getIso()));
            }
            mExifIso.addTextChangedListener(new EditPhotoTextWatcher(mExifIso));
            if (mPhoto.getLocation().getCountry() != null) {
                mLocationCountry.setText(mPhoto.getLocation().getCountry());
            }
            if (mPhoto.getLocation().getCity() != null) {
                mLocationCity.setText(mPhoto.getLocation().getCity());
            }
        }
    }

    @Override
    public void onValidationSucceeded() {
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            ViewParent viewParent = view.getParent();
            String message = error.getCollatedErrorMessage(this);
            if (viewParent instanceof TextInputLayout) {
                ((TextInputLayout) viewParent).setErrorEnabled(true);
                ((TextInputLayout) viewParent).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    class EditPhotoTextWatcher implements TextWatcher {

        TextInputEditText mEditText;

        public EditPhotoTextWatcher(TextInputEditText mEditText) {
            this.mEditText = mEditText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            TextInputLayout layout = (TextInputLayout) mEditText.getParent();
            layout.setErrorEnabled(false);
            layout.setError(null);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mValidator.validate();
        }
    }
}
