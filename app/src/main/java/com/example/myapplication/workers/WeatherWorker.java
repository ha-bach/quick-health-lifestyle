package com.example.myapplication.workers;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;

import kotlinx.coroutines.DelicateCoroutinesApi;
import kotlinx.coroutines.GlobalScope;

public class WeatherWorker extends Worker {

    private static final String TAG = "WeatherWorker";

    Location location;

    public WeatherWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String userId = "YnfZYzNM1OqMnXgqyA6D";
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
                    Log.d(TAG, "Failed to retrieve location from Firestore: " + e.getMessage());
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
                    String userId = "YnfZYzNM1OqMnXgqyA6D";  // Replace with the actual user ID
                    CollectionReference weatherRef = firestore.collection("users")
                            .document(userId)
                            .collection("weatherData");

                    // TODO: push weather data to Firebase

                } else {
                    Log.e(TAG, "HTTP response code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching weather data: " + e.getMessage());
            }
        }).start();
    }
}