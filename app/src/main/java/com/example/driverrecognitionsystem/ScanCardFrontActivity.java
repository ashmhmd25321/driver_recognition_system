package com.example.driverrecognitionsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ScanCardFrontActivity extends AppCompatActivity {

    private final OkHttpClient okHttpClient = new OkHttpClient();
    private ImageView imageView;
    private EditText convertedText, user;

    private Button btnSelect;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 22;

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_CAMERA_CAPTURE = 101;

    private String capturedImagePath;

    // Firebase instance
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_card_front);

        // Initialize views
        btnSelect = findViewById(R.id.btnChoose);
        Button btnUpload = findViewById(R.id.btnUpload);
        convertedText = findViewById(R.id.convertedText);
        imageView = findViewById(R.id.imageView);
        user = findViewById(R.id.user);

        // Set the Firebase reference
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        convertedText.setVisibility(View.GONE);

        String username = getIntent().getStringExtra("user");

        databaseReference = FirebaseDatabase.getInstance().getReference("Images");

        user.setText(username);
        user.setVisibility(View.GONE);

        // Select btn on press
        btnSelect.setOnClickListener(v -> selectImage());

        // Upload btn on press
        btnUpload.setOnClickListener(v -> uploadImage());
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ScanCardFrontActivity.this);
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

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
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

    private void uploadImage() {
        if (filePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/imageFront/" + user.getText().toString());

            // Listener on upload
            ref.putFile(filePath).addOnSuccessListener(taskSnapshot -> {
                progressDialog.dismiss();
                Toast.makeText(ScanCardFrontActivity.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();

                // Points to the root reference
                StorageReference dataRef = storageReference.child("images/imageFront/" + user.getText().toString());
                dataRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Toast.makeText(ScanCardFrontActivity.this, uri.toString(), Toast.LENGTH_SHORT).show();

                    // Save image info to Firebase Database for both ImageViews
                    saveImageInfoToDatabase(uri.toString(), user.getText().toString(), "FrontImage");

                    // Use AsyncTask or another background threading mechanism instead of StrictMode
                    new Thread(() -> {
                        try {
                            sendPost(uri.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(ScanCardFrontActivity.this, "Failed!!", Toast.LENGTH_SHORT).show();
            }).addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setMessage("Uploaded" + (int) progress + "%");
            });
        }
    }

    private void sendPost(String imageUrl) throws IOException, JSONException {
        RequestBody formBody = new FormBody.Builder()
                .add("language", "eng")
                .add("isOverlayRequired", "false")
                .add("url", imageUrl)
                .add("iscreatesearchablepdf", "false")
                .add("issearchablepdfhidetextlayer", "false")
                .build();

        Request request = new Request.Builder()
                .url("https://api.ocr.space/parse/image")
                .addHeader("User-Agent", "OkHttp Bot")
                .addHeader("apikey", "K83799547888957")
                .post(formBody)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected Error" + response);

            String res = response.body().string();
            JSONObject obj = new JSONObject(res);
            JSONArray jsonArray = obj.getJSONArray("ParsedResults");
            StringBuilder extractedText = new StringBuilder();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                extractedText.append(jsonObject.getString("ParsedText")).append("\n");
            }

            byte[] imageData = getImageData(imageUrl);

            // Send the extracted text to a new activity
            Intent intent = new Intent(ScanCardFrontActivity.this, ScanCardBackActivity.class);
            intent.putExtra("EXTRACTED_TEXT", extractedText.toString());
            intent.putExtra("user", user.getText().toString());
            intent.putExtra("IMAGE_DATA", imageData);
            startActivity(intent);
        }
    }

    private void saveImageInfoToDatabase(String imageUrl, String username, String imageType) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("FrontImage");

        FrontImageInfo imageInfo = new FrontImageInfo(imageUrl, username, imageType);

        databaseReference.child(username).setValue(imageInfo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ScanCardFrontActivity.this, "Image Info Saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ScanCardFrontActivity.this, "Failed to Save Image Info", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private byte[] getImageData(String imageUrl) throws IOException {
        Request imageRequest = new Request.Builder()
                .url(imageUrl)
                .build();

        try (Response imageResponse = okHttpClient.newCall(imageRequest).execute()) {
            if (!imageResponse.isSuccessful()) throw new IOException("Unexpected Error" + imageResponse);

            return imageResponse.body().bytes();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera
                openCamera();
            } else {
                // Permission denied
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                filePath = data.getData();
                // Load the selected image into the ImageView
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == REQUEST_CAMERA_CAPTURE && resultCode == RESULT_OK) {
            // Load the captured image into the ImageView
            if (capturedImagePath != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(capturedImagePath);
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}