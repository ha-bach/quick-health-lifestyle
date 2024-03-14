package com.example.myapplication.sleep;

import com.google.android.gms.location.SleepClassifyEvent;
import com.google.android.gms.location.SleepSegmentEvent;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SleepDataHandler {

    private final FirebaseFirestore db;

    public SleepDataHandler() {
        db = FirebaseFirestore.getInstance();
    }

    public void addSleepSegmentEventsToDatabase(List<SleepSegmentEvent> sleepSegmentEvents) {
        for (SleepSegmentEvent event : sleepSegmentEvents) {
            Map<String, Object> data = new HashMap<>();
            data.put("startTimeMillis", event.getStartTimeMillis());
            data.put("endTimeMillis", event.getEndTimeMillis());
            data.put("segmentDurationMillis", event.getSegmentDurationMillis());

            // Add data to Firestore
            db.collection("users")
                    .add(data)
                    .addOnSuccessListener(documentReference -> {
                        // Handle success if needed
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure if needed
                    });
        }
    }

    public void addSleepClassifyEventsToDatabase(List<SleepSegmentEvent> sleepSegmentEvents) {
        for (SleepSegmentEvent event : sleepSegmentEvents) {
            Map<String, Object> data = new HashMap<>();

            db.collection("sleep-data")
                    .add(data);

        }
    }
}
