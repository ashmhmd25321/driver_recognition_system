package com.example.driverrecognitionsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
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

public class ViewLicenceImages extends AppCompatActivity {

    TextView username;

    ImageView frontImage, backImage;

    FirebaseAuth firebaseAuth;
    FirebaseStorage firebaseStorage;

    DatabaseReference reference;
    FirebaseDatabase db;

    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_licence_images);

        username = findViewById(R.id.textView6);

        frontImage = findViewById(R.id.imageView3);
        backImage = findViewById(R.id.imageView4);

        String userName = getIntent().getStringExtra("user");
        username.setText(userName);
        username.setVisibility(View.GONE);

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        storageReference = firebaseStorage.getReference();

        loadFrontImage(userName);

        loadBackImage(userName);

    }

    private void loadFrontImage(String username) {
        StorageReference ref = storageReference.child("images/imageFront/" + username);

        ref.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load the image using the obtained URL
            Glide.with(this)
                    .load(uri)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Toast.makeText(ViewLicenceImages.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(frontImage);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Toast.makeText(ViewLicenceImages.this, "Failed to retrieve profile image", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadBackImage(String username) {
        StorageReference ref = storageReference.child("images/imageBack/" + username);

        ref.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load the image using the obtained URL
            Glide.with(this)
                    .load(uri)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Toast.makeText(ViewLicenceImages.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(backImage);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Toast.makeText(ViewLicenceImages.this, "Failed to retrieve profile image", Toast.LENGTH_SHORT).show();
        });
    }
}