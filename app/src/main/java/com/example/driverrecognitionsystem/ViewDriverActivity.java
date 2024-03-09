package com.example.driverrecognitionsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import javax.annotation.Nullable;

public class ViewDriverActivity extends AppCompatActivity {

    Button scanner, search;
    EditText text;

    TextView driverDetails, username;

    FirebaseAuth firebaseAuth;
    FirebaseStorage firebaseStorage;

    DatabaseReference reference;
    FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_driver);

        scanner = findViewById(R.id.scanner);
        text = findViewById(R.id.driverId);
        search = findViewById(R.id.search);
        driverDetails = findViewById(R.id.licenceDetails);

        String userName = getIntent().getStringExtra("user");

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        scanner.setOnClickListener(v -> {
            IntentIntegrator intentIntegrator = new IntentIntegrator(ViewDriverActivity.this);
            intentIntegrator.setOrientationLocked(true);
            intentIntegrator.setPrompt("Scan the QR code");
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            intentIntegrator.initiateScan();
        });

        search.setOnClickListener(v -> {

            db = FirebaseDatabase.getInstance();

            reference = db.getReference("Licence_Details").child(text.getText().toString());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        DriverLicence driverLicence = dataSnapshot.getValue(DriverLicence.class);
                        if (driverLicence != null) {

                            String details = driverLicence.getText() + driverLicence.getVehicles();
                            driverDetails.setText(details);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ViewDriverActivity.this, "Failed to retrieve licence details", Toast.LENGTH_SHORT).show();
                }
            });

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (intentResult != null) {
            String contents = intentResult.getContents();
            if (contents != null) {
                text.setText(intentResult.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}