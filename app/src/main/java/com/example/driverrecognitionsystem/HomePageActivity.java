package com.example.driverrecognitionsystem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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

public class HomePageActivity extends AppCompatActivity {

    TextView welcome;
    Button signOut, scanBtn, viewDetailsBtn, fines;

    ImageView profileImage;
    FirebaseAuth firebaseAuth;
    FirebaseStorage firebaseStorage;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        welcome = findViewById(R.id.welcomeMessage);
        signOut = findViewById(R.id.signOutButton);
        scanBtn = findViewById(R.id.scanButton);
        viewDetailsBtn = findViewById(R.id.viewDetailsBtn);
        firebaseAuth = FirebaseAuth.getInstance();
        profileImage = findViewById(R.id.profileImage);
        firebaseStorage = FirebaseStorage.getInstance();
        fines = findViewById(R.id.fines);

        String userName = getIntent().getStringExtra("userName");
        String email = getIntent().getStringExtra("userEmail");

        welcome.setText(userName);

        loadProfileImage(userName);

        fines.setOnClickListener(view -> {
            Intent intent = new Intent(HomePageActivity.this, ViewFinesActivity.class);
            intent.putExtra("user", userName);
            startActivity(intent);
        });

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

        viewDetailsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, ViewLicenceDetails.class);
            intent.putExtra("user", userName);
            startActivity(intent);
        });

        scanBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, ScanCardFrontActivity.class);
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
                            Toast.makeText(HomePageActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(HomePageActivity.this, "Failed to retrieve profile image", Toast.LENGTH_SHORT).show();
        });
    }
}