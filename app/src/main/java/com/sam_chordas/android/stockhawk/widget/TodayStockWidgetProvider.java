package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by HoaNV on 9/12/16.
 */
public class TodayStockWidgetProvider extends AppWidgetProvider {
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    int stockArtResourceId = R.drawable.ic_drawer;
    String description = "24%";
    String stockPrice = "220";

    for (int appWidgetId : appWidgetIds) {
      int layoutId = R.layout.widget_today_small;
      RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);

      remoteViews.setImageViewResource(R.id.widget_icon, stockArtResourceId);
      // Content Descriptions for RemoteViews were only added in ICS MR1
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
        setRemoteContentDescription(remoteViews, description);
      }
      remoteViews.setTextViewText(R.id.widget_stock_price, stockPrice);

      Intent launchIntent = new Intent(context, MyStocksActivity.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
      remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

      appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }
  }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
  private void setRemoteContentDescription(RemoteViews views, String description) {
    views.setContentDescription(R.id.widget_icon, description);
  }
}
