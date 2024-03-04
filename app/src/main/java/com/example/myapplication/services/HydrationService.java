package com.example.myapplication.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class HydrationService extends Service {

    private final IBinder binder = new HydrationBinder();
    private static final double BASE_INTAKE = 2500;
    private static final double MALE_INTAKE_FACTOR = 1.1;
    private static final double FEMALE_INTAKE_FACTOR = 1.0;
    private static final double HUMIDITY_INTAKE_FACTOR = 0.05;
    private static final double TEMPERATURE_INTAKE_FACTOR = 0.1;


    public class HydrationBinder extends Binder {
        HydrationService getService() {
            return HydrationService.this;
        }
    }

    public void onCreate() {
        super.onCreate();
        // add Firebase listener code here: https://chat.openai.com/c/4332d77b-c237-423d-b014-803f7dd99f35
    }

    private double calculateHydrationRecommendation() {
        double intake;

        // TODO: replace with Firebase code
        double age = 35;
        String sex = "female";
        double weight = 65;
        double temperature = 25;
        double humidity = 40;

        double ageFactor = 1.0 + (age - 18) * 0.02;
        double weightFactor = 1.0 + (weight - 70) * 0.01;
        double sexFactor = 1;
        if (sex.equalsIgnoreCase("male")) {
            sexFactor = MALE_INTAKE_FACTOR;
        } else if (sex.equalsIgnoreCase("female")) {
            sexFactor = FEMALE_INTAKE_FACTOR;
        }
        double temperatureFactor = TEMPERATURE_INTAKE_FACTOR * ((temperature - 25) / 10);
        double humidityFactor = HUMIDITY_INTAKE_FACTOR * (humidity / 100);

        intake = BASE_INTAKE * ageFactor * weightFactor * sexFactor * temperatureFactor * humidityFactor;

        return intake;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
