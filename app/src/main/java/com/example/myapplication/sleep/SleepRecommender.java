package com.example.myapplication.sleep;

public class SleepPredictor {
    private int previousSleep;
    private int age;

    public SleepPredictor(int age, int previousSleep) {
        this.age = age;
        this.previousSleep = previousSleep;
    }

    public int sleepTimeRecommender() {
        if (age < 18) {
            if (previousSleep >= 8)
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
