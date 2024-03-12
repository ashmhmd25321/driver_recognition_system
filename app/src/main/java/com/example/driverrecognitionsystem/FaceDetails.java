package com.example.driverrecognitionsystem;

public class FaceDetails {

    private String username;
    private String faceID;
    private String faceKey;

    // Required default constructor for Firebase
    public FaceDetails() {
    }

    public FaceDetails(String username, String faceID, String faceKey) {
        this.username = username;
        this.faceID = faceID;
        this.faceKey = faceKey;
    }

    public String getUsername() {
        return username;
    }

    public String getFaceID() {
        return faceID;
    }

    public String getFaceKey() {
        return faceKey;
    }
}
