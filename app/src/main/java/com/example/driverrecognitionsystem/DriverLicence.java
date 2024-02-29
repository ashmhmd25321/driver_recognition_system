package com.example.driverrecognitionsystem;

public class DriverLicence {

    private String text;
    private String user;

    public DriverLicence() {
    }

    public DriverLicence(String text, String user) {
        this.text = text;
        this.user = user;
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
}
