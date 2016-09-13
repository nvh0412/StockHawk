package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by HoaNV on 9/13/16.
 */
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
  private static final String[] STOCK_COLUMNS = {
    QuoteColumns._ID,
    QuoteColumns.SYMBOL,
    QuoteColumns.BIDPRICE,
    QuoteColumns.ISUP,
    QuoteColumns.PERCENT_CHANGE
  };
  private static final int STOCK_ID_INDEX = 0;
  private static final int SYMBOL_INDEX = 1;
  private static final int PRICE_INDEX = 2;
  private static final int IS_UP_INDEX = 3;
  private static final int PERCENT_CHANGE_INDEX = 4;

  @Override
  public RemoteViewsFactory onGetViewFactory(Intent intent) {
    return new RemoteViewsFactory() {
      private Cursor data = null;

      @Override
      public void onCreate() {

      }

      @Override
      public void onDataSetChanged() {
        if (data != null) {
          data.close();
        }

        final long identityToken = Binder.clearCallingIdentity();
        data = getContentResolver().query(
          QuoteProvider.Quotes.CONTENT_URI,
          STOCK_COLUMNS,
          QuoteColumns.ISCURRENT + " = ?",
          new String[]{"1"},
          null);
        Binder.restoreCallingIdentity(identityToken);
      }

      @Override
      public void onDestroy() {
        if (data != null) {
          data.close();
          data = null;
        }
      }

      @Override
      public int getCount() {
        return data == null ? 0 : data.getCount();
      }

      @Override
      public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
          data == null || !data.moveToPosition(position)) {
          return null;
        }
        RemoteViews views = new RemoteViews(getPackageName(),
          R.layout.widget_detail_list_item);

        int icon = data.getInt(IS_UP_INDEX) == 1 ?
          R.drawable.ic_trending_up_black_24dp
          : R.drawable.ic_trending_down_black_24dp;

        views.setImageViewResource(R.id.widget_item_icon, icon);
        views.setTextViewText(R.id.widget_symbol, data.getString(SYMBOL_INDEX));
        views.setTextViewText(R.id.widget_bid_price, data.getString(PRICE_INDEX));
        views.setTextViewText(R.id.widget_percent_change, data.getString(PERCENT_CHANGE_INDEX));

        return views;
      }

      @Override
      public RemoteViews getLoadingView() {
        return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
      }

      @Override
      public int getViewTypeCount() {
        return 1;
      }

      @Override
      public long getItemId(int position) {
        if (data.moveToPosition(position))
          return data.getLong(STOCK_ID_INDEX);
        return position;
      }

      @Override
      public boolean hasStableIds() {
        return true;
      }
    };
  }
}
