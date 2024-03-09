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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private FirebaseAuth firebaseAuth;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        etEmail = rootView.findViewById(R.id.et_email);
        etPassword = rootView.findViewById(R.id.et_password);
        btnLogin = rootView.findViewById(R.id.btn_login);

        firebaseAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        return rootView;
    }

    private void loginUser() {
        String emailOrDisplayName = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(emailOrDisplayName) || TextUtils.isEmpty(password)) {
            // Handle empty fields
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the input is an email
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrDisplayName).matches()) {
            // Input is an email, sign in with email
            firebaseAuth.signInWithEmailAndPassword(emailOrDisplayName, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Retrieve user display name after signing in
                                String userName = getUserDisplayName();

                                checkUserRoleAndNavigate(userName);
//                                // Navigate to the HomePageActivity
//                                Intent intent = new Intent(getActivity(), HomePageActivity.class);
//                                intent.putExtra("userName", userName);
//                                intent.putExtra("userEmail", emailOrDisplayName);
//                                startActivity(intent);
//                                getActivity().finish();
                            } else {
                                String errorMessage = "Login failed. Please check your credentials and try again.";
                                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            checkDisplayNameAndPassword(emailOrDisplayName, password);
        }
    }

    private void checkUserRoleAndNavigate(String userName) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.orderByChild("displayName").equalTo(userName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);

                        if (user != null) {
                            String userType = user.getUserType();

                            // Check user role and navigate accordingly
                            switch (userType) {
                                case "Driver":
                                    navigateToDriverActivity(userName);
                                    break;
                                case "Police":
                                    navigateToPoliceActivity(userName);
                                    break;
                                case "Post Office":
//                                    navigateToPostOfficeActivity(userName);
                                    break;
                                default:
                                    // Unknown role
                                    Toast.makeText(getActivity(), "Unknown user role", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    // User not found
                    Toast.makeText(getActivity(), "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(getActivity(), "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToDriverActivity(String userName) {
        // Redirect to the DriverActivity
        Intent intent = new Intent(getActivity(), HomePageActivity.class);
        intent.putExtra("userName", userName);
        startActivity(intent);
        getActivity().finish();
    }

    private void navigateToPoliceActivity(String userName) {
        // Redirect to the PoliceActivity
        Intent intent = new Intent(getActivity(), PoliceHomeActivity.class);
        intent.putExtra("userName", userName);
        startActivity(intent);
        getActivity().finish();
    }

    private String getUserDisplayName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // User is signed in
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                return displayName;
            } else {
                return "User";
            }
        } else {
            return "Guest";
        }
    }

    private void checkDisplayNameAndPassword(String displayName, String password) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.orderByChild("displayName").equalTo(displayName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User with provided display name found
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);

                        if (user != null && user.getPassword().equals(password)) {
                            // Passwords match, login successful
                            checkUserRoleAndNavigate(displayName);
                            // Navigate to the HomePageActivity
//                            Intent intent = new Intent(getActivity(), HomePageActivity.class);
//                            intent.putExtra("userName", displayName);
//                            startActivity(intent);
//                            getActivity().finish();
                        } else {
                            // Passwords do not match
                            Toast.makeText(getActivity(), "Incorrect password", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // User with provided display name not found
                    Toast.makeText(getActivity(), "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(getActivity(), "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }


}