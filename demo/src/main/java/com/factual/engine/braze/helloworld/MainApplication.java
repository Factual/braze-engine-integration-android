package com.factual.engine.braze.helloworld;

import android.app.Application;
import android.util.Log;
import com.appboy.Appboy;
import com.appboy.configuration.AppboyConfig;
import com.appboy.support.AppboyLogger;
import com.factual.FactualConfigMetadata;
import com.factual.FactualError;
import com.factual.FactualException;
import com.factual.FactualInfo;
import com.factual.engine.FactualClientListener;
import com.factual.engine.FactualEngine;
import com.factual.engine.braze.EngineBrazeIntegration;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initializeAppBoy();
        initializeFactualEngine();
    }

    private void initializeAppBoy() {
        AppboyLogger.setLogLevel(Log.VERBOSE);
        AppboyConfig appboyConfig = new AppboyConfig.Builder()
                .setApiKey("YOUR_BRAZE_APP_API_KEY")
                .build();
        Appboy.configure(this, appboyConfig);
    }

    private void initializeFactualEngine() {
        try {
            FactualEngine.initialize(getApplicationContext(), "YOUR_FACTUAL_GARAGE_API_KEY");
        } catch (FactualException e) {
            throw new RuntimeException(e);
        }
        FactualEngine.setListener(new FactualEngineListener());
    }

    public class FactualEngineListener implements FactualClientListener {
        @Override
        public void onStarted() {
            Log.i("engine", "Engine has started.");
            EngineBrazeIntegration.initializeEngineBrazeIntegration(getApplicationContext());
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
