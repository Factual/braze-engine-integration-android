package com.factual.engine.braze.helloworld;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.appboy.support.AppboyLogger;
import com.factual.FactualCircumstance;
import com.factual.FactualConfigMetadata;
import com.factual.FactualError;
import com.factual.FactualException;
import com.factual.FactualInfo;
import com.factual.engine.FactualClientListener;
import com.factual.engine.FactualEngine;
import com.factual.engine.braze.BrazeEngineIntegration;

public class MainActivity extends AppCompatActivity implements FactualClientListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    try {
      initializeEngine();
      if (isRequiredPermissionAvailable()) {
        startEngine();
      } else {
        requestLocationPermissions();
      }
    } catch (FactualException e) {
      Log.e("engine", e.getMessage());
    }

  }

  public void initializeEngine() throws FactualException {
    Log.i("engine", "starting initialization");
    Resources res = getResources();
    FactualEngine.initialize(getApplicationContext(), res.getString(R.string.com_factual_engine_api_key));
    FactualEngine.setListener(this);
    Log.i("engine", "initialization complete");
  }

  private void startEngine() {
    try {
      AppboyLogger.setLogLevel(Log.VERBOSE);
      FactualEngine.start();
    } catch (FactualException e) {
      Log.e("engine", e.getMessage());
    }
  }

  /*********************************** Permission Boiler plate ************************************/

  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    if (isRequiredPermissionAvailable()) {
      startEngine();
    } else {
      Log.e("engine", "Necessary permissions were never provided.");
    }
  }

  public boolean isRequiredPermissionAvailable(){
    return ContextCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this,
            Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
  }

  public void requestLocationPermissions() {
    ActivityCompat.requestPermissions(
        this,
        new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET
        },
        0);
  }

  /*********************************** FactualClientListener **************************************/

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
    if (data.getGarageRelease() != null) {
      for (FactualCircumstance circumstance : data.getGarageRelease().getCircumstances()){
        Log.i("engine", "loaded circumstance: " + circumstance.getCircumstanceId());
      }
    } else {
      Log.i("engine", "Garage release is empty");
    }
  }

  @Override
  public void onDiagnosticMessage(String diagnosticMessage) {
    Log.i("engine", diagnosticMessage);
  }
}
