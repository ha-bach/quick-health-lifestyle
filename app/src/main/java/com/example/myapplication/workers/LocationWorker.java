package com.example.myapplication.workers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationWorker extends Worker {

    private static final String TAG = "LocationWorker";

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            Log.e(TAG, "Location permission not granted");
            return Result.failure();
        }
        return Result.success();
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // Create a FusedLocationProviderClient
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.d(TAG, "Last known location: " + location.getLatitude() + ", " + location.getLongitude());

                            Data outputData = new Data.Builder()
                                    .putDouble("latitude", location.getLatitude())
                                    .putDouble("longitude", location.getLongitude())
                                    .putLong("timestamp", location.getTime())
                                    .build();

                            Result.success(outputData);
                        } else {
                            Log.e(TAG, "Last known location is null");
                            Result.failure();
                        }
                    }
                });
    }
}
