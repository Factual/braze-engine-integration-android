package com.factual.engine.braze;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.support.AppboyLogger;
import com.factual.engine.FactualEngine;
import com.factual.engine.api.FactualException;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "BrazeEngine";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Appboy appboy = Appboy.getInstance(getApplicationContext());
    appboy.changeUser(Configuration.BRAZE_USER_ID);
    appboy.getCurrentUser().setEmail(Configuration.BRAZE_USER_EMAIL);

    /* Set up Engine */
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
    FactualEngine.initialize(getApplicationContext(), Configuration.ENGINE_API_KEY);
    FactualEngine.setReceiver(ExampleFactualClientReceiver.class);
    FactualEngine.setUserJourneyReceiver(BrazeEngineUserJourneyReceiver.class);
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

  /* Permission Boiler plate */

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
      @NonNull int[] grantResults) {
    if (isRequiredPermissionAvailable()) {
      startEngine();
    } else {
      Log.e(TAG, "Necessary permissions were never provided.");
    }
  }

  public boolean isRequiredPermissionAvailable() {
    return ContextCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
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
}