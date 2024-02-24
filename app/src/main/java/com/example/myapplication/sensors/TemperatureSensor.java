package com.example.myapplication.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class TemperatureSensor implements SensorEventListener{

    private final SensorManager sensorManager;
    private Sensor sensor;
    private float temperature = Float.NaN;
    private int sensorAccuracy = -1;

    private static final String TAG = "TemperatureSensor";

    public TemperatureSensor(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            if (sensor == null) {
                Log.e(TAG, "No ambient temperature sensor detected.");
            }
        }
    }

    public boolean isSensorAvailable() {
        return sensor != null;
    }

    public float getTemperature() {
        return temperature;
    }

    public int getSensorAccuracy() {
        return sensorAccuracy;
    }

    public void onSensorChanged(SensorEvent event) {
            temperature = event.values[0];
    };

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        sensorAccuracy = accuracy;
    }
}