package com.example.driverrecognitionsystem;

public class User {
    private String email;
    private String displayName;
    private String userType;

    private String password;

    // Empty constructor for Firebase
    public User() {
    }

    public User(String email, String displayName, String userType, String password) {
        this.email = email;
        this.displayName = displayName;
        this.userType = userType;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}

