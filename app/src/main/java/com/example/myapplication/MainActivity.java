package com.example.myapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.example.myapplication.services.HydrationService;
import com.example.myapplication.workers.LocationWorker;
import com.example.myapplication.workers.WeatherWorker;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.myapplication.databinding.ActivityMainBinding;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

private ActivityMainBinding binding;

    //FirebaseFirestore firestore; //Testing firebase instance uncomment for testing
    private HydrationService hydrationService;
    private boolean hydrationBound = false;
    private static final int REQUEST_CODE_LOCATION = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Testing Firestore: if you want to test it, uncomment this below
//        firestore = FirebaseFirestore.getInstance();
//        Map<String, Object> userProfile = new HashMap<>();
//        userProfile.put("FirstName", "Ha");
//        userProfile.put("Last", "K");
//        userProfile.put("Height", "5'5");
//
//        firestore.collection("users").add(userProfile).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//            @Override
//            public void onSuccess(DocumentReference documentReference) {
//                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getApplicationContext(), "Failure", Toast.LENGTH_LONG).show();
//            }
//        });
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
//        }
//        else
//          start

        startWeatherWorker();
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
//            TextView textView = findViewById(R.id.text_home);
//            textView.setText(String.format(Locale.US, "%.2f",hydrationService.getHydrationRecommendation()));
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            hydrationBound = false;
        }
    };

//    void getLocationUpdates() {
//        long repeatIntervalMinutes = 15;
//        PeriodicWorkRequest locationWorkRequest = new PeriodicWorkRequest.Builder(LocationWorker.class, repeatIntervalMinutes, TimeUnit.MINUTES)
//                .build();
//        WorkManager.getInstance(getApplicationContext())
//                .enqueue(locationWorkRequest);
//    }
//
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == REQUEST_CODE_LOCATION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Log.d("MainActivity", "Location permission denied.");
//            }
//            else {
//                getLocationUpdates();
//            }
//        }
//    }

    public void startWeatherWorker() {
        long repeatIntervalHours = 4;
        PeriodicWorkRequest weatherWorkRequest = new PeriodicWorkRequest.Builder(WeatherWorker.class, repeatIntervalHours, TimeUnit.HOURS)
                .build();
        WorkManager.getInstance(getApplicationContext())
                .enqueue(weatherWorkRequest);
    }
}

