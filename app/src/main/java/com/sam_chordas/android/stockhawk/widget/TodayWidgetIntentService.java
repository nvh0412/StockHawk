package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by HoaNV on 9/12/16.
 */
public class TodayWidgetIntentService extends IntentService {
  private static final String[] STOCK_COLUMNS = {
    QuoteColumns._ID,
    QuoteColumns.SYMBOL,
    QuoteColumns.BIDPRICE,
    QuoteColumns.ISUP,
    QuoteColumns.PERCENT_CHANGE
  };
  private static final int SYMBOL_INDEX = 1;
  private static final int PRICE_INDEX = 2;
  private static final int IS_UP_INDEX = 3;
  private static final int PERCENT_CHANGE_INDEX = 4;

  /**
   * Creates an IntentService.  Invoked by your subclass's constructor.
   */
  public TodayWidgetIntentService() {
    super("TodayWidgetIntentService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, TodayStockWidgetProvider.class));

    Uri stockUri = QuoteProvider.Quotes.withSymbol("YHOO");
    Cursor data = getContentResolver().query(stockUri, STOCK_COLUMNS, null, null, QuoteColumns.CREATED + " DESC");

    if (data == null) {
      return;
    }

    if (!data.moveToFirst()) {
      data.close();
      return;
    }

    int stockArtResourceId = data.getInt(IS_UP_INDEX) == 1 ?
      R.drawable.ic_trending_up_black_24dp :
      R.drawable.ic_trending_down_black_24dp;
    String description = data.getString(SYMBOL_INDEX);
    String stockPrice = data.getString(PRICE_INDEX);
    String percentChange = data.getString(PERCENT_CHANGE_INDEX);

    for (int appWidgetId : appWidgetIds) {
      int layoutId;
      int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
      int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
      int largeWidth = getResources().getDimensionPixelOffset(R.dimen.widget_today_large_width);

      if (widgetWidth >= largeWidth) {
        layoutId = R.layout.widget_today_large;
      } else if (widgetWidth >= defaultWidth) {
        layoutId = R.layout.widget_today;
      } else {
        layoutId = R.layout.widget_today_small;
      }

      RemoteViews remoteViews = new RemoteViews(getPackageName(), layoutId);

      remoteViews.setImageViewResource(R.id.widget_icon, stockArtResourceId);
      // Content Descriptions for RemoteViews were only added in ICS MR1
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
        setRemoteContentDescription(remoteViews, description);
      }
      remoteViews.setTextViewText(R.id.widget_stock_price, stockPrice);
      remoteViews.setTextViewText(R.id.bid_price, stockPrice);
      remoteViews.setTextViewText(R.id.percent_change, percentChange);

      Intent launchIntent = new Intent(this, MyStocksActivity.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
      remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

      appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }
  }

  private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
      return getResources().getDimensionPixelOffset(R.dimen.widget_today_default_width);
    }
    return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
    Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
    if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
      int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
      DisplayMetrics dm = getResources().getDisplayMetrics();
      return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp, dm);
    }
    return getResources().getDimensionPixelOffset(R.dimen.widget_today_default_width);
  }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
  private void setRemoteContentDescription(RemoteViews views, String description) {
    views.setContentDescription(R.id.widget_icon, description);
  }
}
