package com.example.driverrecognitionsystem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PoliceHomeActivity extends AppCompatActivity {

    TextView welcome;
    Button signOut, viewProfileBtn, driverProfile;

    ImageView profileImage;
    FirebaseAuth firebaseAuth;
    FirebaseStorage firebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_police_home);

        welcome = findViewById(R.id.welcomeMessage);

        signOut = findViewById(R.id.signOutButton);
        viewProfileBtn = findViewById(R.id.viewDetailsBtn);
        driverProfile = findViewById(R.id.driverProfile);

        profileImage = findViewById(R.id.profileImage);

        String userName = getIntent().getStringExtra("userName");

        welcome.setText(userName);

        loadProfileImage(userName);

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();

                // Redirect to the login page
                Intent intent = new Intent(PoliceHomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        viewProfileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PoliceHomeActivity.this, PoliceProfileActivity.class);
            intent.putExtra("user", userName);
            startActivity(intent);
        });

    }

    private void loadProfileImage(String username) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + username);

        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load the image using the obtained URL
            Glide.with(this)
                    .load(uri)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Toast.makeText(PoliceHomeActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(profileImage);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Toast.makeText(PoliceHomeActivity.this, "Failed to retrieve profile image", Toast.LENGTH_SHORT).show();
        });
    }
}