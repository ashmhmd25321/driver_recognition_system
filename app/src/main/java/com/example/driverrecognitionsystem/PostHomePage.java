package com.example.driverrecognitionsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

public class PostHomePage extends AppCompatActivity {

    TextView welcome;
    Button signOut, viewFines;

    ImageView profileImage;
    FirebaseAuth firebaseAuth;
    FirebaseStorage firebaseStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_home_page);

        welcome = findViewById(R.id.welcomeMessage);

        signOut = findViewById(R.id.signOutButton);
        viewFines = findViewById(R.id.viewFines);

        String userName = getIntent().getStringExtra("userName");

        welcome.setText(userName);


        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();

                // Redirect to the login page
                Intent intent = new Intent(PostHomePage.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        viewFines.setOnClickListener(view -> {
            Intent intent = new Intent(PostHomePage.this, ViewFinesAccordingToDriverId.class);
            startActivity(intent);
        });
    }
}