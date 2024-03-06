package com.example.driverrecognitionsystem;

public class FrontImageInfo {

    private String imageUrl;

    private String username;

    private String imageType;

    public FrontImageInfo(String imageUrl, String username, String imageType) {
        this.imageUrl = imageUrl;
        this.username = username;
        this.imageType = imageType;
    }

    public FrontImageInfo() {
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }
}
