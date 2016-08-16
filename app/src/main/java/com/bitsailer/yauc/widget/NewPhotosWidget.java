package com.bitsailer.yauc.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.bitsailer.yauc.sync.SyncAdapter;

/**
 * Implementation of App Widget functionality.
 */
public class NewPhotosWidget extends AppWidgetProvider {

    public static final String EXTRA_NUM_PHOTOS = "extra_num_photos";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, NewPhotosWidgetIntentService.class));
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (SyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            int numNewPhotos = intent.getIntExtra(SyncAdapter.EXRTA_NUM_INSERTED, 0);
            Intent intentService = new Intent(context, NewPhotosWidgetIntentService.class);
            intentService.putExtra(EXTRA_NUM_PHOTOS, numNewPhotos);
            context.startService(intentService);
        }
    }
}

