package com.bitsailer.yauc.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bitsailer.yauc.R;
import com.bitsailer.yauc.api.model.SimplePhoto;
import com.bumptech.glide.Glide;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link RecyclerView.Adapter} that can display a {@link SimplePhoto}.
 */
public class PhotoListAdapter extends CursorRecyclerViewAdapter<PhotoListAdapter.PhotoListItemViewHolder> {

    public PhotoListAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public PhotoListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_grid_item, parent, false);
        return new PhotoListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotoListItemViewHolder viewHolder, Cursor cursor) {
        SimplePhoto photo = SimplePhoto.fromCursor(cursor);
        viewHolder.mImageViewPhoto.setBackgroundColor(getBackgroundColor(photo.getColor()));
        Glide.with(getContext())
                .load(photo.getUrls().getSmall())
                .centerCrop()
                .into(viewHolder.mImageViewPhoto);
    }

    public class PhotoListItemViewHolder extends RecyclerView.ViewHolder {
        public SimplePhoto mItem;
        @BindView(R.id.photo)
        ImageView mImageViewPhoto;

        public PhotoListItemViewHolder(View view) {
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