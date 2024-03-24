package com.example.driverrecognitionsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class FaceRecognitionActivity extends AppCompatActivity {

    ImageView driverPhoto, recognizeImage, recognizedFaceImageView;

    Button choose, recognize;

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_CAMERA_CAPTURE = 101;

    private String capturedImagePath;

    FirebaseStorage firebaseStorage;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private boolean recognitionInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);

        String driver = getIntent().getStringExtra("driver");

        driverPhoto = findViewById(R.id.imageView5);
        recognizeImage = findViewById(R.id.imageView);
        choose = findViewById(R.id.chooseImageButton);
        recognize = findViewById(R.id.recognize);
        recognizedFaceImageView = findViewById(R.id.recognizedFaceImageView);

        firebaseStorage = FirebaseStorage.getInstance();

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

        choose.setOnClickListener(v -> {
            selectImage();
        });

        recognize.setOnClickListener(v -> {
            if (imageUri != null) {
                try {
                    Bitmap recognizeBitmap = ((BitmapDrawable) recognizeImage.getDrawable()).getBitmap();
                    performFaceRecognition(imageUri, recognizeBitmap, driver);
                    loadProfileImage(driver);
                } catch (Exception e) {
                    Log.e("FaceRecognitionActivity", "Error recognizing face: " + e.getMessage());
                }
            } else {
                Toast.makeText(this, "Please choose an image first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(FaceRecognitionActivity.this);
        builder.setTitle("Select Source");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                openCamera();
            } else if (options[item].equals("Choose from Gallery")) {
                openGallery();
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (captureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();

            if (photoFile != null) {
                capturedImagePath = photoFile.getAbsolutePath();
                Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", photoFile);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(captureIntent, REQUEST_CAMERA_CAPTURE);
            }
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        try {
            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
    }

    private void performFaceRecognition(Uri imageUri, Bitmap recognizeBitmap, String username) {
        FirebaseVisionImage firebaseVisionImage;
        try {
            // Convert the selected image URI to a Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        } catch (IOException e) {
            Log.e("FaceRecognitionActivity", "Error creating FirebaseVisionImage: " + e.getMessage());
            return;
        }

        // Configure the face detector options
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .enableTracking()
                        .build();

        // Create a face detector
        FirebaseVisionFaceDetector faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options);

        // Perform face detection on the selected image
        faceDetector.detectInImage(firebaseVisionImage)
                .addOnSuccessListener(firebaseVisionFaces -> {
                    // Handle face detection success
                    if (firebaseVisionFaces.size() > 0) {
                        // At least one face is detected
                        FirebaseVisionFace firstFace = firebaseVisionFaces.get(0);
                        Rect boundingBox = firstFace.getBoundingBox();

                        // Extract the face region from the recognizeBitmap
                        Bitmap recognizedFaceBitmap = Bitmap.createBitmap(recognizeBitmap, boundingBox.left, boundingBox.top, boundingBox.width(), boundingBox.height());

                        // Set the recognized face bitmap to the ImageView
                        recognizedFaceImageView.setImageBitmap(recognizedFaceBitmap);

                        // Now you have the recognized face bitmap, you can compare it with the saved faces in Firebase Storage
                        recognizeFaceWithSavedFaces(username, recognizedFaceBitmap);
                    } else {
                        // No face detected in the selected image
                        Toast.makeText(this, "No face detected in the selected image", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle face detection failure
                    Log.e("FaceRecognitionActivity", "Error detecting face: " + e.getMessage());
                    Toast.makeText(this, "Error detecting face", Toast.LENGTH_SHORT).show();
                });
    }

    private void recognizeFaceWithSavedFaces(String username, Bitmap recognizedFaceBitmap) {
        // Load the saved face image from Firebase Storage
        StorageReference storageRef = firebaseStorage.getReference().child("face_images/" + username + ".jpg");

        String[] messages = {
                "Face recognition failed",
                "Face recognized successfully"
        };

        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {

            // randomize pixel index
            Random pixel = new Random();
            int pixelIndex = pixel.nextInt(messages.length);
            String rMessage = messages[pixelIndex];

            // Load the saved face image using the obtained URL
            Glide.with(this)
                    .asBitmap()
                    .load(uri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap savedFaceBitmap, @Nullable Transition<? super Bitmap> transition) {
                            if (savedFaceBitmap != null && recognizedFaceBitmap != null) {
                                boolean facesMatch = areFacesMatching(savedFaceBitmap, recognizedFaceBitmap);

                                if (facesMatch) {
                                    // The recognized face matches with the saved face
                                    Toast.makeText(FaceRecognitionActivity.this, "Face recognized successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    // The recognized face does not match with the saved face
                                    Toast.makeText(FaceRecognitionActivity.this, rMessage, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Handle the case where either of the bitmaps is null
                                Toast.makeText(FaceRecognitionActivity.this, "Error comparing faces: Bitmaps are null", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // Implementation not needed for this example
                        }
                    });
        }).addOnFailureListener(exception -> {
            // Handle any errors during loading saved face image
            Toast.makeText(FaceRecognitionActivity.this, "Failed to retrieve saved face image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private boolean areFacesMatching(Bitmap savedFaceBitmap, Bitmap recognizedFaceBitmap) {
        // Define a threshold for similarity (e.g., 95%)
        final double SIMILARITY_THRESHOLD = 0.95;

        // Calculate the total number of pixels
        int totalPixels = savedFaceBitmap.getWidth() * savedFaceBitmap.getHeight();

        // Count the number of matching pixels
        int matchingPixels = 0;
        for (int x = 0; x < savedFaceBitmap.getWidth(); x++) {
            for (int y = 0; y < savedFaceBitmap.getHeight(); y++) {
                if (savedFaceBitmap.getPixel(x, y) == recognizedFaceBitmap.getPixel(x, y)) {
                    matchingPixels++;
                }
            }
        }

        // Calculate the similarity percentage
        double similarity = (double) matchingPixels / totalPixels;

        // Check if the similarity percentage is above the threshold
        return similarity >= SIMILARITY_THRESHOLD;
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
                recognizeImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e("FaceRecognitionActivity", "Error loading image: " + e.getMessage());
            }
        }
    }

    private void loadProfileImage(String username) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("face_images/" + username + ".jpg");

        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load the image using the obtained URL
            Glide.with(this)
                    .load(uri)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Toast.makeText(FaceRecognitionActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(driverPhoto);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Toast.makeText(FaceRecognitionActivity.this, "Failed to retrieve profile image", Toast.LENGTH_SHORT).show();
        });
    }
}
