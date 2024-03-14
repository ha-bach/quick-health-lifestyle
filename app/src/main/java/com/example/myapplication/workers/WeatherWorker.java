package com.example.myapplication.workers;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class WeatherWorker extends Worker {

    private static final String TAG = "WeatherWorker";
    FirebaseAuth auth;
    FirebaseUser user;

    public WeatherWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    @NonNull
    @Override
    public Result doWork() {
        if(user == null) return Result.failure();
        String userId = user.getUid();
        Log.d(TAG, "User id: " + userId);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firestore.collection("users")
                .document(userId)
                .collection("locationHistory");
        Query query = collectionRef.orderBy("time", Query.Direction.DESCENDING).limit(1);
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        if (documentSnapshot.exists()) {
                            Location location = new Location("gps");
                            location.setLatitude(documentSnapshot.getDouble("latitude"));
                            location.setLongitude(documentSnapshot.getDouble("longitude"));
                            location.setTime(documentSnapshot.getLong("time"));
                            Log.d(TAG, "Location retrieved: " + location.getLatitude() + ", " + location.getLongitude());
                            getWeatherData(location);
                        } else {
                            Log.d(TAG, "Document snapshot does not exist.");
                        }
                    } else {
                        Log.d(TAG, "Document snapshots are empty.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Failed to retrieve weather from Firestore: " + e.getMessage());
                });

        return Result.success();
    }

    private void getWeatherData(Location location) {
        new Thread(() -> {
            try {
                String apiKey = "4da53a4d9ca0b4c227c5d4be7b6f5995";
                String apiUrl = "https://api.openweathermap.org/data/2.5/weather?" +
                        "lat=" + location.getLatitude() +
                        "&lon=" + location.getLongitude() +
                        "&units=" + "metric" +
                        "&appid=" + apiKey;
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    Gson gson = new Gson();
                    JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                    JsonObject mainWeather = jsonResponse.getAsJsonObject("main");
                    double humidity = mainWeather.get("humidity").getAsDouble();
                    double temp = mainWeather.get("temp").getAsDouble();
                    Log.d(TAG, "Weather API called. Humidity: " + humidity + ". Temperature: " + temp);

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    if(user == null)    return;
                    String userId = user.getUid();
                    CollectionReference docRef = firestore.collection("users")
                            .document(userId)
                            .collection("weatherData");

                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                    String docId = formatter.format(location.getTime());
                    Map<String, Object> locationPost = new HashMap<>();
                    locationPost.put("humidity", humidity);
                    locationPost.put("temperature", temp);
                    locationPost.put("time", location.getTime());

                    docRef.document(docId)
                            .set(locationPost)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Weather data pushed to Firestore with ID: " + docId);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Weather pushing location data to Firestore", e);
                                }
                            });

                } else {
                    Log.e(TAG, "HTTP response code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching weather data: " + e.getMessage());
            }
        }).start();
    }
}