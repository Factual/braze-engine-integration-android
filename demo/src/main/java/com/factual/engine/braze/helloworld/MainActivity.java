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
import com.factual.engine.api.FactualException;
import com.factual.engine.FactualEngine;

public class MainActivity extends AppCompatActivity {

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
    FactualEngine.setReceiver(ExampleFactualClientReceiver.class);
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

}
