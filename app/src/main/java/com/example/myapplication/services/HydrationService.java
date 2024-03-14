package com.example.myapplication.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.concurrent.CompletableFuture;

public class HydrationService extends Service {

    private final IBinder binder = new HydrationBinder();
    private final String TAG = "HydrationService";
    FirebaseAuth auth;
    FirebaseUser user;

    public class HydrationBinder extends Binder {
        public HydrationService getService() {
            return HydrationService.this;
        }
    }

    public void onCreate() {
        super.onCreate();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    public CompletableFuture<Double> getHydrationRecommendation() {
        CompletableFuture<Double> contextualIntake = getContextualIntake();
        CompletableFuture<Double> personalIntake = getPersonalIntake();
        return contextualIntake.thenCombine(personalIntake, (contextual, personal) -> (contextual + personal) / 236.588); // convert from mL to cups
    }

    public CompletableFuture<Double> getContextualIntake() {
        CompletableFuture<Double> future = new CompletableFuture<>();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firestore.collection("users")
                .document(user.getUid())
                .collection("weatherData");
        Query query = collectionRef.orderBy("time", Query.Direction.DESCENDING).limit(1);
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        if (documentSnapshot.exists()) {
                            double temperature = documentSnapshot.getDouble("temperature");
                            double humidity = documentSnapshot.getDouble("humidity");
                            Log.d(TAG, "Temperature: " + temperature + ". Humidity: " + humidity);
                            double contextualIntake = getWeatherIntake(temperature, humidity);
                            Log.d(TAG, "Contextual intake calculated: " + contextualIntake);
                            future.complete(contextualIntake);
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

    private CompletableFuture<Double> getPersonalIntake() {
        CompletableFuture<Double> future = new CompletableFuture<>();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = firestore.collection("users").document(user.getUid());
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    double age = documentSnapshot.getDouble("age");
                    String sex = documentSnapshot.getString("sex");
                    double weightInLbs = documentSnapshot.getDouble("weight");
                    double weightInKg = weightInLbs * 0.453592;
                    double personalIntake = calculatePersonalIntake(age, sex, weightInKg);
                    Log.d(TAG, "Personal intake calculated: " + personalIntake);
                    future.complete(personalIntake);
                } else {
                    Log.d(TAG, "User document does not exist");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error getting user document", e);
            }
        });

        return future;
    }

    private double calculatePersonalIntake(double age, String sex, double weight) {
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    double getWeatherIntake(double humidity, double temperature) {
        // add intake if too hot or humid
        double humidityIntake = ((humidity - 30) / 40) * 500;
        if (humidityIntake < 0) {
            humidityIntake = 0;
        }
        double temperatureIntake = (temperature - 20) * 50;
        if (temperatureIntake < 0) {
            temperatureIntake = 0;
        }
        return humidityIntake + temperatureIntake;
    }
}
