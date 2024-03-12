package com.example.myapplication.workers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class LocationWorker extends Worker {

    private static final String TAG = "LocationWorker";
    Location location;

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting work. ");
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
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location lastLocation) {
                        if (lastLocation != null) {
                            Log.d(TAG, "Last known location: " + lastLocation.getLatitude() + ", " + lastLocation.getLongitude());
                            location = lastLocation;
                            pushToFirestore();
                        } else {
                            Log.e(TAG, "Last known location is null");
                        }
                    }
                });
    }

    private void pushToFirestore() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String docId = formatter.format(location.getTime());
        Map<String, Object> locationPost = new HashMap<>();
        locationPost.put("longitude", location.getLongitude());
        locationPost.put("latitude", location.getLatitude());
        locationPost.put("time", location.getTime());

        // TODO: replace with code getting current user ID
        String userId = "YnfZYzNM1OqMnXgqyA6D";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference postsRef = db.collection("users")
                .document(userId)
                .collection("locationHistory");

        postsRef.document(docId)
                .set(locationPost)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Location data pushed to Firestore with ID: " + docId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error pushing location data to Firestore", e);
                    }
                });
    }
}
