package com.example.myapplication.sleep;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class SleepAPIDetection {
    // SleepDetectionService.java
    private Context context;
    private static final String TAG = "SleepAPIDetection";

    private ActivityRecognitionClient activityRecognitionClient;
    private static final long DETECTION_INTERVAL_IN_MILLISECONDS = 10*60*1000;

    public SleepAPIDetection(Context context) {
            this.context = context;
            this.activityRecognitionClient = ActivityRecognition.getClient(context);
        }

        public void subscribeToSleepUpdates() {
            Log.d(TAG, "subscribeToSleepUpdates: ");
            Task<Void> task = activityRecognitionClient.requestActivityUpdates(
                    DETECTION_INTERVAL_IN_MILLISECONDS,
                    getPendingIntent());

            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Successfully requested activity updates
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Failed to request activity updates
                }
            });
        }

        private PendingIntent getPendingIntent() {
            Intent intent = new Intent(context, SleepAPIDetection.class);
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }


