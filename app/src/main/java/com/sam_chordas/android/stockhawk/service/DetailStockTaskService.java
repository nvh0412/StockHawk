package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by HoaNV on 9/7/16.
 */
public class DetailStockTaskService extends GcmTaskService {

  private static final String LOG_TAG = DetailStockTaskService.class.getSimpleName();
  private OkHttpClient client = new OkHttpClient();
  private Context mContext;

  public DetailStockTaskService(){}

  public DetailStockTaskService(Context mContext) {
    this.mContext = mContext;
  }

  private String fetchData(String url) throws IOException {
    Log.d(LOG_TAG, "URL: " + url);
    Request request = new Request.Builder()
      .url(url)
      .build();

    Response response = client.newCall(request).execute();
    return response.body().string();
  }

  @Override
  public int onRunTask(TaskParams params) {
    if (mContext == null){
      mContext = this;
    }
    StringBuilder urlStringBuilder = new StringBuilder();
    String symbol = null;
    try{
      // Base URL for the Yahoo query
      urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");

      Calendar calendar = Calendar.getInstance();

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      String endDate = sdf.format(calendar.getTime());

      calendar.add(Calendar.DAY_OF_MONTH, -7);
      String startDate = sdf.format(calendar.getTime());

      symbol = params.getExtras().getString("symbol");

      urlStringBuilder.append(
        URLEncoder.encode("select * from   yahoo.finance.historicaldata\n" +
            " where symbol  = \"" + symbol + "\"\n" +
            " and startDate = \"" + startDate + "\"\n" +
            " and endDate   = \"" + endDate + "\"",
          "UTF-8")
      );
      urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
        + "org%2Falltableswithkeys&callback=");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    String urlString;
    String getResponse;
    int result = GcmNetworkManager.RESULT_FAILURE;

    urlString = urlStringBuilder.toString();
    try{
      getResponse = fetchData(urlString);
      result = GcmNetworkManager.RESULT_SUCCESS;
      try {
        mContext.getContentResolver().delete(QuoteProvider.DetailQuotes.withSymbol(symbol), null, null);
        mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY, Utils.detailQuoteJsonToContentVals(getResponse));
      }catch (RemoteException | OperationApplicationException e){
        Log.e(LOG_TAG, "Error applying batch insert", e);
      }
    } catch (IOException e){
      e.printStackTrace();
    }
    return result;
  }
}
