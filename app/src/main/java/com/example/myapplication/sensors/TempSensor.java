package com.example.myapplication.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class TempSensor implements SensorEventListener {

    private final SensorManager sensorManager;
    private final Sensor temperatureSensor;
    private float lastTemperature = Float.NaN;
    private int accuracy = -1;

    public TempSensor(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        if (temperatureSensor == null) {
            throw new UnsupportedOperationException("Device does not have an ambient temperature sensor");
        }
    }

    public void startTemperatureUpdates(SensorEventListener sensorEventListener) {
        sensorManager.registerListener(sensorEventListener, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopTemperatureUpdates(SensorEventListener sensorEventListener) {
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            lastTemperature = event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        this.accuracy = accuracy;
    }

    public float getLastTemperature() {
        return lastTemperature;
    }

    public int getAccuracy() {
        return accuracy;
    }

}
