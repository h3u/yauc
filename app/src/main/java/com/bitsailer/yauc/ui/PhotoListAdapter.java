package com.bitsailer.yauc.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bitsailer.yauc.R;
import com.bitsailer.yauc.Util;
import com.bitsailer.yauc.api.model.SimplePhoto;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bitsailer.yauc.R.id.photo;

/**
 * {@link RecyclerView.Adapter} that can display a {@link SimplePhoto}.
 */
class PhotoListAdapter extends CursorRecyclerViewAdapter<PhotoListAdapter.PhotoListItemViewHolder> {

    private static final int VIEW_TYPE_GRID = 0;
    private static final int VIEW_TYPE_LIST = 1;
    private int mColumnCount = 2;
    private final PhotoOnClickHandler mClickHandler;
    private boolean mUseRegularPhotoSize = false;

    PhotoListAdapter(Context context, int columns, PhotoOnClickHandler clickHandler) {
        super(context, null);
        mColumnCount = columns;
        mClickHandler = clickHandler;
    }

    interface PhotoOnClickHandler {
        void onClick(String photoId, PhotoListItemViewHolder vh);
    }

    PhotoListAdapter setColumnCount(int columnCount) {
        mColumnCount = columnCount;
        return this;
    }

    PhotoListAdapter setUseRegularPhotoSize(boolean useRegularPhotoSize) {
        mUseRegularPhotoSize = useRegularPhotoSize;
        return this;
    }

    @Override
    public int getItemViewType(int position) {
        return mColumnCount != 1 ? VIEW_TYPE_GRID : VIEW_TYPE_LIST;
    }

    @Override
    public PhotoListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = R.layout.photo_grid_item;
        if (viewType == VIEW_TYPE_LIST) {
            layout = R.layout.photo_list_item;
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
        return new PhotoListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotoListItemViewHolder viewHolder, Cursor cursor) {
        SimplePhoto photo = SimplePhoto.fromCursor(cursor);
        viewHolder.setPhotoId(photo.getId());
        viewHolder.imageViewPhoto.setBackgroundColor(Util.getBackgroundColor(photo.getColor()));
        viewHolder.imageViewPhoto.setAspectRatio((photo.getWidth() / (float) photo.getHeight()));

        // for bigger screens using the list layout prevent blurry photos
        String url = photo.getUrls().getSmall();
        if (mUseRegularPhotoSize && mColumnCount == 1) {
            DrawableRequestBuilder thumbnailRequest = Glide.with(getContext())
                    .load(url) // small photo
                    .fitCenter();
            url = photo.getUrls().getRegular();
            Glide.with(getContext())
                    .load(url)
                    .thumbnail(thumbnailRequest)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .fitCenter()
                    .dontAnimate()
                    .into(viewHolder.imageViewPhoto);
        } else {
            Glide.with(getContext())
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .fitCenter()
                    .into(viewHolder.imageViewPhoto);
        }
    }

    @SuppressWarnings("unused")
    class PhotoListItemViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        @BindView(photo)
        DynamicImageView imageViewPhoto;

        private String mPhotoId;

        PhotoListItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        void setPhotoId(String photoId) {
            mPhotoId = photoId;
        }

        @Override
        public void onClick(View v) {
            if (mPhotoId != null) {
                mClickHandler.onClick(mPhotoId, this);
            }
        }
    }
}