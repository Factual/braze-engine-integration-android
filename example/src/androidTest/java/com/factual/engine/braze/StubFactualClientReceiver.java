package com.factual.engine.braze;

import android.util.Log;
import com.factual.engine.FactualClientReceiver;

public class StubFactualClientReceiver extends FactualClientReceiver {

  static final Object lock = new Object();

  @Override
  public void onStarted() {
    Log.i("engine", "Engine has started.");

    synchronized (lock) {
      lock.notifyAll();
    }
  }
}
