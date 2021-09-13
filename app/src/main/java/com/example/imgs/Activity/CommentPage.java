package com.example.imgs.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.imgs.Adapters.CommentAdapter;
import com.example.imgs.Data.Comment;
import com.example.imgs.Data.Photos;
import com.example.imgs.Data.User;
import com.example.imgs.Interfaces.AdapterListener;
import com.example.imgs.Interfaces.CommentListener;
import com.example.imgs.Presenter.CommentPresenter;
import com.example.imgs.R;
import com.google.firebase.storage.StorageReference;

public class CommentPage extends AppCompatActivity {
    public static final String TAG = CommentPage.class.getSimpleName();
    Photos cur_photo;

    ImageView contentImage, posterAvatar;
    TextView poster, hashtags;

    RecyclerView commentRV;
    CommentAdapter mAdapter;

    EditText commentInput;
    Button commentSend;

    ProgressDialog progressDialog;

    CommentPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_page);

        Intent intent = getIntent();
        cur_photo = intent.getParcelableExtra("photo_item");

        contentImage = findViewById(R.id.contentPhoto);
        poster = findViewById(R.id.Poster);
        hashtags = findViewById(R.id.Description);
        posterAvatar = findViewById(R.id.posteravatar);

        commentRV = findViewById(R.id.RVcomment);

        commentInput = findViewById(R.id.CommentInput);
        commentSend = findViewById(R.id.CommentPost);

        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        mPresenter = new CommentPresenter(new CommentListener() {
            @Override
            public void onLoadUser(User user, StorageReference sRef) {
                poster.setText(user.getUsername());
                hashtags.setText(cur_photo.getCaption());
                Glide.with(CommentPage.this).load(sRef).into(contentImage);
                Glide.with(CommentPage.this).load(user.getsRef()).into(posterAvatar);
                progressDialog.dismiss();
            }

            @Override
            public void onLoadPaginateComments(Comment[] comments) {
                mAdapter.doAppendData(comments);
            }
        }, cur_photo.getUid(), cur_photo);

        mAdapter = new CommentAdapter(this, new AdapterListener() {
            @Override
            public void onScrollEnd() {
                mPresenter.fetchPaginateComment();
            }

            @Override
            public void onClickStartActivity(Photos photo) {
                ;
            }
        });
        commentRV.setAdapter(mAdapter);
        commentRV.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.fetchUser();
        mPresenter.fetchPaginateComment();
    }
}