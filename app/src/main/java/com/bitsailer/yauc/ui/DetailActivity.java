package com.bitsailer.yauc.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bitsailer.yauc.Preferences;
import com.bitsailer.yauc.R;
import com.bitsailer.yauc.Util;
import com.bitsailer.yauc.YaucApplication;
import com.bitsailer.yauc.api.model.Photo;
import com.bitsailer.yauc.event.PhotoLikedEvent;
import com.bitsailer.yauc.event.PhotoUnlikedEvent;
import com.bitsailer.yauc.sync.PhotoManagement;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Detail view of Photo.
 */
@SuppressWarnings("unused")
public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 0;
    private Uri mUri;
    private String mPhotoId;
    private String mShareUrl;
    private boolean mFavorite = false;
    private int mLikes = 0;
    private Tracker mTracker;

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

    @BindView(R.id.textViewAuthor)
    TextView mAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        Window w = getWindow(); // in Activity's onCreate() for instance
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // get content uri from intent
        mUri = getIntent().getData();

        PhotoManagement.amendPhoto(this, mUri.getLastPathSegment(), false);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        // Analytics
        mTracker = ((YaucApplication) getApplication()).getDefaultTracker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Analytics track screen name
        mTracker.setScreenName(getString(R.string.ga_name_detail_activity));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
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
            mAuthor.setText(getString(R.string.text_author, photo.getUser().getName()));
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
                    .centerCrop()
                    .crossFade()
                    .into(mContentView);
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
            mLikeButton.setText(String.format(Locale.getDefault(), "%d", likes));
            mLikeButton.setContentDescription(getString(R.string.button_detail_unlike_content_description));
        } else {
            mLikeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white, 0, 0);
            mLikeButton.setText(String.format(Locale.getDefault(), "%d", likes));
            mLikeButton.setContentDescription(getString(R.string.button_detail_like_content_description));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @OnClick(R.id.buttonShare)
    public void onShareButtonClicked() {
        if (mShareUrl != null) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.ga_category_action))
                    .setAction(getString(R.string.ga_action_share))
                    .build());
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
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(getString(R.string.ga_category_action))
                        .setAction(getString(R.string.ga_action_unlike))
                        .build());
            } else {
                // like it
                mFavorite = true;
                toggleLikeButton(true, mLikes);
                PhotoManagement.likePhoto(this, mPhotoId);
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(getString(R.string.ga_category_action))
                        .setAction(getString(R.string.ga_action_like))
                        .build());
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

    @SuppressWarnings("UnusedParameters")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhotoLiked(PhotoLikedEvent event) {
        Toast.makeText(this, R.string.liked_message, Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("UnusedParameters")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhotoUnliked(PhotoUnlikedEvent event) {
        Toast.makeText(this, R.string.unliked_message, Toast.LENGTH_SHORT).show();
    }
}
