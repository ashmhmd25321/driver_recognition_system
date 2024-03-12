package com.example.driverrecognitionsystem;

public class FineDetails {

    private String driverUser;
    private String addedDate;
    private String policeUser;
    private String reason;

    public FineDetails() {
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
}
