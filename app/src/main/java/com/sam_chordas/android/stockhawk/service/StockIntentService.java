package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.gcm.TaskParams;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

  public StockIntentService(){
    super(StockIntentService.class.getName());
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Bundle args = new Bundle();
    args.putString("symbol", intent.getStringExtra("symbol"));
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.
    if (("detail").equals(intent.getStringExtra("type"))) {
      DetailStockTaskService stockTaskService = new DetailStockTaskService(this);
      stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
    } else {
      StockTaskService stockTaskService = new StockTaskService(this);
      stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
    }
  }
}
