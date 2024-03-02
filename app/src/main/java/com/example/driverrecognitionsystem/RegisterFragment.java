package com.example.driverrecognitionsystem;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {

    private EditText etEmail, etPassword, etRePassword, fName;
    private Button btnRegister;
    private FirebaseAuth firebaseAuth;

    private RadioGroup radioGroupUserType;
    private RadioButton radioUser, radioPolice, radioPostOffice;

    public RegisterFragment() {
        // empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_register, container, false);

        etEmail = rootView.findViewById(R.id.et_email);
        etPassword = rootView.findViewById(R.id.et_password);
        etRePassword = rootView.findViewById(R.id.et_repassword);
        btnRegister = rootView.findViewById(R.id.btn_register);
        fName = rootView.findViewById(R.id.et_name);

        radioGroupUserType = rootView.findViewById(R.id.radioGroupUserType);
        radioUser = rootView.findViewById(R.id.radioUser);
        radioPolice = rootView.findViewById(R.id.radioPolice);
        radioPostOffice = rootView.findViewById(R.id.radioPostOffice);

        firebaseAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        return rootView;
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String rePassword = etRePassword.getText().toString().trim();
        String name = fName.getText().toString().trim();

        if (radioGroupUserType.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getActivity(), "Please select user type", Toast.LENGTH_SHORT).show();
            return;
        }

        String userType = (radioUser.isChecked()) ? "Driver" : (radioPolice.isChecked() ? "Police" : "Post Office");


        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(rePassword) || TextUtils.isEmpty(name)) {
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(rePassword)) {
            Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user with Firebase Authentication
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Set display name for the user
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Save the user type in the database
                                                    saveUserTypeInDatabase(userType);

                                                    // Send email verification
                                                    sendEmailVerification(user);

                                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(getActivity(), "Failed to set display name", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            String errorMessage = "Registration failed. Please try again.";
                            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserTypeInDatabase(String userType) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

            // Assuming you have a User class with a userType property
            User userInfo = new User(user.getEmail(), user.getDisplayName(), userType, etPassword.getText().toString());

            databaseReference.child(user.getUid()).setValue(userInfo)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getActivity(), "Failed to save user type", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            // Redirect to the main activity or perform any other actions
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getActivity(), "Failed to send verification email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}