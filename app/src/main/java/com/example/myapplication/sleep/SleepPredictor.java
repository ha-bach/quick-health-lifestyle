package com.example.myapplication.sleep;

public class SleepPredictor {
    private int wakeUpPrediction;
    private int age;

    public SleepPredictor(int age, int wakeUpPrediction) {
        this.age = age;
        this.wakeUpPrediction = wakeUpPrediction;
    }

    public int sleepTimeRecommender() {
        if (age < 18) {
            if (wakeUpPrediction >= 8)
                return wakeUpPrediction - 8;
            else
                return 24 + (wakeUpPrediction - 8);
        } else {
            if (wakeUpPrediction >= 7)
                return wakeUpPrediction - 7;
            else
                return 24 + (wakeUpPrediction - 7);
        }
    }
}
