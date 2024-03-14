package com.example.myapplication.services;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.myapplication.R;
import com.example.myapplication.ui.notifications.HydrationNotifActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HydrationService extends Service {

    private final IBinder binder = new HydrationBinder();
    private final String TAG = "HydrationService";
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore firestore;
    double recommendedIntakeInCups;

    public class HydrationBinder extends Binder {
        public HydrationService getService() {
            return HydrationService.this;
        }
    }

    public void onCreate() {
        super.onCreate();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
    }

    public CompletableFuture<Double> getHydrationRecommendation() {
        CompletableFuture<Double> contextualIntake = getContextualIntake();
        CompletableFuture<Double> personalIntake = getPersonalIntake();
        showNotification();
        return contextualIntake.thenCombine(personalIntake, (contextual, personal) -> {
            recommendedIntakeInCups = (contextual + personal) / 236.588;
            return recommendedIntakeInCups;
        });
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if ("HYDRATION_YES_ACTION".equals(action)) {
                Log.d(TAG, "User clicked Yes on notification");
                handleYesAction();
            } else if ("HYDRATION_NO_ACTION".equals(action)) {
                Log.d(TAG, "User clicked No on notification");
                handleNoAction();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private CompletableFuture<Double> getPersonalIntake() {
        CompletableFuture<Double> future = new CompletableFuture<>();

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
        double intakePerKg = 40;
        double personalIntakeFactor = 1;
        if(age <= 18) {
            if(age == 0) personalIntakeFactor += 1;
            else personalIntakeFactor += 1/age;
        }
        else if(age < 60) {
            if(sex.equalsIgnoreCase("male"))    personalIntakeFactor += 0.3;
        }
        else {
            personalIntakeFactor -= 0.1;
            if(sex.equalsIgnoreCase("male")) personalIntakeFactor += 0.3;
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
        double humidityIntake = ((humidity - 60) / 40) * 500;
        if (humidityIntake < 0) {
            humidityIntake = 0;
        }
        double temperatureIntake = (temperature - 30) * 50;
        if (temperatureIntake < 0) {
            temperatureIntake = 0;
        }
        return humidityIntake + temperatureIntake;
    }

//    @SuppressLint("MissingPermission")
//    private void showNotification() {
//        String chanelID = "HYDRATION_NOTIFICATION";
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), chanelID);
//        builder.setSmallIcon(R.drawable.baseline_notifications)
//                .setContentTitle("Quick Health Manager")
//                .setContentText("Have you drank the recommended amount of water today?")
//                .setAutoCancel(true)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//        Intent intent = new Intent(getApplicationContext(), HydrationNotifActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("data", "Parameters");
//        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
//                0, intent, PendingIntent.FLAG_MUTABLE);
//        builder.setContentIntent(pendingIntent);
//        NotificationManager manager = (NotificationManager)
//                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//        Intent yesIntent = new Intent(getApplicationContext(),
//                HydrationNotificationReceiver.class);
//        yesIntent.setAction("HYDRATION_YES_ACTION");
//        PendingIntent yesPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
//                0, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//        Intent noIntent = new Intent(getApplicationContext(),
//                HydrationNotificationReceiver.class);
//        noIntent.setAction("HYDRATION_NO_ACTION");
//        PendingIntent noPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, noIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel notificationChannel =
//                    manager.getNotificationChannel(chanelID);
//            if (notificationChannel == null){
//                int importance = NotificationManager.IMPORTANCE_HIGH;
//                notificationChannel = new NotificationChannel(  chanelID, "Description", importance);
//
//                notificationChannel.setLightColor(Color.BLUE);
//                notificationChannel.enableVibration(true);
//                manager.createNotificationChannel(notificationChannel);
//            }
//        }
//        builder.addAction(R.drawable.baseline_notifications, "Yes", yesPendingIntent);
//        builder.addAction(R.drawable.ic_account_circle_black_24dp, "No", noPendingIntent);
//
//        manager.notify(0, builder.build());
//
////        String channelId = "HYDRATION_NOTIFICATION";
////        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
////        builder.setSmallIcon(R.drawable.baseline_notifications)
////                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
////
////        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.hydration_notification);
////        remoteViews.setTextViewText(R.id.notification_title, "Notification Title");
////        remoteViews.setTextViewText(R.id.notification_message, "Notification Message");
////
////        Intent button1Intent = new Intent(this, NotificationReceiver.class);
////        button1Intent.setAction("BUTTON_1_ACTION");
////        PendingIntent button1PendingIntent = PendingIntent.getBroadcast(this, 0, button1Intent, PendingIntent.FLAG_UPDATE_CURRENT);
////        remoteViews.setOnClickPendingIntent(R.id.button1, button1PendingIntent);
////
////        Intent button2Intent = new Intent(this, NotificationReceiver.class);
////        button2Intent.setAction("BUTTON_2_ACTION");
////        PendingIntent button2PendingIntent = PendingIntent.getBroadcast(this, 0, button2Intent, PendingIntent.FLAG_UPDATE_CURRENT);
////        remoteViews.setOnClickPendingIntent(R.id.button2, button2PendingIntent);
////
////        builder.setCustomContentView(remoteViews);
////
////        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
////        notificationManager.notify(1, builder.build());
//    }

        @SuppressLint("MissingPermission")
        public void showNotification() {
        String channelId = "HYDRATION_NOTIFICATION";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.drawable.baseline_notifications)
                .setContentTitle("Quick Health Manager")
                .setContentText("Have you drank the recommended amount of water today?")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(getApplicationContext(), HydrationNotifActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("data", "Parameters");
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        Intent yesIntent = new Intent(getApplicationContext(), HydrationService.class);
        yesIntent.setAction("HYDRATION_YES_ACTION");
        PendingIntent yesPendingIntent = PendingIntent.getService(getApplicationContext(),
                0, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent noIntent = new Intent(getApplicationContext(), HydrationService.class);
        noIntent.setAction("HYDRATION_NO_ACTION");
        PendingIntent noPendingIntent = PendingIntent.getService(getApplicationContext(),
                1, noIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager)
                    getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel =
                    manager.getNotificationChannel(channelId);
            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelId, "Description", importance);

                notificationChannel.setLightColor(Color.BLUE);
                notificationChannel.enableVibration(true);
                manager.createNotificationChannel(notificationChannel);
            }
        }

        builder.addAction(R.drawable.baseline_notifications, "Yes", yesPendingIntent);
        builder.addAction(R.drawable.ic_account_circle_black_24dp, "No", noPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());
    }

    private void handleYesAction() {
        updateFirestoreHydrationHistory(true);
    }

    private void handleNoAction() {
        updateFirestoreHydrationHistory(false);
    }

    private void updateFirestoreHydrationHistory(boolean recommendationFulfilled) {
        if(user == null)    {
            Log.e(TAG, "Cannot retrieve current user. Cancelled pushing hydration record to Firestore.");
            return;
        }
        DocumentReference userDocRef = firestore.collection("users").document(user.getUid());

        userDocRef.update("hydrationHistory", FieldValue.arrayUnion(recommendationFulfilled ? recommendedIntakeInCups : 0))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Hydration history record pushed to Firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to push hydration history record to Firestore", e);
                    }
                });
    }
}
