package com.example.myapplication.exercise;

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
