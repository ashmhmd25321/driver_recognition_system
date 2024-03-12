package com.example.driverrecognitionsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AddFineActivity extends AppCompatActivity {

    private EditText editTextDriverUser, editTextAddedDate, editTextPoliceUser, editTextReason, fineAmount, targetId, editTextVehicleNo;

    private TextView status;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fine);

        editTextDriverUser = findViewById(R.id.editTextDriverUser);
        editTextAddedDate = findViewById(R.id.editTextAddedDate);
        editTextPoliceUser = findViewById(R.id.editTextPoliceUser);
        editTextReason = findViewById(R.id.editTextReason);
        status = findViewById(R.id.status);
        fineAmount = findViewById(R.id.fineAmount);
        targetId = findViewById(R.id.targetId);
        editTextVehicleNo = findViewById(R.id.editTextVehicleNo);

        status.setText("Incomplete");
        status.setVisibility(View.GONE);

        int randomId = generateRandomId();

        targetId.setText(String.valueOf(randomId));
        targetId.setVisibility(View.GONE);

        String userName = getIntent().getStringExtra("user");
        String driverName = getIntent().getStringExtra("driver");

        editTextDriverUser.setText(driverName);
        editTextPoliceUser.setText(userName);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        editTextAddedDate.setText(currentDate);

        Button buttonSaveFine = findViewById(R.id.buttonSaveFine);
        buttonSaveFine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFineDetails();
            }
        });

    }

    private int generateRandomId() {
        Random random = new Random();
        return random.nextInt(10000);
    }

    private void saveFineDetails() {
        String driverUser = editTextDriverUser.getText().toString();
        String addedDate = editTextAddedDate.getText().toString();
        String policeUser = editTextPoliceUser.getText().toString();
        String reason = editTextReason.getText().toString();
        String statusF = status.getText().toString();
        String amount = fineAmount.getText().toString();
        String target = targetId.getText().toString();
        String vehicle = editTextVehicleNo.getText().toString();

        FineDetails fineDetails = new FineDetails(target, driverUser, addedDate, policeUser, reason, statusF, amount, vehicle);

        DatabaseReference finesReference = FirebaseDatabase.getInstance().getReference("Fine_Details");

        String fineId = finesReference.push().getKey();
        finesReference.child(fineId).setValue(fineDetails);

        Toast.makeText(this, "Fine details saved successfully with ID: " + fineId, Toast.LENGTH_SHORT).show();
    }
}