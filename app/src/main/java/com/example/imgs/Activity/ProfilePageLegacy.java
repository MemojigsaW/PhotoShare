package com.example.imgs.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imgs.Adapters.feed_rc_adapterLegacy;
import com.example.imgs.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

public class ProfilePageLegacy extends AppCompatActivity {
    public static final String TAG = ProfilePageLegacy.class.getSimpleName();
    private static final String TAKE_PHOTO_KEY = "com.example.assignmentproject.TAKE_PHOTO_KEY";
    public static final int CAMERA_REQ = 1;
    public static final int CAPTION_PAGE = 2;
    public static final int COMMENT_PAGE = 3;

    private boolean personal_feed;

    private ConstraintLayout mPersonalFeed, mGlobalFeed;
    private TextView mUsername, mBio;
    private ImageView IVavatar;
    private FloatingActionButton mAdd, mSwitch, mLogout;
    private RecyclerView recyclerViewPersonal, recyclerViewGlobal;
    private RecyclerView.LayoutManager layoutManagerPersonal, layoutManagerGlobal;
    private feed_rc_adapterLegacy recyclerViewAdapter;
    private ActionBar AB;

    String s_username, s_bio, s_profilepath;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private static final String USERS = "users";
    private static final String PHOTOS = "photos";

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilepage);

        mPersonalFeed = findViewById(R.id.personalfeed);
        mGlobalFeed = findViewById(R.id.globalfeed);

        mUsername = findViewById(R.id.Username);
        mBio = findViewById(R.id.UserBio);
        mBio.setMovementMethod(new ScrollingMovementMethod());
        IVavatar=findViewById(R.id.ProfileAvatar);

        recyclerViewPersonal = findViewById(R.id.ImageRecycleViewPersonal);
        recyclerViewGlobal = findViewById(R.id.ImageRecycleViewGlobal);
        layoutManagerPersonal = new GridLayoutManager(this, 3);
        layoutManagerGlobal = new GridLayoutManager(this, 1);
        recyclerViewPersonal.setLayoutManager(layoutManagerPersonal);
        recyclerViewGlobal.setLayoutManager(layoutManagerGlobal);

        AB = getSupportActionBar();

        mAdd = findViewById(R.id.floatingActionButtonadd);
        mSwitch = findViewById(R.id.floatingActionButtonswitch);
        mLogout = findViewById(R.id.floatingActionButtonlogout);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();


        if (savedInstanceState != null){
            personal_feed = savedInstanceState.getBoolean("PERSONALFEED");
            Log.d(TAG, "current personal_feed " + personal_feed);
        }else{
            personal_feed = true;
        }

        if (personal_feed){
            mPersonalFeed.setVisibility(View.VISIBLE);
            mGlobalFeed.setVisibility(View.GONE);
            AB.setTitle("Personal Feed");
        }else{
            mPersonalFeed.setVisibility(View.GONE);
            mGlobalFeed.setVisibility(View.VISIBLE);
            AB.setTitle("Global Feed");
        }
        mHandler.postDelayed(removesupportbar, 5000);
        DocumentReference docref = mFirestore.collection(USERS).document(mUser.getUid());
        docref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: User info retrived successfully");
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()){
                                Log.d(TAG, "onComplete: document found");
                                ProfilePageLegacy.this.s_username =document.get("username").toString();
                                ProfilePageLegacy.this.s_bio = document.get("bio").toString();
                                ProfilePageLegacy.this.s_profilepath = document.get("displayPicPath").toString();

                                loadProfile();
                                loadSortedImagePath();

                            }else{
                                Log.d(TAG, "onComplete: this document not in collection");
                                mAuth.signOut();
                                finish();                            }
                        }else{
                            Log.d(TAG, "onComplete: Failed to retrive user from firestore");
                            mAuth.signOut();
                            finish();
                        }
                    }
                });
    }

    private Runnable removesupportbar = new Runnable() {
        @Override
        public void run() {
            AB.hide();
        }
    };

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("PERSONALFEED", personal_feed);
        Log.d(TAG, "saving personal feed as "+personal_feed);
        super.onSaveInstanceState(outState);
    }

    private void loadSortedImagePath(){
        CollectionReference collectionReference = mFirestore.collection(PHOTOS);
        Query query;
        if (personal_feed){
            query = collectionReference
                    .whereEqualTo("uid", mUser.getUid())
                    .orderBy("timeStamp", Query.Direction.DESCENDING);
        } else{
            query = collectionReference
                    .orderBy("timeStamp", Query.Direction.DESCENDING);
        }

        query
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            QuerySnapshot allDocument = task.getResult();
                            int total_records = allDocument.size();

                            LinkedHashMap<String, String> mp = new LinkedHashMap<String, String>(total_records);

//                            The key is the id of photos which is unique, thus linkedhashmap is fine
                            for (DocumentSnapshot document : allDocument.getDocuments()){
                                String value = document.getData().get("storageRef").toString();
                                String key = document.getId();
                                mp.put(key, value);
                            }

                            recyclerViewAdapter = new feed_rc_adapterLegacy(mp, personal_feed, ProfilePageLegacy.this, s_profilepath);
                            if (personal_feed){
                                ProfilePageLegacy.this.recyclerViewPersonal.setAdapter(recyclerViewAdapter);
                                ProfilePageLegacy.this.recyclerViewPersonal.setHasFixedSize(true);
                            }else{
                                ProfilePageLegacy.this.recyclerViewGlobal.setAdapter(recyclerViewAdapter);
                                ProfilePageLegacy.this.recyclerViewGlobal.setHasFixedSize(true);
                            }

                            Log.d(TAG, "RecyclerView setted");
                        }else{
                            Log.d(TAG, "Retrive image list failed");
                        }
                    }
                });
    }

    private void loadProfile(){
        StorageReference refpath = mStorageRef.child(s_profilepath);
        final long BYTE_LIMIT = 1024 *1024;

        refpath.getBytes(BYTE_LIMIT)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        IVavatar.setImageBitmap(Bitmap.createScaledBitmap(bmp, IVavatar.getWidth(), IVavatar.getHeight(), false));
                        mUsername.setText(s_username);
                        mBio.setText(s_bio);
                        Log.d(TAG, "Load profile pic success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "load profile pic failed");
                    }
                });
    }

    public void doLogOut(View view) {
        mAuth.signOut();
        finish();
    }

    public void doAdd(View view) {
        Intent camera_i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            startActivityForResult(camera_i, CAMERA_REQ);
        }catch (ActivityNotFoundException e){
            Log.d(TAG, "PackageManger not resolved");
            Toast.makeText(this, "No Camera Functionality", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQ && resultCode == RESULT_OK){
            Log.d(TAG, "photo caught");

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            try {
                FileOutputStream stream = this.openFileOutput(TAKE_PHOTO_KEY, Context.MODE_PRIVATE);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                stream.close();
                imageBitmap.recycle();
                Log.d(TAG, "save photo succeed");

                Intent launchcaptionpage = new Intent(this, CaptionPage.class);
                launchcaptionpage.putExtra("Caption_Image", TAKE_PHOTO_KEY);
                startActivityForResult(launchcaptionpage, CAPTION_PAGE);

            }catch (IOException e){
                Log.d(TAG, "save photo failed");
            }
        } else {
            Log.d(TAG, "camera event dismissed");
        }

        if ((requestCode == CAPTION_PAGE || requestCode == COMMENT_PAGE) && resultCode == Activity.RESULT_OK){
            boolean result = data.getBooleanExtra("do_refresh", true);
            Log.d(TAG, "Return Result from captionpage: "+result);
            if (result) {
                loadSortedImagePath();
            }
        }
    }

    public void switchmode(View view) {
        personal_feed = !personal_feed;
        Log.d(TAG, "set personal_feed to "+ personal_feed);
        loadProfile();
        loadSortedImagePath();

        if (personal_feed){
            mPersonalFeed.setVisibility(View.VISIBLE);
            mGlobalFeed.setVisibility(View.GONE);
            getSupportActionBar().setTitle("Personal Feed");
        }else{
            mPersonalFeed.setVisibility(View.GONE);
            mGlobalFeed.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("Global Feed");
        }
        getSupportActionBar().show();
        mHandler.postDelayed(removesupportbar, 5000);
    }
}