package com.example.myapplication.ui.notifications;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class GymNotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "ExerciseRecommendation";

    private static int NOTIFICATION_ID = 1002;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("YES_ACTION".equals(action)) {
            updateFirestore(true);
        } else if ("NO_ACTION".equals(action)) {
            updateFirestore(false);
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void updateFirestore(boolean userAgreed) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        if(user == null)    return;

        DocumentReference userDocRef = firestore.collection("users").document(user.getUid());
        userDocRef.update("exercisedToday", userAgreed)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "exercisedToday field set on Firestore: " + userAgreed);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error setting exercisedToday field", e);
                    }
                });

        int date = Calendar.getInstance().get(Calendar.DATE);
        userDocRef.update("exerciseHistory." + date, userAgreed)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Exercise history record pushed to Firestore: " + userAgreed);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to push exercise history record to Firestore", e);
                    }
                });
    }
}
