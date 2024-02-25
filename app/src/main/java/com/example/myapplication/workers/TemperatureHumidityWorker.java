package com.example.myapplication.workers;

import android.content.Context;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myapplication.sensors.HumiditySensor;
import com.example.myapplication.sensors.TemperatureSensor;

public class TemperatureHumidityWorker extends Worker {

    private SensorManager sensorManager;
    private static final String TAG = "TemperatureHumidityWorker";

    public TemperatureHumidityWorker (
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public Result doWork() {
        TemperatureSensor temperatureSensor = new TemperatureSensor(getApplicationContext());
        HumiditySensor humiditySensor = new HumiditySensor(getApplicationContext());
        if(!temperatureSensor.isSensorAvailable() && !humiditySensor.isSensorAvailable())
        {
            WorkManager.getInstance(getApplicationContext()).cancelWorkById(this.getId());
            return Result.failure();
        }

        // TODO: add notification code here
        float temperature = temperatureSensor.getTemperature();

        return Result.success();
    }

}
