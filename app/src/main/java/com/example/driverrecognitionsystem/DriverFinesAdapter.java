package com.example.driverrecognitionsystem;

import static com.google.android.gms.vision.L.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverFinesAdapter extends RecyclerView.Adapter<DriverFinesAdapter.FineViewHolder>{
    private List<FineDetails> fineList;
    private Context context;
    private AppCompatActivity activity;

    public DriverFinesAdapter(List<FineDetails> fineList, Context context, AppCompatActivity activity) {
        this.fineList = fineList;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public FineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fine, parent, false);
        return new FineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FineViewHolder holder, int position) {
        FineDetails fineDetails = fineList.get(position);

        holder.textDriverUser.setText("Driver User: " + fineDetails.getDriverUser());
        holder.textAddedDate.setText("Added Date: " + fineDetails.getAddedDate());
        holder.textPoliceUser.setText("Police User: " + fineDetails.getPoliceUser());
        holder.textReason.setText("Reason: " + fineDetails.getReason());
        holder.amount.setText("Amount (LKR): " + fineDetails.getFineAmount());
        holder.status.setText("Status: " + fineDetails.getStatus());
        holder.vehicle.setText("Vehicle Number: " + fineDetails.getVehicleNo());

        if ("Incomplete".equals(fineDetails.getStatus())) {
            holder.payment.setVisibility(View.VISIBLE);
        } else {
            holder.payment.setVisibility(View.GONE);
        }

        holder.payment.setOnClickListener(v -> showPaymentConfirmationDialog(fineDetails));
    }

    private void showPaymentConfirmationDialog(FineDetails fineDetails) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Payment");
        builder.setMessage("Do you want to confirm the payment for this fine?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            updateFineStatus(fineDetails);

            removeFineDetails(fineDetails);

            dialog.dismiss();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updateFineStatus(FineDetails fineDetails) {
        DatabaseReference finesReference = FirebaseDatabase.getInstance().getReference("Fine_Details");

        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("status", "Completed");

        finesReference.child(fineDetails.getTargetId())
                .updateChildren(updateFields)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        removeFineDetails(fineDetails);

                        addNewFineRecord(fineDetails);

                        refreshActivity();

                    } else {
                        Log.e("FineAdapter", "Failed to update fine status: " + task.getException().getMessage());
                    }
                });
    }

    private void addNewFineRecord(FineDetails fineDetails) {
        DatabaseReference finesReference = FirebaseDatabase.getInstance().getReference("Fine_Details");

        FineDetails newFineDetails = new FineDetails();
        newFineDetails.setDriverUser(fineDetails.getDriverUser());
        newFineDetails.setAddedDate(fineDetails.getAddedDate());
        newFineDetails.setPoliceUser(fineDetails.getPoliceUser());
        newFineDetails.setReason(fineDetails.getReason());
        newFineDetails.setFineAmount(fineDetails.getFineAmount());
        newFineDetails.setVehicleNo(fineDetails.getVehicleNo());
        newFineDetails.setTargetId(fineDetails.getTargetId());
        newFineDetails.setStatus("Completed");

        finesReference.push().setValue(newFineDetails)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Payment confirmed. New fine record added.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("FineAdapter", "Failed to add new fine record: " + task.getException().getMessage());
                    }
                });
    }

    private void refreshActivity() {
        Intent intent = activity.getIntent();
        activity.finish();
        activity.startActivity(intent);
    }

    public void setFineList(List<FineDetails> fineList) {
        this.fineList = fineList;
        notifyDataSetChanged();
    }

    private void removeFineDetails(FineDetails fineDetails) {
        Log.d("FineAdapter", "Removing fine details: " + fineDetails.getId());
        DatabaseReference finesReference = FirebaseDatabase.getInstance().getReference();
        Query applesQuery = finesReference.child("Fine_Details").orderByChild("targetId").equalTo(fineDetails.getTargetId());

        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    @Override
    public int getItemCount() {
        return fineList.size();
    }

    public class FineViewHolder extends RecyclerView.ViewHolder {

        public TextView textDriverUser;
        public TextView textAddedDate, vehicle;
        public TextView textPoliceUser, amount;
        public TextView textReason, status;

        Button payment;

        public FineViewHolder(@NonNull View itemView) {
            super(itemView);

            textDriverUser = itemView.findViewById(R.id.textDriverUser);
            textAddedDate = itemView.findViewById(R.id.textAddedDate);
            textPoliceUser = itemView.findViewById(R.id.textPoliceUser);
            textReason = itemView.findViewById(R.id.textReason);
            status = itemView.findViewById(R.id.status);
            amount = itemView.findViewById(R.id.amount);
            payment = itemView.findViewById(R.id.payment);
            vehicle = itemView.findViewById(R.id.vehicle);
        }
    }
}
