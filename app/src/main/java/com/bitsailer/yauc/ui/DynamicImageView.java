package com.bitsailer.yauc.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Change height of image view in respect to the aspect ratio.
 * This keeps the character of a photo with a fixed width even
 * if displayed in a (staggered) grid.
 * https://developer.android.com/training/custom-views/create-view.html
 * http://stackoverflow.com/questions/13992535/android-imageview-scale-smaller-image-to-width-with-flexible-height-without-crop
 * Created by Uli Wucherer (u.wucherer@gmail.com) on 07/08/16.
 */

public class DynamicImageView extends ImageView {
    private float mRatio = 1.0f;

    public DynamicImageView(Context context) {
        super(context);
    }

    public DynamicImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAspectRatio(float ratio) {
        mRatio = ratio;
        invalidate();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int measuredWidth = getMeasuredWidth();
        final int height = (int) Math.ceil((measuredWidth / mRatio));
        setMeasuredDimension(measuredWidth, height);
    }
}
