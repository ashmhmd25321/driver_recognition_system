package com.example.driverrecognitionsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.textclassifier.TextLinks;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ScanCardActivity extends AppCompatActivity {

    private final OkHttpClient okHttpClient = new OkHttpClient();
    private ImageView imageView;
    private EditText convertedText;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 22;

    // Firebase instance
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_card);

        // Initialize views
        Button btnSelect = findViewById(R.id.btnChoose);
        Button btnUpload = findViewById(R.id.btnUpload);
        convertedText = findViewById(R.id.convertedText);
        imageView = findViewById(R.id.imageView);

        // Set the Firebase reference
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        // Select btn on press
        btnSelect.setOnClickListener(v -> selectImage());

        // Upload btn on press
        btnUpload.setOnClickListener(v -> uploadImage());
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select the image from here..."), PICK_IMAGE_REQUEST);
    }

    private void uploadImage() {
        if (filePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/image");

            // Listener on upload
            ref.putFile(filePath).addOnSuccessListener(taskSnapshot -> {
                progressDialog.dismiss();
                Toast.makeText(ScanCardActivity.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();

                // Points to the root reference
                StorageReference dataRef = storageReference.child("images/image");
                dataRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Toast.makeText(ScanCardActivity.this, uri.toString(), Toast.LENGTH_SHORT).show();

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
                Toast.makeText(ScanCardActivity.this, "Failed!!", Toast.LENGTH_SHORT).show();
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
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                convertedText.setText(jsonObject.getString("ParsedText"));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}