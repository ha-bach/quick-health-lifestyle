package com.example.myapplication.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class TemperatureSensor {

    private final SensorManager sensorManager;
    private Sensor temperatureSensor;
    private float lastTemperature;
    private int sensorAccuracy;

    private static final String TAG = "TemperatureSensor";

    public TemperatureSensor(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            if (temperatureSensor == null) {
                Log.e(TAG, "No ambient temperature sensor detected.");
            }
        }
    }

    public boolean isTemperatureSensorAvailable() {
        return temperatureSensor != null;
    }

    public void startTemperatureUpdates() {
        if (isTemperatureSensorAvailable()) {
            sensorManager.registerListener(sensorEventListener, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void stopTemperatureUpdates() {
        if (isTemperatureSensorAvailable()) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    public float getLastTemperature() {
        return lastTemperature;
    }

    public int getSensorAccuracy() {
        return sensorAccuracy;
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                lastTemperature = event.values[0];
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            sensorAccuracy = accuracy;
        }
    };
}