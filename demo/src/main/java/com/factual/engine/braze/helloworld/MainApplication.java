package com.factual.engine.braze.helloworld;

import android.app.Application;
import android.content.res.Resources;
import android.util.Log;
import com.factual.FactualConfigMetadata;
import com.factual.FactualError;
import com.factual.FactualException;
import com.factual.FactualInfo;
import com.factual.engine.FactualClientListener;
import com.factual.engine.FactualEngine;
import com.factual.engine.braze.BrazeEngineIntegration;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initializeFactualEngine();
    }

    private void initializeFactualEngine() {
        try {
            Resources res = getResources();
            FactualEngine.initialize(getApplicationContext(), res.getString(R.string.com_factual_engine_api_key));
        } catch (FactualException e) {
            throw new RuntimeException(e);
        }
        FactualEngine.setListener(new FactualEngineListener()); //should this go before initialize?
    }

    public class FactualEngineListener implements FactualClientListener {
        @Override
        public void onStarted() {
            Log.i("engine", "Engine has started.");
            BrazeEngineIntegration.initializeBrazeEngineIntegration(getApplicationContext());
        }

        @Override
        public void onStopped() {
            Log.i("engine", "Engine has stopped.");
        }

        @Override
        public void onError(FactualError e) {
            Log.i("engine", e.getMessage());
        }

        @Override
        public void onInfo(FactualInfo i) {
            Log.i("engine", i.getInfo());
        }

        @Override
        public void onSyncWithGarageComplete() {
            Log.i("engine", "Garage synced.");
        }

        @Override
        public void onConfigLoad(FactualConfigMetadata data) {
            Log.i("engine", "Garage config loaded at: " + data.getVersion());
        }
    }
}
