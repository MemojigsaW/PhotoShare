package com.example.imgs.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.imgs.Activity.CommentPage;
import com.example.imgs.Activity.Profile;
import com.example.imgs.Adapters.FeedImgAdapter;
import com.example.imgs.Data.Parameters;
import com.example.imgs.Data.Photos;
import com.example.imgs.Data.User;
import com.example.imgs.Interfaces.AdapterListener;
import com.example.imgs.Interfaces.GeneralListener;
import com.example.imgs.Presenter.FeedPresenter;
import com.example.imgs.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class GlobalFragment extends Fragment {
    RecyclerView rvPhotos;
    FeedImgAdapter rvAdapter;

    FeedPresenter presenter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new FeedPresenter(new GeneralListener() {
            @Override
            public void onUserProfileLoaded(User user, int postsnum, StorageReference sRef) {
                ;
            }

            @Override
            public void onPaginatePhotoLoaded(Photos[] photos) {
                rvAdapter.loadMoreData(photos);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_global, container, false);

        rvPhotos = view.findViewById(R.id.globalrv);
        rvAdapter = new FeedImgAdapter(getContext(), new AdapterListener() {
            @Override
            public void onScrollEnd() {
                presenter.fetchPaginateData(Parameters.GLOBAL);
            }

            @Override
            public void onClickStartActivity(Photos photo) {
                Intent LaunchCommentPage = new Intent(getActivity(), CommentPage.class);
                LaunchCommentPage.putExtra("photo_item", photo);
                LaunchCommentPage.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                getActivity().startActivityForResult(LaunchCommentPage, Profile.COMMENT_PAGE);
            }
        });
        rvPhotos.setAdapter(rvAdapter);
        rvPhotos.setLayoutManager(new GridLayoutManager(getContext(), 2));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.fetchUserProfile();
        presenter.fetchPaginateData(Parameters.GLOBAL);
    }

    public void refresh(){
        rvAdapter.clearData();
        presenter.resetPaginatecursor();
        presenter.fetchPaginateData(Parameters.GLOBAL);
    }
}