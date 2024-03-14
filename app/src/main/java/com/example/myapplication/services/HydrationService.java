package com.example.myapplication.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.work.ListenableWorker;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.concurrent.CompletableFuture;

public class HydrationService extends Service {

    private final IBinder binder = new HydrationBinder();
    private final String TAG = "HydrationService";

    double temperature;
    double humidity;

    public class HydrationBinder extends Binder {
        public HydrationService getService() {
            return HydrationService.this;
        }
    }

    public void onCreate() {
        super.onCreate();
    }

    public CompletableFuture<Double> getHydrationRecommendationAsync() {
        CompletableFuture<Double> future = new CompletableFuture<>();

        // getting contextual weather data from Firebase
        String userId = "YnfZYzNM1OqMnXgqyA6D";
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firestore.collection("users")
                .document(userId)
                .collection("weatherData");
        Query query = collectionRef.orderBy("time", Query.Direction.DESCENDING).limit(1);
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        if (documentSnapshot.exists()) {
                            double temperature = documentSnapshot.getDouble("temperature");
                            double humidity = documentSnapshot.getDouble("humidity");
                            Log.d(TAG, "Weather data retrieved. Humidity: " + humidity + ". Temperature: " + temperature);

                            double age = 35;
                            String sex = "male";
                            double weight = 65; // kg
                            double personalIntake = getPersonalIntake(age, sex, weight);
                            double contextualIntake = getContextualIntake(temperature, humidity);

                            Log.d(TAG, "Intake calculated. Personal: " + personalIntake + ". Contextual: " + contextualIntake);

                            double totalIntake = personalIntake + contextualIntake;
                            future.complete(totalIntake);
                        } else {
                            Log.d(TAG, "Document snapshot does not exist.");
                            future.completeExceptionally(new RuntimeException("Document snapshot does not exist"));
                        }
                    } else {
                        Log.d(TAG, "Document snapshots are empty.");
                        future.completeExceptionally(new RuntimeException("Document snapshots are empty"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Failed to retrieve weather from Firestore: " + e.getMessage());
                    e.printStackTrace();
                    future.completeExceptionally(e);
                });

        return future;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    double getPersonalIntake(double age, String sex, double weight) {
        double intakePerKg = 35;
        double personalIntakeFactor = 1;
        if(age <= 18) {
            if(age == 0) personalIntakeFactor += 1;
            else personalIntakeFactor += 1/age;
        }
        else if(age < 60) {
            if(sex.equalsIgnoreCase("male"))    personalIntakeFactor += 0.5;
        }
        else {
            personalIntakeFactor -= 0.1;
            if(sex.equalsIgnoreCase("male")) personalIntakeFactor += 0.5;
        }
        return intakePerKg * personalIntakeFactor * weight;
    }

    double getContextualIntake(double humidity, double temperature) {
        double humidityIntake = ((humidity - 60) / 40) * 500;
        double temperatureIntake = (temperature - 30) * 50;
        return humidityIntake + temperatureIntake;
    }
}
