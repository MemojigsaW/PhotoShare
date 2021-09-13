package com.example.imgs.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.imgs.Fragments.GlobalFragment;
import com.example.imgs.Fragments.PersonalFragment;
import com.example.imgs.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.protobuf.StringValue;

import java.io.FileOutputStream;
import java.io.IOException;

public class Profile extends AppCompatActivity {
    public final static String TAG = "ProfileActivity";
    private static final int CAMERA_REQ = 1;
    private static final String TAKE_PHOTO_KEY = "com.example.assignmentproject.TAKE_PHOTO_KEY";
    public static final int CAPTION_PAGE = 2;
    public static final int COMMENT_PAGE = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

//        Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_include);
        setSupportActionBar(toolbar);

//        Navbar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavController navController = Navigation.findNavController(findViewById(R.id.navhostfrag));
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_add:
                doAdd();
                return true;
            case R.id.toolbar_refresh:
                doRefresh();
                return true;
            case R.id.toolbar_logout:
                doLogout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void doAdd() {
        Intent camera_i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(camera_i, CAMERA_REQ);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "PackageManger not resolved");
        }
    }

    private void doRefresh(){
        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.navhostfrag);
        Fragment cur_frag;
        if (navHostFragment!=null){
            cur_frag = navHostFragment.getChildFragmentManager().getFragments().get(0);
            if (cur_frag instanceof PersonalFragment){
                ((PersonalFragment)cur_frag).refresh();
            }else if (cur_frag instanceof GlobalFragment){
                ((GlobalFragment)cur_frag).refresh();
            }
        }
    }

    private void doLogout() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQ && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            try {
                FileOutputStream stream = this.openFileOutput(TAKE_PHOTO_KEY, Context.MODE_PRIVATE);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                stream.close();
                imageBitmap.recycle();

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
            if (result) {
                doRefresh();
            }
        }
    }
}