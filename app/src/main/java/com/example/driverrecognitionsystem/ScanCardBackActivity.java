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

public class ScanCardBackActivity extends AppCompatActivity {

    private final OkHttpClient okHttpClient = new OkHttpClient();
    private ImageView imageView, imageFront;
    private EditText convertedText, user, previousText;

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
        setContentView(R.layout.activity_scan_card_back);

        // Initialize views
        btnSelect = findViewById(R.id.btnChoose);
        Button btnUpload = findViewById(R.id.btnUpload);
        convertedText = findViewById(R.id.convertedText);
        imageView = findViewById(R.id.imageView);
        user = findViewById(R.id.user);
        previousText = findViewById(R.id.previousText);
        imageFront = findViewById(R.id.imageFront);

        // Set the Firebase reference
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        convertedText.setVisibility(View.GONE);

        databaseReference = FirebaseDatabase.getInstance().getReference("Images");

        String username = getIntent().getStringExtra("user");
        String previous = getIntent().getStringExtra("EXTRACTED_TEXT");
        byte[] imageData = getIntent().getByteArrayExtra("IMAGE_DATA");

        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

        imageFront.setImageBitmap(bitmap);

        user.setText(username);
        previousText.setText(previous);
        user.setVisibility(View.GONE);
        previousText.setVisibility(View.GONE);

        // Select btn on press
        btnSelect.setOnClickListener(v -> selectImage());

        // Upload btn on press
        btnUpload.setOnClickListener(v -> uploadImage());
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ScanCardBackActivity.this);
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

            StorageReference ref = storageReference.child("images/imageBack/" + user.getText().toString());

            // Listener on upload
            ref.putFile(filePath).addOnSuccessListener(taskSnapshot -> {
                progressDialog.dismiss();
                Toast.makeText(ScanCardBackActivity.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();

                // Points to the root reference
                StorageReference dataRef = storageReference.child("images/imageBack/" + user.getText().toString());
                dataRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Toast.makeText(ScanCardBackActivity.this, uri.toString(), Toast.LENGTH_SHORT).show();

                    // Save image info to Firebase Database for both ImageViews
                    saveImageInfoToDatabase(uri.toString(), user.getText().toString(), "BackImage");

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
                Toast.makeText(ScanCardBackActivity.this, "Failed!!", Toast.LENGTH_SHORT).show();
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

            // Send the extracted text to a new activity
            Intent intent = new Intent(ScanCardBackActivity.this, ConfirmDetailsActivity.class);
            intent.putExtra("EXTRACTED_TEXT_BACK", extractedText.toString());
            intent.putExtra("EXTRACTED_TEXT_FRONT", previousText.getText().toString());
            intent.putExtra("user", user.getText().toString());
            startActivity(intent);
        }
    }

    private void saveImageInfoToDatabase(String imageUrl, String username, String imageType) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("BackImage");

        BackImageInfo imageInfo = new BackImageInfo(imageUrl, username, imageType);

        databaseReference.child(username).setValue(imageInfo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ScanCardBackActivity.this, "Image Info Saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ScanCardBackActivity.this, "Failed to Save Image Info", Toast.LENGTH_SHORT).show();
                    }
                });
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