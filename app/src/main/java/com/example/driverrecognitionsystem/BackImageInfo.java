package com.example.driverrecognitionsystem;

public class BackImageInfo {
    private String imageUrl;
    private String username;
    private String imageType;

    public BackImageInfo() {
    }

    public BackImageInfo(String imageUrl, String username, String imageType) {
        this.imageUrl = imageUrl;
        this.username = username;
        this.imageType = imageType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getImageType() {
        return imageType;
    }
}

