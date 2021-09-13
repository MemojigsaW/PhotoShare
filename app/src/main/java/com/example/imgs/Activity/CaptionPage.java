package com.example.imgs.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.imgs.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaptionPage extends AppCompatActivity {
    public static final String TAG = CaptionPage.class.getSimpleName();
    private String img_key;

    private ImageView caption_image;
    private EditText caption_box;
    private Button autohash_bt, cancel_bt, accept_bt;
    private ProgressDialog progressDialog;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private static final String USERS = "users";
    private static final String PHOTOS = "photos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caption_page);

        caption_image = findViewById(R.id.captionimage);
        caption_box = findViewById(R.id.captionbox);
        autohash_bt = findViewById(R.id.autohashtagbt);
        autohash_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autohash_handle();
            }
        });
        cancel_bt = findViewById(R.id.cancelbt);
        cancel_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel_handle();
            }
        });
        accept_bt = findViewById(R.id.acceptbt);
        accept_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload_handle();
            }
        });

        Intent from_pp = getIntent();
        img_key = from_pp.getStringExtra("Caption_Image");
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            FileInputStream in_st = this.openFileInput(img_key);
            Bitmap bmp = BitmapFactory.decodeStream(in_st);
            in_st.close();
            caption_image.setImageBitmap(bmp);
        } catch (Exception e) {
            Log.d(TAG, "Image failed to load");
        }

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

    }

    private void cancel_handle() {
        File dir = getFilesDir();
        File file = new File(dir, img_key);
        boolean result = file.delete();
        Log.d(TAG, "File delete status: " + result);
        finish();
    }

    private void upload_handle() {
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Bitmap bmp;
        try {
            FileInputStream in_st = this.openFileInput(img_key);
            bmp = BitmapFactory.decodeStream(in_st);
            in_st.close();
        } catch (Exception e) {
            Log.d(TAG, "Image failed to load");
            progressDialog.dismiss();
            return;
        }

        String TIMESTAMP = this.getTimeStamp();
        String UID = mUser.getUid();
        String SAVEPATH = "images/" + UID +"/" + TIMESTAMP + ".jpg";
        String caption = caption_box.getText().toString();
        if (caption.equals("")){
            caption_box.setError("Caption is Empty!");
            progressDialog.dismiss();
            return;
        }

        Bitmap resized = Bitmap.createScaledBitmap(bmp, 1024, 1024, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference refpath = mStorageRef.child(SAVEPATH);
        UploadTask uploadTask = refpath.putBytes(data);

//        cloud stroage does not support batch operation coupling firestore/cloud storage
//        do upload, then firestore update, if fail delete uploaded
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Image upload success");
                //        chains async requests
                int time = Integer.parseInt(TIMESTAMP);
                setPhotoInfo(UID, SAVEPATH, time, caption);
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

    private void autohash_handle() {
        Bitmap bmp;
        try {
            FileInputStream in_st = this.openFileInput(img_key);
            bmp = BitmapFactory.decodeStream(in_st);
            in_st.close();
        } catch (Exception e) {
            Log.d(TAG, "Image failed to load");
            return;
        }

//        no rotation, assume the image is upright
        InputImage img = InputImage.fromBitmap(bmp, 0);

        ImageLabelerOptions options =
                new ImageLabelerOptions.Builder()
                        .setConfidenceThreshold(0.7f)
                        .build();
        ImageLabeler labeler = ImageLabeling.getClient(options);

        labeler.process(img).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
            @Override
            public void onSuccess(List<ImageLabel> imageLabels) {
                Log.d(TAG, "label success");
                for (ImageLabel label : imageLabels) {
                    String text = label.getText();
                    float confidence = label.getConfidence();
                    int index = label.getIndex();
                    caption_box.append(" #"+text);
                    Log.d(TAG, "0.7f label success");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "label failed" + e);
            }
        });
    }

    private String getTimeStamp(){
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        return ts;
    }

    private void setPhotoInfo(String uid, String path, int time, String caption){
        Map<String, Object> photoinfo = new HashMap<>();
        photoinfo.put("uid", uid);
        photoinfo.put("storageRef", path);
        photoinfo.put("timeStamp", time);
        photoinfo.put("caption", caption);

        mFirestore.collection(PHOTOS).add(photoinfo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "PhotoInfo added");
                        Toast.makeText(getApplicationContext(),"Photo upload success",Toast.LENGTH_SHORT).show();

                        Intent pp_refresh = new Intent();
                        pp_refresh.putExtra("do_refresh", true);
                        setResult(Activity.RESULT_OK, pp_refresh);
                        progressDialog.dismiss();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "PhotoInfo add Fail");
//                        on fail, deletes the uploaded image
                        delete_onfail(path);
                    }
                });
    }

    private void delete_onfail(String ref){
        StorageReference refpath = mStorageRef.child(ref);
        refpath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Successful delete of uploaded image after failed setphotoinfo");
                progressDialog.dismiss();
                return;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "FAIL del after fail set photoinfo");
                progressDialog.dismiss();
                return;
            }
        });
    }












}