package com.example.imgs.Interfaces;

import com.example.imgs.Data.Photos;

public interface AdapterListener {
    void onScrollEnd();
    void onClickStartActivity(Photos photo);
}
