package com.example.myapplication.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class HydrationService extends Service {

    private final IBinder binder = new HydrationBinder();

    public class HydrationBinder extends Binder {
        public HydrationService getService() {
            return HydrationService.this;
        }
    }

    public void onCreate() {
        super.onCreate();
    }

    public double getHydrationRecommendation() {
        // based on metric units.
        double age = 35;
        String sex = "male";
        double weight = 65; // kg
        double temperature = 25; // Celsius
        double humidity = 40; // percent

        double personalIntake = getPersonalIntake(age, sex, weight);
        double contextualIntake = getContextualIntake(temperature, humidity);

        return personalIntake + contextualIntake;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    double getPersonalIntake(double age, String sex, double weight) {
        double intakePerKg = 35;
        double personalIntakeFactor = 1;
        if(age <= 18) {
            if(age == 0) personalIntakeFactor += 1;
            else personalIntakeFactor += 1/age;
        }
        else if(age < 60) {
            if(sex.equalsIgnoreCase("male"))    personalIntakeFactor += 0.5;
        }
        else {
            personalIntakeFactor -= 0.1;
            if(sex.equalsIgnoreCase("male")) personalIntakeFactor += 0.5;
        }
        return intakePerKg * personalIntakeFactor * weight;
    }

    double getContextualIntake(double humidity, double temperature) {
        double humidityIntake = ((humidity - 60) / 40) * 500;
        double temperatureIntake = (temperature - 30) * 50;
        return humidityIntake + temperatureIntake;
    }
}
