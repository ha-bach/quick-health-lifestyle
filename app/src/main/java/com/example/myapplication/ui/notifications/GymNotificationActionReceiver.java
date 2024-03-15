package com.example.myapplication.ui.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("YES_ACTION".equals(action)) {
            Log.d("NotificationAction", "Yes button clicked");
            // Handle Yes action
        } else if ("NO_ACTION".equals(action)) {
            Log.d("NotificationAction", "No button clicked");
            // Handle No action
        }
    }
}
