package com.example.driverrecognitionsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.driverrecognitionsystem.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class QRActivity extends AppCompatActivity {

    EditText extractedTextView;
    TextView user;

    Button saveToDb, backToHome;

    DatabaseReference reference;
    String licenceDetails;
    FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qractivity);

        String extractedText = getIntent().getStringExtra("EXTRACTED_TEXT");
        String username = getIntent().getStringExtra("user");

        user = findViewById(R.id.user);

        extractedTextView = findViewById(R.id.textView);
        saveToDb = findViewById(R.id.button);
        backToHome = findViewById(R.id.button2);

        extractedTextView.setText(extractedText);
        user.setText(username);
        user.setVisibility(View.GONE);

        saveToDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                licenceDetails = extractedTextView.getText().toString();

                if (!licenceDetails.isEmpty()) {
                    DriverLicence driverLicence = new DriverLicence(licenceDetails, username);
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
                                Toast.makeText(QRActivity.this, "Error checking existing data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        backToHome.setOnClickListener(v -> {
            Intent intent = new Intent(QRActivity.this, HomePageActivity.class);
            intent.putExtra("userName", username);
            startActivity(intent);
        });
    }

    private void showUpdateConfirmationDialog(DriverLicence driverLicence) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Confirmation");
        builder.setMessage("Driver Licence details already exist. Do you want to update the details?");
        builder.setPositiveButton("Update", (dialog, which) -> {
            updateRecord(driverLicence);
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void updateRecord(DriverLicence driverLicence) {
        reference.child(user.getText().toString()).setValue(driverLicence).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(QRActivity.this, "Driver Licence Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNewRecord(DriverLicence driverLicence) {
        reference.child(user.getText().toString()).setValue(driverLicence).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(QRActivity.this, "Driver Licence Saved Successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }
}