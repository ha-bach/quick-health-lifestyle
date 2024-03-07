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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {

    EditText editTextEmail, editTextPassword, editTextHeight, editTextWeight, editTextAge;
    Spinner spinnerSex, spinnerPreferredGymTime;
    Button buttonRegister;
    ProgressBar registerProgressBar;
    TextView toLogin;
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
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
        mAuth = FirebaseAuth.getInstance();
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
                String email, password, height, weight, sex, age, preferredGymTime;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                height = String.valueOf(editTextHeight.getText());
                weight = String.valueOf(editTextWeight.getText());
                sex = spinnerSex.getSelectedItem().toString();
                age = String.valueOf(editTextAge.getText());
                preferredGymTime = spinnerPreferredGymTime.getSelectedItem().toString();

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

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                registerProgressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(Register.this, "Account Registration Successful.",
                                            Toast.LENGTH_SHORT).show();
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