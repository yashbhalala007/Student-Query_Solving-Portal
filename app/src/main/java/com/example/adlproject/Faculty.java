package com.example.adlproject;

public class Faculty {
    private String profileImage, name;

    public Faculty() {
    }

    public Faculty(String profileImage, String name) {
        this.profileImage = profileImage;
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
