package com.bitsailer.yauc.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bitsailer.yauc.R;
import com.bitsailer.yauc.api.model.SimplePhoto;
import com.bumptech.glide.Glide;
import com.orhanobut.logger.Logger;

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

    public PhotoListAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    PhotoListAdapter(Context context, Cursor cursor, int columns) {
        super(context, cursor);
        mColumnCount = columns;
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
        viewHolder.imageViewPhoto.setBackgroundColor(getBackgroundColor(photo.getColor()));
        viewHolder.imageViewPhoto.setAspectRatio((photo.getWidth() / (float) photo.getHeight()));
        Glide.with(getContext())
                .load(photo.getUrls().getSmall())
                .fitCenter()
                .into(viewHolder.imageViewPhoto);
    }

    class PhotoListItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(photo)
        DynamicImageView imageViewPhoto;

        PhotoListItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private int getBackgroundColor(String color) {
        int intColor = Color.TRANSPARENT;
        if (color != null && !TextUtils.isEmpty(color)) {
            try {
                intColor = Color.parseColor(color);
            } catch (IllegalArgumentException e) {
                Logger.e("background color <%s> failed", color, e);
            }
        }
        return intColor;
    }
}