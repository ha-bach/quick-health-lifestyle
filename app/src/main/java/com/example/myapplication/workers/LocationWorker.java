package com.example.myapplication.workers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myapplication.R;
import com.example.myapplication.ui.notifications.GymNotifActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class LocationWorker extends Worker {

    private static final String TAG = "LocationWorker";
    Location location;
    FirebaseAuth auth;
    FirebaseUser user;

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    @NonNull
    @Override
    public Result doWork() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Log.e(TAG, "Location permission not granted");
            return Result.failure();
        }
        return Result.success();
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location lastLocation) {
                        if (lastLocation != null) {
                            Log.d(TAG, "Last known location: " + lastLocation.getLatitude() + ", " + lastLocation.getLongitude());
                            location = lastLocation;
                            pushToFirestore();
                            fetchNearbyGym(location.getLatitude(), location.getLongitude());
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

        String userId = user.getUid();
        Log.d(TAG, "User ID: " + userId);
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

    void fetchNearbyGym(double latitude, double longitude) {
        OkHttpClient client = new OkHttpClient();
        String apiKey = "AIzaSyAPtLBOP5-S3G7gDN0n7-ZE6MjRUioxjOc";
        String baseUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
        String requestUrl = baseUrl + "?location=" + latitude + "," + longitude + "&radius=5000&type=gym&key=" + apiKey;

        Request request = new Request.Builder()
                .url(requestUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error fetching nearby gyms", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    // Parse the JSON response to log the names of the gyms
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray results = jsonObject.getJSONArray("results");
                        for (int i = 0; i < 5; i++) {
                            JSONObject gym = results.getJSONObject(i);
                            String gymName = gym.getString("name");
                            Log.i(TAG, "Found gym: " + gymName);
                        }
                        if (results.length() > 0) {
                            Calendar cal = Calendar.getInstance();
//                            int hour = cal.get(Calendar.HOUR_OF_DAY);
                            int hour = 0;
                            // Check if it's 5 PM
                            if (hour == 0) {
                                showNotification();
                                String n = "done";
                                Log.i("Show2 Notification", n);
                                hour++;
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing response", e);
                    }
                } else {
                    Log.e(TAG, "Failed to fetch nearby gyms");
                }
            }
        });
    }

//    private void showNotification() {
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "notifyGym")
//                .setSmallIcon(android.R.drawable.ic_dialog_info)
//                .setContentTitle("Gym Nearby")
//                .setContentText("There's a gym nearby! Time for a workout?")
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
//
//        // notificationId is a unique int for each notification that you must define
//        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        notificationManager.notify(1, builder.build());
//    }
    private void showNotification() {
//        button = findViewById(R.id.btnNotifications);
//        button.setOnClickListener(new View.OnClickListener(){
//          @Override
//            public void onClick(View v){
//            showNotification();
//          }
//        });

        String chanelID = "GYM_NOTIFICATION";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), chanelID);
        builder.setSmallIcon(R.drawable.baseline_notifications)
        .setContentTitle("Gym Motivator")
        .setContentText("Do you plan to go to the gym today?")
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Intent intent = new Intent(getApplicationContext(), GymNotifActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("data", "Parameters");
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    manager.getNotificationChannel(chanelID);
            if (notificationChannel == null){
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(  chanelID, "Description", importance);

                notificationChannel.setLightColor(Color.BLUE);
                notificationChannel.enableVibration(true);
                manager.createNotificationChannel(notificationChannel);
            }
        }
        manager.notify(0, builder.build());

    }
}
