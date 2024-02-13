package com.example.driverrecognitionsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class HomePageActivity extends AppCompatActivity {

    TextView welcome;
    Button signOut, scanBtn;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        welcome = findViewById(R.id.welcomeMessage);
        signOut = findViewById(R.id.signOutButton);
        scanBtn = findViewById(R.id.scanButton);
        firebaseAuth = FirebaseAuth.getInstance();

        String userName = getIntent().getStringExtra("userName");

        welcome.setText("Hi " + userName + " Welcome to the System");

        // Set an OnClickListener for the sign-out button
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();

                // Redirect to the login page
                Intent intent = new Intent(HomePageActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        scanBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, ScanCardActivity.class);
            startActivity(intent);
        });
    }
}