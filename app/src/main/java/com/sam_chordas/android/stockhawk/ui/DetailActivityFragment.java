package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.DetailQuoteColumns;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final int DETAIL_LOADER = 0;
  private static final String DETAIL_URI = "DU";
  private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
  private Uri mUriData;
  @BindView(R.id.detail_stock_symbol_textview) TextView mStockSymbolTV;
  @BindView(R.id.detail_stock_bid_price_textview) TextView mStockBidPriceTV;
  @BindView(R.id.detail_stock_percent_change_textview) TextView mPercentChangeTV;
  @BindView(R.id.linechart) LineChartView mLineChartView;

  private static final String[] STOCK_COLUMNS = {
    DetailQuoteColumns._ID,
    DetailQuoteColumns.DATE,
    DetailQuoteColumns.OPEN,
    DetailQuoteColumns.CLOSE,
    DetailQuoteColumns.HIGH,
    DetailQuoteColumns.LOW
  };

  private static final int DATE_INDEX = 1;
  private static final int OPEN_INDEX = 2;
  private static final int CLOSE_INDEX = 3;
  private static final int HIGH_INDEX = 4;
  private static final int LOW_INDEX = 6;

  public DetailActivityFragment() {
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getLoaderManager().initLoader(DETAIL_LOADER, null, this);

    Bundle arguments = getArguments();
    if (arguments != null) {
      mUriData = arguments.getParcelable(DETAIL_URI);
    } else {
      mUriData = getActivity().getIntent().getData();
    }

    Intent mServiceIntent = new Intent(getActivity(), StockIntentService.class);
    mServiceIntent.putExtra("type", "detail");
    mServiceIntent.putExtra("symbol", mUriData.getPathSegments().get(1));
    getActivity().startService(mServiceIntent);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_detail, container, false);

    AppCompatActivity activity = (AppCompatActivity)getActivity();
    Toolbar toolbarView = (Toolbar) view.findViewById(R.id.toolbar);
    if ( null != toolbarView ) {
      activity.setSupportActionBar(toolbarView);
      activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
      activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    if (mUriData != null) {
      return new CursorLoader(getActivity(), mUriData, STOCK_COLUMNS, null, null, null);
    }
    return null;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    if (null != data && data.moveToFirst()) {
      List<Float> values = new ArrayList<>();
      List<String> mLabels = new ArrayList<>();

      Intent intent = getActivity().getIntent();
      mStockBidPriceTV.setText(intent.getStringExtra("bid_price"));
      mStockSymbolTV.setText(intent.getStringExtra("symbol"));
      mPercentChangeTV.setText(intent.getStringExtra("percent_change"));

      do {
        values.add(Float.parseFloat(data.getString(HIGH_INDEX)) % 10);
        mLabels.add(data.getString(DATE_INDEX));
      } while (data.moveToNext());

      Log.d(LOG_TAG, values.toString());

      Float[] reverseValues = values.toArray(new Float[values.size()]);
      ArrayUtils.reverse(reverseValues);
      LineSet dataset = new LineSet(
        mLabels.toArray(new String[mLabels.size()]),
        ArrayUtils.toPrimitive(reverseValues)
      );

      dataset.setColor(Color.parseColor("#758cbb"))
        .setFill(Color.parseColor("#2d374c"))
        .setDotsColor(Color.parseColor("#758cbb"))
        .setThickness(4)
        .setDashed(new float[]{10f,10f});
      mLineChartView.addData(dataset);

      mLineChartView.setBorderSpacing(Tools.fromDpToPx(0))
        .setYLabels(AxisController.LabelPosition.NONE)
        .setLabelsColor(Color.parseColor("#6a84c3"))
        .setXAxis(false)
        .setYAxis(false);

      mLineChartView.show();
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) { }
}
