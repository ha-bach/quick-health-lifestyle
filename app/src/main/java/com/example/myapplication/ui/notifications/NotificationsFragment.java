package com.example.myapplication.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.Login;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.Register;
import com.example.myapplication.databinding.FragmentNotificationsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsFragment extends Fragment {

private FragmentNotificationsBinding binding;

    TextView textViewProfileTitle, textViewEmail;
    EditText editTextFirstName, editTextLastName, editTextHeight, editTextWeight, editTextAge;
    Spinner spinnerSex, spinnerPreferredGymTime;
    Button updateButton, logoutButton;
    ProgressBar profileProgressBar;
    FirebaseAuth auth;
    FirebaseUser user;
    String userID;
    FirebaseFirestore firestore;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        textViewProfileTitle = binding.profileWelcome;
        textViewEmail = binding.profileEmailSample;
        editTextFirstName = binding.profileFirstNameInput;
        editTextLastName = binding.profileLastNameInput;
        editTextHeight = binding.profileHeightInput;
        editTextWeight = binding.profileWeightInput;
        spinnerSex = binding.profileSexSpinner;
        ArrayAdapter sexAdapter = (ArrayAdapter) spinnerSex.getAdapter();
        editTextAge = binding.profileAgeInput;
        spinnerPreferredGymTime = binding.profilePreferredGymTimeSpinner;
        ArrayAdapter preferredGymTimeAdapter = (ArrayAdapter) spinnerPreferredGymTime.getAdapter();
        updateButton = binding.profileUpdateButton;
        profileProgressBar = binding.profileProgressBar;
        logoutButton = binding.profileLogoutButton;

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userID = user.getUid();
        firestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firestore.collection("users").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (getActivity() != null && documentSnapshot != null) {
                    textViewProfileTitle.setText(getString(R.string.profile_intro, documentSnapshot.getString("firstName")));
                    editTextFirstName.setText(documentSnapshot.getString("firstName"));
                    editTextLastName.setText(documentSnapshot.getString("lastName"));
                    textViewEmail.setText(user.getEmail());
                    editTextHeight.setText(documentSnapshot.getLong("height").toString());
                    editTextWeight.setText(documentSnapshot.getLong("weight").toString());
                    spinnerSex.setSelection(sexAdapter.getPosition(documentSnapshot.getString("sex")));
                    editTextAge.setText(documentSnapshot.getLong("age").toString());
                    spinnerPreferredGymTime.setSelection(preferredGymTimeAdapter.getPosition(documentSnapshot.getString("preferredGymTime")));
                }
            }
        });


        updateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String firstName, lastName, email, password, height, weight, sex, age, preferredGymTime;
                firstName = String.valueOf(editTextFirstName.getText());
                lastName = String.valueOf(editTextLastName.getText());
                height = String.valueOf(editTextHeight.getText());
                weight = String.valueOf(editTextWeight.getText());
                sex = spinnerSex.getSelectedItem().toString();
                age = String.valueOf(editTextAge.getText());
                preferredGymTime = spinnerPreferredGymTime.getSelectedItem().toString();

                if (TextUtils.isEmpty(firstName)){
                    Toast.makeText(getActivity(), "Please enter your first name.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(lastName)){
                    Toast.makeText(getActivity(), "Please enter your last name.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(height)){
                    Toast.makeText(getActivity(), "Please enter your height.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(weight)){
                    Toast.makeText(getActivity(), "Please enter your weight.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(age)){
                    Toast.makeText(getActivity(), "Please enter your age.", Toast.LENGTH_SHORT).show();
                    return;
                }

                profileProgressBar.setVisibility(View.VISIBLE);

                DocumentReference documentReference = firestore.collection("users").document(userID);
                Map<String,Object> user = new HashMap<>();
                user.put("firstName", firstName);
                user.put("lastName", lastName);
                user.put("height", Integer.parseInt(height));
                user.put("weight", Integer.parseInt(weight));
                user.put("sex", sex);
                user.put("age", Integer.parseInt(age));
                user.put("preferredGymTime", preferredGymTime);
                Toast.makeText(getActivity(), "Account Update Successful.", Toast.LENGTH_SHORT).show();
                firestore.collection("users").document(userID).set(user, SetOptions.merge());

                profileProgressBar.setVisibility(View.GONE);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity().getApplicationContext(), Login.class);
                startActivity(intent);
                getActivity().finish();
            }
        });


        final TextView textView = binding.textNotifications;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}