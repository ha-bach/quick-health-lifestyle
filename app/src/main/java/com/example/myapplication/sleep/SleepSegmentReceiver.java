package com.example.myapplication.sleep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;

public class SleepSegmentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            // Access sleep segment data from 'result'
            // 'result.getSleepSegmentDurations()' provides sleep segment durations
            // Process the sleep data as needed
        }
    }
}
