package com.bitsailer.yauc.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bitsailer.yauc.Preferences;
import com.bitsailer.yauc.R;
import com.bitsailer.yauc.Util;
import com.bitsailer.yauc.api.model.Photo;
import com.bitsailer.yauc.event.PhotoLikedEvent;
import com.bitsailer.yauc.event.PhotoUnlikedEvent;
import com.bitsailer.yauc.sync.PhotoManagement;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Detail view of Photo.
 */
public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 0;
    private Uri mUri;
    private String mPhotoId;
    private String mShareUrl;
    private boolean mFavorite = false;
    private int mLikes = 0;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHideSystemViewsRunnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    };
    @BindView(R.id.fullscreen_content_controls)
    View mControlsView;

    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    @BindView(R.id.textViewEmpty)
    TextView mTextViewEmptyMessage;

    @BindView(R.id.fullscreenPhoto)
    ImageView mContentView;

    @BindView(R.id.buttonLike)
    Button mLikeButton;

    private final Runnable mShowControlsRunnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private final Runnable mHideControlsRunnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            mControlsView.setVisibility(View.GONE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        // get content uri from intent
        mUri = getIntent().getData();

        PhotoManagement.amendPhoto(this, mUri.getLastPathSegment(), false);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // UI first
        // hide status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // hide bottom bar
        mHideHandler.postDelayed(mHideControlsRunnable, UI_ANIMATION_DELAY);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowControlsRunnable);
        mHideHandler.postDelayed(mHideSystemViewsRunnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // show status bar
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHideSystemViewsRunnable);
        mHideHandler.postDelayed(mShowControlsRunnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
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
            mPhotoId = photo.getId();
            mShareUrl = photo.getLinks().getHtml();
            mFavorite = photo.getLikedByUser();
            mLikes = photo.getLikes();
            toggleLikeButton(mFavorite, mLikes);
            Glide.with(this)
                    .load(photo.getUrls().getRegular())
                    .listener(new RequestListener<String, GlideDrawable>() {

                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            mProgressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .fitCenter()
                    .crossFade()
                    .into(mContentView);
            delayedHide(1000);
        } else {
            // photo not found
            mProgressBar.setVisibility(View.GONE);
            mControlsView.setVisibility(View.GONE);
            mTextViewEmptyMessage.setVisibility(View.VISIBLE);
        }
    }

    private void toggleLikeButton(boolean liked, int likes) {
        if (liked) {
            mLikeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_favorite_white, 0, 0);
            mLikeButton.setText(String.format("%d", likes));
        } else {
            mLikeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white, 0, 0);
            mLikeButton.setText(String.format("%d", likes));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @OnClick(R.id.buttonShare)
    public void onShareButtonClicked() {
        if (mShareUrl != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, mShareUrl));
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)));
        }
    }

    @OnClick(R.id.buttonLike)
    public void onLikeButtonClicked() {
        if (!Preferences.get(this).isAuthenticated()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.like_dialog_sign_in_message);
            builder.setPositiveButton(R.string.button_dialog_positive, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked Sign in button
                    startActivity(new Intent(DetailActivity.this, LoginActivity.class));
                }
            });
            builder.setNegativeButton(R.string.button_dialog_dismiss, null);
            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            if (mFavorite) {
                // already liked => unlike
                mFavorite = false;
                toggleLikeButton(false, mLikes);
                PhotoManagement.unlikePhoto(this, mPhotoId);
            } else {
                // like it
                mFavorite = true;
                toggleLikeButton(true, mLikes);
                PhotoManagement.likePhoto(this, mPhotoId);
            }
        }
    }

    @OnClick(R.id.buttonInfo)
    public void onInfoButtonClicked() {
        Intent intent = new Intent(this, InformationActivity.class)
                .setData(mUri);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhotoLiked(PhotoLikedEvent event) {
        Toast.makeText(this, R.string.liked_message, Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhotoUnliked(PhotoUnlikedEvent event) {
        Toast.makeText(this, R.string.unliked_message, Toast.LENGTH_SHORT).show();
    }
}
