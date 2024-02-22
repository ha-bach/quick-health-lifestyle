package com.example.myapplication.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class HumiditySensor implements SensorEventListener {
    private final String TAG = "HumiditySensor";
    private final SensorManager sensorManager;
    private Sensor sensor;
    private int sensorAccuracy = -1;
    private float humidity = Float.NaN;

    public HumiditySensor(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
            if (sensor == null) {
                Log.e(TAG, "No ambient temperature sensor detected.");
            }
        }
    }

    public boolean isSensorAvailable() {
        return sensor != null;
    }

    public float getHumidity() {
        return humidity;
    }

    public int getSensorAccuracy() {
        return sensorAccuracy;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float humidityValue = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        sensorAccuracy = accuracy;
    }
}
