package com.example.driverrecognitionsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewFinesAccordingToDriverId extends AppCompatActivity {

    private EditText editTextDriverUsername;
    private Button buttonSearch;
    private RecyclerView recyclerViewFines;
    private DriverFinesAdapter fineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_fines_according_to_driver_id);

        editTextDriverUsername = findViewById(R.id.editTextDriverUsername);
        buttonSearch = findViewById(R.id.buttonSearch);
        recyclerViewFines = findViewById(R.id.recyclerViewFines);

        // Set up RecyclerView
        recyclerViewFines.setLayoutManager(new LinearLayoutManager(this));
        fineAdapter = new DriverFinesAdapter(new ArrayList<>(), this, this);
        recyclerViewFines.setAdapter(fineAdapter);

        buttonSearch.setOnClickListener(v -> searchFinesByUsername());
    }

    private void searchFinesByUsername() {
        String username = editTextDriverUsername.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            editTextDriverUsername.setError("Please enter a username");
            return;
        }

        // Perform a query to search fines by driver's username
        DatabaseReference finesReference = FirebaseDatabase.getInstance().getReference("Fine_Details");
        Query query = finesReference.orderByChild("driverUser").equalTo(username);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<FineDetails> finesList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FineDetails fineDetails = snapshot.getValue(FineDetails.class);
                    if (fineDetails != null) {
                        finesList.add(fineDetails);
                    }
                }

                displayFines(finesList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    private void displayFines(List<FineDetails> finesList) {
        if (finesList.isEmpty()) {

        } else {
            fineAdapter.setFineList(finesList);
            fineAdapter.notifyDataSetChanged();
            recyclerViewFines.setVisibility(android.view.View.VISIBLE);
        }
    }
}