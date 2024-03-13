package com.example.myapplication.sleep;

import com.google.android.gms.location.SleepClassifyEvent;
import com.google.android.gms.location.SleepSegmentEvent;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SleepDataHandler {

    private FirebaseFirestore db;

    public SleepDataHandler() {
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
    }

    public void addSleepSegmentEventsToDatabase(List<SleepSegmentEvent> sleepSegmentEvents) {
        // Iterate through sleep segment events and add them to Firestore
        for (SleepSegmentEvent event : sleepSegmentEvents) {
            // Create a Map to represent the data
            Map<String, Object> data = new HashMap<>();
            data.put("startTimeMillis", event.getStartTimeMillis());
            data.put("endTimeMillis", event.getEndTimeMillis());
            data.put("segmentDurationMillis", event.getSegmentDurationMillis());
            data.put("status", event.getStatus());

            // Add data to Firestore
            db.collection("your-collection")
                    .add(data)
                    .addOnSuccessListener(documentReference -> {
                        // Handle success if needed
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure if needed
                    });
        }
    }

    public void addSleepClassifyEventsToDatabase(List<SleepClassifyEvent> sleepClassifyEvents) {
        // Iterate through sleep classify events and add them to Firestore
        for (SleepClassifyEvent event : sleepClassifyEvents) {
            // Create a Map to represent the data
            Map<String, Object> data = new HashMap<>();
            // Add necessary fields to the data Map

            // Add data to Firestore
            db.collection("sleep-data")
                    .add(data)
                    .addOnSuccessListener(documentReference -> {
                        // Handle success if needed
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure if needed
                    });
        }
    }
}
