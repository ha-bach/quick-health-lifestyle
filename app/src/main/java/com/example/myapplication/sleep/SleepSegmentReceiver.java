package com.example.myapplication.sleep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.SleepSegmentEvent;

public class SleepSegmentReceiver extends BroadcastReceiver {
    private static final String TAG = "SleepSegmentReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Handle sleep segment events here
        // Extract sleep segment data from the intent
        if ("com.example.myapplication.sleep.ACTION_SLEEP_SEGMENT_EVENT".equals(intent.getAction())) {
            SleepSegmentEvent segmentEvent = intent.getParcelableExtra("com.example.myapplication.sleep.EXTRA_SLEEP_SEGMENT_EVENT");

            if (segmentEvent != null) {
                // Access sleep segment data
                long startTimeMillis = segmentEvent.getStartTimeMillis();
                long endTimeMillis = segmentEvent.getEndTimeMillis();
                int status = segmentEvent.getStatus();
                long segmentDurationMillis = segmentEvent.getSegmentDurationMillis();

                // Log the sleep segment data
                Log.d(TAG, "Sleep Segment Data:\n" +
                        "Start Time: " + startTimeMillis + "\n" +
                        "End Time: " + endTimeMillis + "\n" +
                        "Status: " + status + "\n" +
                        "Segment Duration: " + segmentDurationMillis);
            }
        }
    }
}
