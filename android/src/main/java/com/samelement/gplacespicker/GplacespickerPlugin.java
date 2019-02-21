package com.samelement.gplacespicker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** GplacespickerPlugin */
public class GplacespickerPlugin implements MethodCallHandler,
        PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener {
  final int REQUEST_PLACE_PICKER = 10001;
  final int REQUEST_FINE_LOCATION_PERMISSION = 20001;
  final int REQUEST_LOCATION_SETTINGS = 30001;

  final Activity activity;
  Result currentResult;

  public GplacespickerPlugin(Activity activity) {
    this.activity = activity;
  }

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "gplacespicker");
    GplacespickerPlugin instance = new GplacespickerPlugin(registrar.activity());
    registrar.addActivityResultListener(instance);
    registrar.addRequestPermissionsResultListener(instance);
    channel.setMethodCallHandler(instance);
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    currentResult = result;

    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    }
    else if (call.method.equals("openPlacePicker")) {
      if (checkPermission()) {
        turnOnLocationSetting();
      }
      else {
        requestPermission();
      }
    }
    else if (call.method.equals("openNavigation")) {
      double latitude = 0.0;
      double longitude = 0.0;

      if (call.hasArgument("latitude")) {
        latitude = call.argument("latitude");
      }

      if (call.hasArgument("longitude")) {
        longitude = call.argument("longitude");
      }

      Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
              Uri.parse("google.navigation:q="+latitude+","+longitude));
      activity.startActivity(intent);
    }
    else {
      result.notImplemented();
    }
  }

  private boolean checkPermission() {
    int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
    if (result == PackageManager.PERMISSION_GRANTED) {
      return true;
    }
    else {
      return false;
    }
  }

  private void requestPermission() {
    ActivityCompat.requestPermissions(activity,
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
            REQUEST_FINE_LOCATION_PERMISSION);
  }

  private void turnOnLocationSetting() {
    LocationRequest locationRequest = new LocationRequest();
    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
      builder.addLocationRequest(locationRequest);

    Task<LocationSettingsResponse> result =
            LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build());
    result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
      @Override
      public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
        try {
          task.getResult(ApiException.class);
          openPlacePicker();
        } catch (ApiException exception) {
          switch (exception.getStatusCode()) {
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
              // Location settings are not satisfied. But could be fixed by showing the
              // user a dialog.
              try {
                // Cast to a resolvable exception.
                ResolvableApiException resolvable = (ResolvableApiException) exception;
                // Show the dialog by calling startResolutionForResult(),
                // and check the result in onActivityResult().
                resolvable.startResolutionForResult(
                        activity,
                        REQUEST_LOCATION_SETTINGS);
              } catch (IntentSender.SendIntentException e) {
                // Ignore the error.
              } catch (ClassCastException e) {
                // Ignore, should be an impossible error.
              }
              break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

              break;
          }
        }
      }
    });
  }

  private void openPlacePicker() {
    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

    try {
      activity.startActivityForResult(builder.build(activity), REQUEST_PLACE_PICKER);
    } catch (GooglePlayServicesRepairableException e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    } catch (GooglePlayServicesNotAvailableException e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (requestCode == REQUEST_PLACE_PICKER) {
      if (resultCode == Activity.RESULT_OK) {
        Place place = PlacePicker.getPlace(activity, intent);
        if (currentResult != null) {
          currentResult.success("{\"latitude\": "+place.getLatLng().latitude+
                  ",\"longitude\": "+place.getLatLng().longitude+
                  ",\"place\": \""+place.getAddress()+"\"}");
        }
        return true;
      }
      else if (resultCode == Activity.RESULT_CANCELED) {
        currentResult.success(null);
        return false;
      }
    }

    if (requestCode == REQUEST_LOCATION_SETTINGS) {
      if (resultCode == Activity.RESULT_OK) {
        openPlacePicker();
      }
      else if (resultCode == Activity.RESULT_CANCELED) {
        currentResult.success(null);
        return false;
      }
    }
    return false;
  }

  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode == REQUEST_FINE_LOCATION_PERMISSION) {
      if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        turnOnLocationSetting();
      }
    }
    return false;
  }
}
