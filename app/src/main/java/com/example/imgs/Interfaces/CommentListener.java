package com.example.imgs.Interfaces;

import com.example.imgs.Data.Comment;
import com.example.imgs.Data.User;
import com.google.firebase.storage.StorageReference;

public interface CommentListener {
    void onLoadUser(User user, StorageReference sRef);
    void onLoadPaginateComments(Comment[] comments);
}
