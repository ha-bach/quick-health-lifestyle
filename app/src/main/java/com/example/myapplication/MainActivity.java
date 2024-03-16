package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.os.IBinder;
import android.util.Log;
import android.Manifest;

import com.example.myapplication.services.HydrationService;
import com.example.myapplication.sleep.SleepRecommender;
import com.example.myapplication.ui.notifications.SleepNotifActivity;
import com.example.myapplication.workers.LocationWorker;
import com.example.myapplication.workers.WeatherWorker;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;

    private HydrationService hydrationService;
    private boolean hydrationBound = false;
    private static final int REQUEST_CODE_LOCATION = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
        }
        else
            getLocationUpdates();

        startWeatherWorker();
        scheduleSleepNotification(11);
        // call startSleepNotification() for instant notification
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, HydrationService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (hydrationBound) {
            unbindService(connection);
            hydrationBound = false;
        }
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d("MainActivity", "Hydration Service connected.");
            HydrationService.HydrationBinder hydrationBinder = (HydrationService.HydrationBinder) service;
            hydrationService = hydrationBinder.getService();
            hydrationBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            hydrationBound = false;
        }
    };

    private void getLocationUpdates() {
        long repeatIntervalMinutes = 15;
        PeriodicWorkRequest locationWorkRequest = new PeriodicWorkRequest.Builder(LocationWorker.class, repeatIntervalMinutes, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(getApplicationContext())
                .enqueue(locationWorkRequest);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Location permission denied.");
            }
            else {
                getLocationUpdates();
            }
        }
    }

    public void startWeatherWorker() {
        long repeatIntervalHours = 4;
        PeriodicWorkRequest weatherWorkRequest = new PeriodicWorkRequest.Builder(WeatherWorker.class, repeatIntervalHours, TimeUnit.HOURS)
                .build();
        WorkManager.getInstance(getApplicationContext())
                .enqueue(weatherWorkRequest);
    }

    @SuppressLint("MissingPermission")
    public void showSleepNotification(double recommendedSleepDuration) {
        int notificationId = 1003;
        String channelId = "SLEEP_NOTIFICATION";

        String message = "We recommend sleeping " + recommendedSleepDuration + " hours tonight.";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.drawable.baseline_notifications)
                .setContentTitle("Quick Health Manager")
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(getApplicationContext(), SleepNotifActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("data", "Parameters");
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                4, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager)
                    getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel =
                    manager.getNotificationChannel(channelId);
            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                notificationChannel = new NotificationChannel(channelId, "Description", importance);

                notificationChannel.setLightColor(Color.BLUE);
                notificationChannel.enableVibration(true);
                manager.createNotificationChannel(notificationChannel);
            }
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());
    }

    private CompletableFuture<Double> getUserAge() {
        if(user == null)    return null;

        CompletableFuture<Double> future = new CompletableFuture<>();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference userDoc = firestore.collection("users").document(user.getUid());

        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Double age = documentSnapshot.getDouble("age");
                future.complete(age);
            }
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error getting user age: " + e.getMessage());
        });
        return future;
    }

    private CompletableFuture<Double> getUserLastSleepDuration() {
        if(user == null)    return null;

        CompletableFuture<Double> future = new CompletableFuture<>();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = firestore.collection("users").document(user.getUid());

        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> sleepHistory = (List<Map<String, Object>>) documentSnapshot.get("sleepHistory");
                if (sleepHistory != null && !sleepHistory.isEmpty()) {
                    Map<String, Object> firstEntry = sleepHistory.get(0);
                    future.complete((Double) firstEntry.get("sleepDuration"));
                } else {
                    Log.d("Firestore", "Sleep history array is empty");
                }
            } else {
                Log.d("Firestore", "User document does not exist");
            }
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error getting user document: " + e.getMessage());
        });

        return future;
    }

    private void startSleepNotification() {
        CompletableFuture<Double> lastSleepDurationFuture = getUserLastSleepDuration();
        CompletableFuture<Double> ageFuture = getUserAge();

        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(lastSleepDurationFuture, ageFuture);

        combinedFuture.thenRun(() -> {
            try {
                Double lastSleepDuration = lastSleepDurationFuture.get();
                Double age = ageFuture.get();
                SleepRecommender sleepRecommender = new SleepRecommender(age.intValue(), lastSleepDuration.intValue());
                int recommendedSleep = sleepRecommender.sleepAmountRecommender();

                showSleepNotification(recommendedSleep);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void scheduleSleepNotification(int scheduledHour) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if(hour == scheduledHour)
            startSleepNotification();
    }
}