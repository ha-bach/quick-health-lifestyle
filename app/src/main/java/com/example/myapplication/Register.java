package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Register extends AppCompatActivity {

    EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword, editTextHeight, editTextWeight, editTextAge;
    Spinner spinnerSex, spinnerPreferredGymTime;
    Button buttonRegister;
    ProgressBar registerProgressBar;
    TextView toLogin;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    String userID;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        editTextFirstName = findViewById(R.id.register_first_name_input);
        editTextLastName = findViewById(R.id.register_last_name_input);
        editTextEmail = findViewById(R.id.register_email_input);
        editTextPassword = findViewById(R.id.register_password_input);
        editTextHeight = findViewById(R.id.register_height_input);
        editTextWeight = findViewById(R.id.register_weight_input);
        spinnerSex = findViewById(R.id.register_sex_spinner);
        editTextAge = findViewById(R.id.register_age_input);
        spinnerPreferredGymTime = findViewById(R.id.register_preferred_gym_time_spinner);
        buttonRegister = findViewById(R.id.register_button);
        registerProgressBar = findViewById(R.id.register_progress_bar);
        toLogin = findViewById(R.id.register_return_to_login);
        toLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String firstName, lastName, email, password, height, weight, sex, age, preferredGymTime;
                firstName = String.valueOf(editTextFirstName.getText());
                lastName = String.valueOf(editTextLastName.getText());
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                height = String.valueOf(editTextHeight.getText());
                weight = String.valueOf(editTextWeight.getText());
                sex = spinnerSex.getSelectedItem().toString();
                age = String.valueOf(editTextAge.getText());
                preferredGymTime = spinnerPreferredGymTime.getSelectedItem().toString();

                if (TextUtils.isEmpty(firstName)){
                    Toast.makeText(Register.this, "Please enter your first name.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(lastName)){
                    Toast.makeText(Register.this, "Please enter your last name.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(email)){
                    Toast.makeText(Register.this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() <= 5){
                    Toast.makeText(Register.this, "Please enter a password with 6 to 20 characters.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)){
                    Toast.makeText(Register.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(height)){
                    Toast.makeText(Register.this, "Please enter your height.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(weight)){
                    Toast.makeText(Register.this, "Please enter your weight.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(age)){
                    Toast.makeText(Register.this, "Please enter your age.", Toast.LENGTH_SHORT).show();
                    return;
                }

                registerProgressBar.setVisibility(View.VISIBLE);

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                registerProgressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(Register.this, "Account Registration Successful.",
                                            Toast.LENGTH_SHORT).show();

                                    userID = auth.getCurrentUser().getUid();
                                    DocumentReference documentReference = firestore.collection("users").document(userID);
                                    Map<String,Object> user = new HashMap<>();
                                    user.put("firstName", firstName);
                                    user.put("lastName", lastName);
                                    user.put("height", Integer.parseInt(height));
                                    user.put("weight", Integer.parseInt(weight));
                                    user.put("sex", sex);
                                    user.put("age", Integer.parseInt(age));
                                    user.put("preferredGymTime", preferredGymTime);
                                    Integer[] emptyArray = {0,0,0,0,0,0,0};
                                    List<Integer> emptyHistory = Arrays.asList(emptyArray);
                                    user.put("sleepHistory", emptyHistory);
                                    user.put("hydrationHistory", emptyHistory);
                                    user.put("exerciseHistory", emptyHistory);
                                    user.put("exercisedToday", false);
                                    user.put("hydratedToday", false);
                                    documentReference.set(user);

                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(Register.this, "Account Registration Failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });

    }
}