package com.example.myapplication.sleep;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SleepDatasetGenerator {

    static class Person {
        int age;
        String sex;
        String firstName;
        String lastName;
        int heightInches;
        int weightPounds;
        int wakeupTime;
        int sleepTime;

        public Person(int age, String sex, String firstName, String lastName, int heightInches, int weightPounds, int wakeupTime, int sleepTime) {
            this.age = age;
            this.sex = sex;
            this.firstName = firstName;
            this.lastName = lastName;
            this.heightInches = heightInches;
            this.weightPounds = weightPounds;
            this.wakeupTime = wakeupTime;
            this.sleepTime = sleepTime;
        }
    }

    static Map<String, List<Integer>> nameAttributesMap = new HashMap<>();
    static Map<String, String> firstNameLastNameMap = new HashMap<>();

    public static void main(String[] args) {
        List<Person> dataset = generateSyntheticDataset(10000);
        writeDatasetToFile(dataset, "sleep_dataset.csv");
    }

    public static List<Person> generateSyntheticDataset(int numSamples) {
        List<Person> dataset = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < numSamples; i++) {
            String firstName = generateRandomName(); // Generate a random name
            String lastName = firstNameLastNameMap.getOrDefault(firstName, generateRandomLastName()); // Retrieve or generate last name for the first name
            firstNameLastNameMap.putIfAbsent(firstName, lastName); // Store last name for the first name if not already stored
            List<Integer> attributes = nameAttributesMap.getOrDefault(firstName, generateRandomAttributes(random)); // Retrieve or generate attributes for the first name
            nameAttributesMap.putIfAbsent(firstName, attributes); // Store attributes for the first name if not already stored

            int age = attributes.get(0);
            String sex = attributes.get(1) == 0 ? "Male" : "Female";
            int heightInches = attributes.get(2);
            int weightPounds = attributes.get(3);
            int wakeupTime = random.nextInt(10) + 5; // Random wakeup time between 5 AM and 2 PM
            int sleepTime = random.nextInt(9) + 20; // Random sleep time between 8 PM and 5 AM
            dataset.add(new Person(age, sex, firstName, lastName, heightInches, weightPounds, wakeupTime, sleepTime));
        }
        return dataset;
    }

    public static List<Integer> generateRandomAttributes(Random random) {
        int age = random.nextInt(80) + 18; // Random age between 18 and 97
        int sex = random.nextInt(2); // Random sex (0 for male, 1 for female)
        int heightInches = random.nextInt(36) + 60; // Random height between 60 and 95 inches (5 to 7 ft 11 in)
        int weightPounds = random.nextInt(200) + 100; // Random weight between 100 and 299 pounds
        return Arrays.asList(age, sex, heightInches, weightPounds);
    }

    public static String generateRandomName() {
        String[] maleNames = {"John", "Robert", "Michael", "William", "David", "Joseph", "James", "Charles", "Thomas", "Daniel"};
        String[] femaleNames = {"Mary", "Jennifer", "Linda", "Patricia", "Elizabeth", "Susan", "Jessica", "Sarah", "Karen", "Nancy"};

        Random random = new Random();
        int gender = random.nextInt(2); // Randomly select gender (0 for male, 1 for female)
        if (gender == 0) {
            return maleNames[random.nextInt(maleNames.length)];
        } else {
            return femaleNames[random.nextInt(femaleNames.length)];
        }
    }

    public static String generateRandomLastName() {
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez"};
        Random random = new Random();
        return lastNames[random.nextInt(lastNames.length)];
    }

    public static void writeDatasetToFile(List<Person> dataset, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Age,Sex,FirstName,LastName,HeightInches,WeightPounds,WakeupTime,SleepTime\n");
            for (Person person : dataset) {
                writer.write(person.age + "," + person.sex + "," + person.firstName + "," + person.lastName + "," +
                        person.heightInches + "," + person.weightPounds + "," + person.wakeupTime + "," + person.sleepTime + "\n");
            }
            System.out.println("Dataset written to file: " + filename);
        } catch (IOException e) {
            System.err.println("Error writing dataset to file: " + e.getMessage());
        }
    }
}
