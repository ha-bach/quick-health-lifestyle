package com.example.myapplication.exercise;

// Simple exercise amount recommendation service based on age and CDC guidelines to compliment exercise notification service
public class ExerciseRecommender {
    private int age;

    public ExerciseRecommender(int age) {
        this.age = age;
    }

    public String exerciseAmountRecommender() {
        if (age < 18) {
            return "60-100";
        } else {
            return "25-100";
        }
    }
}
