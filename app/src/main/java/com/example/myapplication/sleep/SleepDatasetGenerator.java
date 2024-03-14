package com.example.myapplication.sleep;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SleepDatasetGenerator {

    static class Person {
        int id;
        int age;
        String sex;
        String firstName;
        String lastName;
        int heightInches;
        int weightPounds;
        int wakeupTime; // in hours (24-hour format)
        int sleepTime; // in hours (24-hour format)
        int sleepDuration; // in hours

        public Person(int id, int age, String sex, String firstName, String lastName, int heightInches, int weightPounds, int wakeupTime, int sleepTime, int sleepDuration) {
            this.id = id;
            this.age = age;
            this.sex = sex;
            this.firstName = firstName;
            this.lastName = lastName;
            this.heightInches = heightInches;
            this.weightPounds = weightPounds;
            this.wakeupTime = wakeupTime;
            this.sleepTime = sleepTime;
            this.sleepDuration = sleepDuration;
        }
    }

    public static void main(String[] args) {
        List<Person> dataset = generateSyntheticDataset(100000);
        writeDatasetToCSV(dataset, "sleep_dataset.csv");
    }

    public static void writeDatasetToCSV(List<Person> dataset, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("id,age,sex,firstName,lastName,heightInches,weightPounds,wakeupTime,sleepTime,sleepDuration\n");
            for (Person person : dataset) {
                writer.write(person.id + "," + person.age + "," + person.sex + "," + person.firstName + "," + person.lastName +
                        "," + person.heightInches + "," + person.weightPounds + "," + person.wakeupTime + "," + person.sleepTime +
                        "," + person.sleepDuration + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Person> generateSyntheticDataset(int numSamples) {
        List<Person> dataset = new ArrayList<>();
        Random random = new Random();
        Map<String, Person> firstNameToPersonMap = new HashMap<>();
        Map<String, String> firstNameToLastNameMap = new HashMap<>();

        for (int i = 0; i < numSamples; i++) {
            String firstName = generateRandomName(random.nextBoolean(), random);
            if (firstNameToPersonMap.containsKey(firstName)) {
                // If the first name already exists, use the same last name, ID, age, weight, and height
                Person existingPerson = firstNameToPersonMap.get(firstName);
                Person person = new Person(existingPerson.id, existingPerson.age, existingPerson.sex, firstName,
                        existingPerson.lastName, existingPerson.heightInches, existingPerson.weightPounds,
                        existingPerson.wakeupTime, existingPerson.sleepTime, existingPerson.sleepDuration);
                dataset.add(person);
            } else {
                String lastName = generateRandomLastName(random);
                int id = i + 1; // Unique ID for each person
                int age = random.nextInt(80) + 18; // Random age between 18 and 97
                String sex = random.nextBoolean() ? "Male" : "Female";
                int heightInches = random.nextInt(36) + 60; // Random height between 60 and 95 inches (5 to 7 ft 11 in)
                int weightPounds = random.nextInt(200) + 100; // Random weight between 100 and 299 pounds
                int wakeupTime = random.nextInt(24); // Random wakeup time between 0 and 23
                int sleepTime = random.nextInt(24); // Random sleep time between 0 and 23
                int sleepDuration = calculateSleepDuration(wakeupTime, sleepTime); // Calculate sleep duration

                if (sleepDuration <= 14 && sleepDuration >=3) {
                    // Only add data where sleep duration is greater than 14 or less than 3
                    Person person = new Person(id, age, sex, firstName, lastName, heightInches, weightPounds, wakeupTime, sleepTime, sleepDuration);
                    dataset.add(person);

                    // Store the first name and last name
                    firstNameToPersonMap.put(firstName, person);
                    firstNameToLastNameMap.put(firstName, lastName);
                }
            }
        }
        return dataset;
    }

    // Generate a random name
    public static String generateRandomName(boolean isMale, Random random) {
        String[] maleNames = {"John", "Robert", "Michael", "William", "David", "Joseph", "James", "Charles", "Thomas", "Daniel"};
        String[] femaleNames = {"Mary", "Jennifer", "Linda", "Patricia", "Elizabeth", "Susan", "Jessica", "Sarah", "Karen", "Nancy"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez"};

        if (isMale) {
            return maleNames[random.nextInt(maleNames.length)];
        } else {
            return femaleNames[random.nextInt(femaleNames.length)];
        }
    }

    // Generate a random last name
    public static String generateRandomLastName(Random random) {
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez"};
        return lastNames[random.nextInt(lastNames.length)];
    }

    // Calculate sleep duration
    public static int calculateSleepDuration(int wakeupTime, int sleepTime) {
        int sleepDuration;
        if (wakeupTime >= sleepTime) {
            sleepDuration = 24 - wakeupTime + sleepTime;
        } else {
            sleepDuration = sleepTime - wakeupTime;
        }
        return 24 - sleepDuration;
    }
}
