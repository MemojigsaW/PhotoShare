package com.example.imgs.Interfaces;

import com.example.imgs.Data.Photos;
import com.example.imgs.Data.User;
import com.google.firebase.storage.StorageReference;

public interface GeneralListener {
    void onUserProfileLoaded(User user, int postsnum, StorageReference sRef);
    void onPaginatePhotoLoaded(Photos[] photos);
}
