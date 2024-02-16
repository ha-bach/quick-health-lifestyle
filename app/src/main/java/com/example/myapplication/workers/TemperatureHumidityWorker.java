package com.example.myapplication.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class TemperatureHumidityWorker extends Worker {
    public TemperatureHumidityWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {

        // TODO: retrieve temperature and humidity data from sensor
            // test temperature sensor here

        return Result.success();
    }
}
