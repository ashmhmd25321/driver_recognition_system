package com.example.driverrecognitionsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddFineActivity extends AppCompatActivity {

    private EditText editTextDriverUser, editTextAddedDate, editTextPoliceUser, editTextReason;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fine);

        editTextDriverUser = findViewById(R.id.editTextDriverUser);
        editTextAddedDate = findViewById(R.id.editTextAddedDate);
        editTextPoliceUser = findViewById(R.id.editTextPoliceUser);
        editTextReason = findViewById(R.id.editTextReason);

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

    private void saveFineDetails() {
        String driverUser = editTextDriverUser.getText().toString();
        String addedDate = editTextAddedDate.getText().toString();
        String policeUser = editTextPoliceUser.getText().toString();
        String reason = editTextReason.getText().toString();

        FineDetails fineDetails = new FineDetails();
        fineDetails.setDriverUser(driverUser);
        fineDetails.setAddedDate(addedDate);
        fineDetails.setPoliceUser(policeUser);
        fineDetails.setReason(reason);

        DatabaseReference fineReference = FirebaseDatabase.getInstance().getReference("Fine_Details");
        fineReference.push().setValue(fineDetails);

        Toast.makeText(this, "Fine details saved successfully", Toast.LENGTH_SHORT).show();
    }
}