package com.example.driverrecognitionsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PoliceProfileActivity extends AppCompatActivity {

    TextView name, username, branch;
    Button updateBtn;
    ImageView profileImage;

    FirebaseAuth firebaseAuth;
    FirebaseStorage firebaseStorage;

    DatabaseReference reference;
    FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_police_profile);

        name = findViewById(R.id.textView3);
        username = findViewById(R.id.textView4);
        branch = findViewById(R.id.textView5);

        updateBtn = findViewById(R.id.button4);
        profileImage = findViewById(R.id.imageView2);

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        String userName = getIntent().getStringExtra("user");
        username.setText(userName);

        loadProfileImage(userName);

        db = FirebaseDatabase.getInstance();

        reference = db.getReference("Licence_Details").child(userName);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DriverLicence driverLicence = dataSnapshot.getValue(DriverLicence.class);
                    if (driverLicence != null) {

                        String details = driverLicence.getText();
                        String branchD = driverLicence.getVehicles();
                        name.setText("FullName: " + details);
                        branch.setText("Police Station: " + branchD);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PoliceProfileActivity.this, "Failed to retrieve details", Toast.LENGTH_SHORT).show();
            }
        });

        updateBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PoliceProfileActivity.this, PoliceUpdateDetailsActivity.class);
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
                            Toast.makeText(PoliceProfileActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(PoliceProfileActivity.this, "Failed to retrieve profile image", Toast.LENGTH_SHORT).show();
        });
    }
}