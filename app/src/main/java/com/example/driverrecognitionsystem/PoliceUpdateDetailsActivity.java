package com.example.driverrecognitionsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PoliceUpdateDetailsActivity extends AppCompatActivity {

    EditText extractedTextView, vehicles;
    TextView user;

    private ImageView imageView;

    Button saveToDb, backToHome, chooseProfile;

    DatabaseReference reference;
    String licenceDetails;
    FirebaseDatabase db;

    FirebaseStorage firebaseStorage;

    FirebaseAuth firebaseAuth;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_police_update_details);

        String extractedTextFront = getIntent().getStringExtra("EXTRACTED_TEXT_FRONT");
        String extractedTextBack = getIntent().getStringExtra("EXTRACTED_TEXT_BACK");
        String username = getIntent().getStringExtra("user");

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        user = findViewById(R.id.user);

        extractedTextView = findViewById(R.id.textView);
        saveToDb = findViewById(R.id.button);
        backToHome = findViewById(R.id.button2);
        chooseProfile = findViewById(R.id.chooseImageButton);
        imageView = findViewById(R.id.imageView);
        vehicles = findViewById(R.id.textView2);

        extractedTextView.setText(extractedTextFront);
        vehicles.setText(extractedTextBack);
        user.setText(username);
        user.setVisibility(View.GONE);

        chooseProfile.setOnClickListener(v -> {
            openGallery();
        });

        saveToDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                licenceDetails = extractedTextView.getText().toString();
                String authorizedVehicles = vehicles.getText().toString();

                if (!licenceDetails.isEmpty() && imageUri != null) {
                    uploadImageToStorage(username);
                    DriverLicence driverLicence = new DriverLicence(licenceDetails, username, imageUri.toString(), authorizedVehicles);
                    db = FirebaseDatabase.getInstance();
                    reference = db.getReference("Licence_Details");

                    reference.child(username).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                DataSnapshot dataSnapshot = task.getResult();
                                if (dataSnapshot.exists()) {
                                    showUpdateConfirmationDialog(driverLicence);
                                } else {
                                    addNewRecord(driverLicence);
                                }
                            } else {
                                Toast.makeText(PoliceUpdateDetailsActivity.this, "Error checking existing data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        backToHome.setOnClickListener(v -> {
            Intent intent = new Intent(PoliceUpdateDetailsActivity.this, PoliceHomeActivity.class);
            intent.putExtra("userName", username);
            startActivity(intent);
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();

            try {
                // Convert the image URI to a Bitmap and set it to the ImageView
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e("ConfirmDetailsActivity", "Error loading image: " + e.getMessage());
            }
        }
    }

    private void showUpdateConfirmationDialog(DriverLicence driverLicence) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Confirmation");
        builder.setMessage("Details already exist. Do you want to update the details?");
        builder.setPositiveButton("Update", (dialog, which) -> {
            updateRecord(driverLicence);
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void updateRecord(DriverLicence driverLicence) {
        // Ensure that user is not null before accessing its text
        if (user != null) {
            reference.child(user.getText().toString()).setValue(driverLicence).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(PoliceUpdateDetailsActivity.this, "Driver Licence Updated Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PoliceUpdateDetailsActivity.this, PoliceHomeActivity.class);
                    intent.putExtra("userName", user.getText().toString());
                    startActivity(intent);
                }
            });
        } else {
            Toast.makeText(PoliceUpdateDetailsActivity.this, "User field is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void addNewRecord(DriverLicence driverLicence) {
        if (user != null) {
            reference.child(user.getText().toString()).setValue(driverLicence).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(PoliceUpdateDetailsActivity.this, "Details Saved Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PoliceUpdateDetailsActivity.this, PoliceHomeActivity.class);
                    intent.putExtra("userName", user.getText().toString());
                    startActivity(intent);
                }
            });
        } else {
            Toast.makeText(PoliceUpdateDetailsActivity.this, "User field is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToStorage(String username) {
        if (imageUri == null) {
            Toast.makeText(this, "Please choose an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a Storage Reference for the image
        StorageReference storageRef = firebaseStorage.getReference().child("profile_images/" + username);

        // Upload the image to Firebase Storage
        storageRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUri) {
                            String imageUrl = downloadUri.toString();
                            String authorizedVehicles = vehicles.getText().toString();
                            saveDataToDatabase(username, imageUrl, authorizedVehicles);
                        }
                    });
                } else {
                    // Handle the error
                    Toast.makeText(PoliceUpdateDetailsActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveDataToDatabase(String username, String imageUrl, String authorizedVehicles) {
        DriverLicence driverLicence = new DriverLicence(licenceDetails, username, imageUrl, authorizedVehicles);
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("Licence_Details");

        reference.child(username).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        showUpdateConfirmationDialog(driverLicence);
                    } else {
                        addNewRecord(driverLicence);
                    }
                } else {
                    Toast.makeText(PoliceUpdateDetailsActivity.this, "Error checking existing data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}