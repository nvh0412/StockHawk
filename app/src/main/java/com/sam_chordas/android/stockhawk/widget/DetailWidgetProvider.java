package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by HoaNV on 9/13/16.
 */

public class DetailWidgetProvider extends AppWidgetProvider {
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    for (int appWidgetId : appWidgetIds) {
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_detail);

      Intent intent = new Intent(context, MyStocksActivity.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
      views.setOnClickPendingIntent(R.id.widget, pendingIntent);

      // Set up the collection
      setRemoteAdapter(context, views);

      boolean useDetailActivity = context.getResources().getBoolean(R.bool.use_detail_activity);

      Intent clickIntentTemplate = new Intent(context, MyStocksActivity.class);

      PendingIntent clickPendingIntent = TaskStackBuilder.create(context)
        .addNextIntentWithParentStack(clickIntentTemplate)
        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

      views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntent);
      views.setEmptyView(R.id.widget_list, R.id.widget_empty);

      appWidgetManager.updateAppWidget(appWidgetId, views);
    }
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);
    if (StockTaskService.ACTION_DATE_UPDATED.equals(intent.getAction())) {
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
      int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
        new ComponentName(context, getClass()));
      appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
    }
  }

  private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
    views.setRemoteAdapter(R.id.widget_list,
      new Intent(context, DetailWidgetRemoteViewsService.class));
  }
}
