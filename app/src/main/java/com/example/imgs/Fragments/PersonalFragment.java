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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class PersonalFragment extends Fragment {
    ConstraintLayout profileSection;
    ProgressBar progressBar;
    ImageView avatar;
    TextView username, bio, posts;

    RecyclerView rvPhotos;
    FeedImgAdapter rvAdapter;

    FeedPresenter presenter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new FeedPresenter(new GeneralListener() {
            @Override
            public void onUserProfileLoaded(User user, int postsnum, StorageReference sRef) {
                profileSection.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                username.setText(user.getUsername());
                bio.setText(user.getBio());
                posts.setText(String.valueOf(postsnum));

                StorageReference ssRef = FirebaseStorage.getInstance().getReference();
                StorageReference nRef = ssRef.child("defaultavatar.jpg");

                Glide
                        .with(PersonalFragment.this)
                        .load(nRef)
                        .into(avatar);
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
        View view = inflater.inflate(R.layout.fragment_personal, container, false);
        avatar = view.findViewById(R.id.avatar);
        username = view.findViewById(R.id.username);
        bio = view.findViewById(R.id.userbio);
        posts = view.findViewById(R.id.posts_num);
        rvPhotos = view.findViewById(R.id.photosRV);
        progressBar = view.findViewById(R.id.personalfeed_progressbar);
        profileSection = view.findViewById(R.id.personalfeed_profile);


        rvPhotos = view.findViewById(R.id.photosRV);
        rvAdapter = new FeedImgAdapter(getContext(), new AdapterListener() {
            @Override
            public void onScrollEnd() {
                Log.d("fragP", "onScrollEnd: ");
                presenter.fetchPaginateData(Parameters.PERSONAL);
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
        presenter.fetchPaginateData(Parameters.PERSONAL);
    }

    public void refresh(){
        rvAdapter.clearData();
        presenter.resetPaginatecursor();
        presenter.fetchPaginateData(Parameters.PERSONAL);
    }

}