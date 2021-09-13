package com.example.imgs.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.imgs.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUp extends AppCompatActivity {
    private static final String TAG = SignUp.class.getSimpleName();
    private static final int CAMERA_REQ = 1;

    private ProgressDialog progressDialog;

    private EditText ETemail, ETpw1, ETpw2, ETusername, ETbio;
    private ImageView IVavatar;
    private String s_email, s_username, s_bio, s_pw;
    private String PROFILEPATH, PROFILETIMESTAMP;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFirestore;
    private static final String USERS = "users";
    private static final String PHOTOS = "photos";
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;

    private boolean useDefaultImg = true;
    private static final String PROFILE_AVATAR_KEY = "com.example.assignmentproject.PROFILE_AVATAR_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ETemail = findViewById(R.id.SignUpemail);
        ETpw1 = findViewById(R.id.SignUppw1);
        ETpw2 = findViewById(R.id.SignUppw2);
        ETusername = findViewById(R.id.SignUpusername);
        ETbio = findViewById(R.id.SignUpbio);
        IVavatar = findViewById(R.id.TempAvatar);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        if (savedInstanceState == null) {
//            upon start
            Log.d(TAG, "Saved instance is null (first time)");
            useDefaultImg = true;
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.default_user);
            IVavatar.setImageBitmap(largeIcon);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getBoolean("useDefault")){
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.default_user);
            IVavatar.setImageBitmap(largeIcon);
            this.useDefaultImg = true;
        }else{
            try{
                FileInputStream is = this.openFileInput(SignUp.PROFILE_AVATAR_KEY);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                is.close();
                IVavatar.setImageBitmap(bmp);
                Log.d(TAG, "onRestore Load photo success");
                this.useDefaultImg = false;
            }catch(IOException e){
                Log.d(TAG, "onRestore Load photo failed");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: " + this.useDefaultImg);
        outState.putBoolean("useDefault", this.useDefaultImg);
    }

    public void doSignUp(View view) {
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        String pw1 = ETpw1.getText().toString();
        String pw2 = ETpw2.getText().toString();
        if (!checkPW(pw1, pw2)) {
            ETpw2.setError("PW does not match");
            progressDialog.dismiss();
            return;
        } else {
            s_email = ETemail.getText().toString();
            s_username = ETusername.getText().toString();
            s_bio = ETbio.getText().toString();
            s_pw = pw1;

            if (s_email.equals("")){
                ETemail.setError("Enter Email");
                progressDialog.dismiss();
                return;
            }else if (s_username.equals("")){
                ETusername.setError("Enter Username");
                progressDialog.dismiss();
                return;
            }else if (pw1.equals("")){
                ETpw1.setError("Enter PW");
                progressDialog.dismiss();
                return;
            }
            else {
                this.createUser();
            }
        }
    }

    private String getTimeStamp(){
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        return ts;
    }

    private void createUser(){
        mAuth.createUserWithEmailAndPassword(s_email, s_pw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User create success");
                            SignUp.this.uploadPic();
                        } else {
                            String reason;
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthWeakPasswordException e) {
                                reason = "weak password";
                                Log.d(TAG, reason);
                                ETpw1.setError("PW require 6 char");
                                progressDialog.dismiss();
                                return;
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                reason = "invalid credential";
                                Log.d(TAG, reason);
                                ETemail.setError("Invalid Credential");
                                progressDialog.dismiss();
                                return;
                            } catch (FirebaseAuthUserCollisionException e) {
                                reason = "auth user collision";
                                Log.d(TAG, reason);
                                ETemail.setError("Email already used");
                                progressDialog.dismiss();
                                return;
                            } catch (Exception e) {
                                reason = "other exceptions";
                                Log.d(TAG, reason);
                                progressDialog.dismiss();
                                return;
                            }
                        }
                    }
                });
    }

    private void setUserInfo(){
        Map<String, Object> userinfo = new HashMap<>();
        userinfo.put("username", SignUp.this.s_username);
        userinfo.put("bio", SignUp.this.s_bio);
        userinfo.put("displayPicPath", this.PROFILEPATH);

        mFirestore.collection(USERS).document(mUser.getUid())
                .set(userinfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User info set Success");
                        SignUp.this.setPhotoInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "User info set failed");
                        progressDialog.dismiss();
                        return;
                    }
                });

    }

    private void setPhotoInfo(){
//        mUser = mAuth.getCurrentUser();
        Map<String, Object> photoinfo = new HashMap<>();
        photoinfo.put("uid", mUser.getUid());
        photoinfo.put("storageRef", this.PROFILEPATH);
        photoinfo.put("timeStamp", Integer.parseInt(this.PROFILETIMESTAMP));
        photoinfo.put("caption", "Profile Avatar");

        mFirestore.collection(PHOTOS).add(photoinfo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "PhotoInfo added");
                        Intent profileintent = new Intent(SignUp.this, Profile.class);
                        startActivity(profileintent);
                        progressDialog.dismiss();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "PhotoInfo add Fail");
                        progressDialog.dismiss();
                        return;
                    }
                });
    }

    private void uploadPic(){
        mUser = mAuth.getCurrentUser();

//        if user does not use own photo, then use pre-existing default avatar and complete set user directly
        if (useDefaultImg){
            Map<String, Object> userinfo = new HashMap<>();
            userinfo.put("username", SignUp.this.s_username);
            userinfo.put("bio", SignUp.this.s_bio);
            userinfo.put("displayPicPath", "defaultavatar.jpg");

            mFirestore.collection(USERS).document(mUser.getUid())
                    .set(userinfo)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "User info set Success");
                            Intent profileintent = new Intent(SignUp.this, Profile.class);
                            startActivity(profileintent);
                            progressDialog.dismiss();
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "User info set failed");
                            progressDialog.dismiss();
                            return;
                        }
                    });
        }else {
            String TIMESTAMP = this.getTimeStamp();
            String UID = mUser.getUid();
            String SAVEPATH = "images/" + UID + "/" + TIMESTAMP + ".jpg";

            Bitmap bitmap = ((BitmapDrawable) IVavatar.getDrawable()).getBitmap();
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 1024, 1024, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

//            do dis to upload to images/ folder
            StorageReference refpath = mStorageRef.child(SAVEPATH);
            UploadTask uploadTask = refpath.putBytes(data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "Image upload success");
                    SignUp.this.PROFILEPATH = SAVEPATH;
                    SignUp.this.PROFILETIMESTAMP = TIMESTAMP;
                    SignUp.this.setUserInfo();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Image upload Fail");
                            progressDialog.dismiss();
                            return;
                        }
                    });
        }
    }

    public boolean checkPW(String pw1, String pw2) {
        if (pw1.equals(pw2)) {
            return true;
        } else {
            return false;
        }
    }

    public void doCamera(View view) {
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
                FileOutputStream stream = this.openFileOutput(SignUp.PROFILE_AVATAR_KEY, Context.MODE_PRIVATE);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                stream.close();
                imageBitmap.recycle();
                useDefaultImg = false;
                Log.d(TAG, "save photo succeed");
                try{
                    FileInputStream is = this.openFileInput(SignUp.PROFILE_AVATAR_KEY);
                    Bitmap bmp = BitmapFactory.decodeStream(is);
                    is.close();
                    IVavatar.setImageBitmap(bmp);
                    Log.d(TAG, "Then Load photo success");
                    this.useDefaultImg = false;
                }catch(IOException e){
                    Log.d(TAG, "Then Load photo failed");
                }
            }catch (IOException e){
                Log.d(TAG, "save photo failed");
            }

        } else {
            Log.d(TAG, "photo not caught");
            Toast.makeText(this, "Camera Failed", Toast.LENGTH_SHORT).show();
        }
    }
}