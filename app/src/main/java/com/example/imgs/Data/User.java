package com.example.imgs.Data;

import com.google.firebase.storage.StorageReference;

public class User {
    private String bio;
    private String displayPicPath;
    private String username;
    private StorageReference sRef;

    public User(){};

    public User(String username, String bio, String displayPicPath) {
        this.bio = bio;
        this.displayPicPath = displayPicPath;
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public String getDisplayPicPath() {
        return displayPicPath;
    }

    public String getUsername() {
        return username;
    }

    public StorageReference getsRef() {
        return sRef;
    }

    public void setsRef(StorageReference sRef) {
        this.sRef = sRef;
    }
}
