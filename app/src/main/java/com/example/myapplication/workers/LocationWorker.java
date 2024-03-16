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

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myapplication.R;
import com.example.myapplication.ui.notifications.GymNotifActivity;
import com.example.myapplication.ui.notifications.GymNotificationActionReceiver;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class LocationWorker extends Worker {

    private static final String TAG = "LocationWorker";
    Location location;
    FirebaseAuth auth;
    FirebaseUser user;
    private final int NOTIFICATION_ID = 1002;

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

        if(user == null)    return;
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
        if(user == null) return;
        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("preferredGymTime")) {
                String preferredGymTime = documentSnapshot.getString("preferredGymTime");
                Log.d(TAG, "Preferred Gym Time: " + preferredGymTime);
                scheduleGymNotification(convertHourStringToInt(preferredGymTime));
                // call showNotification() for immediate notification
            } else {
                Log.e(TAG, "No preferred gym time found for user.");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching preferred gym time", e));

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
                        for (int i = 0; i < 5 && i < results.length(); i++) {
                            JSONObject gym = results.getJSONObject(i);
                            String gymName = gym.getString("name");
                            Log.i(TAG, "Found2 gym: " + gymName);
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

    private void showNotification() {
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
        Intent yesIntent = new Intent(getApplicationContext(),
                GymNotificationActionReceiver.class);
        yesIntent.setAction("YES_ACTION");
        PendingIntent yesPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                1, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Intent noIntent = new Intent(getApplicationContext(),
                GymNotificationActionReceiver.class);
        noIntent.setAction("NO_ACTION");
        PendingIntent noPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, noIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
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
        builder.addAction(R.drawable.baseline_notifications, "Yes", yesPendingIntent);
        builder.addAction(R.drawable.ic_account_circle_black_24dp, "No", noPendingIntent);

        manager.notify(NOTIFICATION_ID, builder.build());
    }

    private void scheduleGymNotification(int scheduledHour) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if(hour == scheduledHour)
            showNotification();
    }

    public int convertHourStringToInt(String hourString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            Date date = dateFormat.parse(hourString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            return hourOfDay;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
