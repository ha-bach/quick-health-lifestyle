package com.example.myapplication.sleep;


// Sleep recommendation service based on user's age, previous night's sleep, and CDC guidelines implemented as machine learning approach was not working
public class SleepRecommender {
    private int previousSleep;
    private int age;

    public SleepRecommender(int age, int previousSleep) {
        this.age = age;
        this.previousSleep = previousSleep;
    }

    public int sleepAmountRecommender() {
        if (age < 18) {
            if (previousSleep <= 5)
                return 10;
            else if (previousSleep <= 7)
                return 9;
            else
                return 8;
        } else {
            if (previousSleep <= 4)
                return 9;
            else if (previousSleep <= 6)
                return 8;
            else
                return 7;
        }
    }
}
