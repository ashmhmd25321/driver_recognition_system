package com.example.driverrecognitionsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewFinesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FineAdapter fineAdapter;
    private List<FineDetails> fineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_fines);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fineList = new ArrayList<>();
        fineAdapter = new FineAdapter(fineList, this, this);
        recyclerView.setAdapter(fineAdapter);

        String userName = getIntent().getStringExtra("user");

        loadFines(userName);
    }

    private void loadFines(String driverUsername) {
        DatabaseReference finesReference = FirebaseDatabase.getInstance().getReference("Fine_Details");

        finesReference.orderByChild("driverUser").equalTo(driverUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        fineList.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            FineDetails fineDetails = snapshot.getValue(FineDetails.class);
                            if (fineDetails != null) {
                                fineList.add(fineDetails);
                            }
                        }

                        fineAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ViewFinesActivity.this, "Failed to load fines: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}