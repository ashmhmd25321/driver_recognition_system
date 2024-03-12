package com.example.driverrecognitionsystem;

import com.google.firebase.database.FirebaseDatabase;

public class FineDetails {

    private String id;

    private String targetId;
    private String driverUser;
    private String addedDate;
    private String policeUser;
    private String reason;
    private String fineAmount;

    private String vehicleNo;

    private String status;

    public FineDetails() {
        this.id = FirebaseDatabase.getInstance().getReference("Fine_Details").push().getKey();
    }

    public FineDetails(String targetId, String driverUser, String addedDate, String policeUser, String reason, String status, String fineAmount, String vehicleNo) {
        this.driverUser = driverUser;
        this.addedDate = addedDate;
        this.policeUser = policeUser;
        this.reason = reason;
        this.status = status;
        this.fineAmount = fineAmount;
        this.targetId = targetId;
        this.vehicleNo = vehicleNo;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(String fineAmount) {
        this.fineAmount = fineAmount;
    }

    public String getDriverUser() {
        return driverUser;
    }

    public void setDriverUser(String driverUser) {
        this.driverUser = driverUser;
    }

    public String getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(String addedDate) {
        this.addedDate = addedDate;
    }

    public String getPoliceUser() {
        return policeUser;
    }

    public void setPoliceUser(String policeUser) {
        this.policeUser = policeUser;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }
}
