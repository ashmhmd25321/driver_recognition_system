package com.example.driverrecognitionsystem;

public class DriverLicence {

    private String text;
    private String user;
    private String imageUri;
    private String vehicles;

    public DriverLicence() {
    }

    public DriverLicence(String text, String user, String imageUri, String vehicles) {
        this.text = text;
        this.user = user;
        this.imageUri = imageUri;
        this.vehicles = vehicles;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getVehicles() {
        return vehicles;
    }

    public void setVehicles(String vehicles) {
        this.vehicles = vehicles;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
