package com.bitsailer.yauc.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.widget.RemoteViews;

import com.bitsailer.yauc.R;
import com.bitsailer.yauc.ui.MainActivity;


/**
 * An {@link IntentService} subclass to update all instances of NewPhotosWidget.
 */
public class NewPhotosWidgetIntentService extends IntentService {

    public NewPhotosWidgetIntentService() {
        super("NewPhotosWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            int numNewPhotos = intent.getIntExtra(NewPhotosWidget.EXTRA_NUM_PHOTOS, 0);

            // Retrieve all of the Today widget ids: these are the widgets we need to update
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                    NewPhotosWidget.class));

            for (int appWidgetId : appWidgetIds) {
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.new_photos_widget);
                Resources res = getResources();
                String text = String.format(
                        res.getString(R.string.widget_new_photos_text), numNewPhotos);
                views.setTextViewText(R.id.appwidget_text, text);

                // Create launch Intent
                Intent launchIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
                views.setOnClickPendingIntent(R.id.widget, pendingIntent);

                // Tell the AppWidgetManager to perform an update on the current app widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }
}
