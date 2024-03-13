package com.example.myapplication.sleep;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.SleepSegmentEvent;
import com.google.android.gms.location.SleepSegmentRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class SleepAPIDetection {
    private final Context context;
    private static final String TAG = "SleepAPIDetection";

    private final ActivityRecognitionClient activityRecognitionClient;
    private static final long DETECTION_INTERVAL_IN_MILLISECONDS = 10 * 60 * 1000;

    private final SleepDataHandler sleepdatahandler = new SleepDataHandler();
    public SleepAPIDetection(Context context) {
        this.context = context;
        this.activityRecognitionClient = ActivityRecognition.getClient(context);
    }

    public void subscribeToSleepAndActivityUpdates() {
        Log.d(TAG, "subscribeToSleepAndActivityUpdates: ");
        @SuppressLint("MissingPermission") Task<Void> task = activityRecognitionClient.requestSleepSegmentUpdates(
                getPendingIntent(),
                SleepSegmentRequest.getDefaultSleepSegmentRequest());

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Successfully requested sleep segment updates
                Log.d(TAG, "Successfully subscribed to sleep data.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to request sleep segment updates
                Log.e(TAG, "Failed to subscribe to sleep data: " + e.getMessage());
            }
        });

        // Add activity recognition subscription logic here
        subscribeToActivityUpdates();
    }

    private void subscribeToActivityUpdates() {
        // Implement your activity recognition subscription logic here
        // You can use ActivityRecognitionClient similar to how it's done for sleep data
        // Don't forget to handle success and failure cases accordingly
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(context, SleepAPIDetection.class);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    public void simulateSleepTrigger() {
        // Simulate a trigger for sleep detection
        Intent intent = new Intent(context, SleepSegmentReceiver.class);
        intent.setAction("com.example.myapplication.sleep.ACTION_SLEEP_SEGMENT_EVENT");// TODO: Extract sleep information from PendingIntent.
        if (SleepSegmentEvent.hasEvents(intent)) {
            List<SleepSegmentEvent> sleepSegmentEvents = SleepSegmentEvent.extractEvents(intent);
            sleepdatahandler.addSleepSegmentEventsToDatabase(sleepSegmentEvents);
        } else {
            // Handle other sleep events if needed
        }



                    context.sendBroadcast(intent);
    }


}

