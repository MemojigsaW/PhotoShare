package com.example.imgs.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imgs.Adapters.comment_rc_adapterLegacy;
import com.example.imgs.Data.Photos;
import com.example.imgs.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommentPageLegacy extends AppCompatActivity {
    public static final String TAG = CommentPageLegacy.class.getSimpleName();

    private String picid;
    private String picpath;
    private String profilepicpath;

    private ProgressDialog progressDialog;

    private ImageView commentImage;
    private TextView commentImageCaption;
    private EditText commentPost;
    private ImageButton commentPostbt, deletePostbt;
    private RecyclerView recyclerView_comments;
    private RecyclerView.LayoutManager layoutManager;
    private comment_rc_adapterLegacy recyclerViewAdapter;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private static final String USERS = "users";
    private static final String PHOTOS = "photos";
    private static final String COMMENTS = "comments";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_page);

        Intent intent = getIntent();
        Photos photo_item = intent.getParcelableExtra("photo_item");

        Log.d(TAG, photo_item.getUid());

//        commentImage = findViewById(R.id.commentimg);
//        commentImageCaption = findViewById(R.id.commentimgcaption);
//        commentImageCaption.setMovementMethod(new ScrollingMovementMethod());
//        commentPost = findViewById(R.id.commentpost);
//        commentPostbt = findViewById(R.id.commentpostbt);
//        deletePostbt = findViewById(R.id.delpostbt);
//        recyclerView_comments = findViewById(R.id.recyclerViewComments);
//        layoutManager = new GridLayoutManager(this, 1);
//        recyclerView_comments.setLayoutManager(layoutManager);
//
//        commentPostbt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                postComment();
//            }
//        });
//        deletePostbt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                deletePost();
//            }
//        });
//
//        Intent intent = getIntent();
//        picpath = intent.getStringExtra("path");
//        picid = intent.getStringExtra("imguid");
//        profilepicpath = intent.getStringExtra("profilepicpath");
//
//
//        mFirestore = FirebaseFirestore.getInstance();
//        mAuth = FirebaseAuth.getInstance();
//        mUser = mAuth.getCurrentUser();
//        mStorage = FirebaseStorage.getInstance();
//        mStorageRef = mStorage.getReference();
//
//        loadimg();
//        loadcaption();
//        loadcomments();
    }


    private void loadimg() {
        String path = picpath;
        StorageReference refpath = mStorageRef.child(path);
        final long BYTE_LIMIT = 1024 * 1024;

        refpath.getBytes(BYTE_LIMIT)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        commentImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, commentImage.getWidth(), commentImage.getHeight(), false));
                        Log.d(TAG, "Comment pic load success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "load comment pic failed");
                    }
                });
    }

    private void loadcaption() {
        String imguid = picid;
        DocumentReference docref = mFirestore.collection(PHOTOS).document(imguid);
        docref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String posteruid = documentSnapshot.get("uid").toString();
                String s_caption = documentSnapshot.get("caption").toString();
                commentImageCaption.setText(s_caption);
                if (posteruid.equals(mUser.getUid())) {
                    CommentPageLegacy.this.deletePostbt.setVisibility(View.VISIBLE);
                }else{
                    CommentPageLegacy.this.deletePostbt.setVisibility(View.GONE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failure to load img caption");
            }
        });

    }

    private void loadcomments() {
        CollectionReference collectionReference = mFirestore.collection(COMMENTS);
        Query query = collectionReference
                .whereEqualTo("imguid", picid)
                .orderBy("timeStamp", Query.Direction.ASCENDING);
        query
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "Comments of pic loaded successfully");
                            QuerySnapshot allDocument = task.getResult();
                            int total_records = allDocument.size();

                            LinkedHashMap<String, String[]> mp = new LinkedHashMap<String, String[]>(total_records);

                            for (DocumentSnapshot document : allDocument.getDocuments()){
                                String value1 = document.getData().get("posteruid").toString();
                                String value2 = document.getData().get("content").toString();
                                String key = document.getId();
                                String[] ar = {value1, value2};
                                mp.put(key, ar);
                            }

                            recyclerViewAdapter = new comment_rc_adapterLegacy(mp);
                            CommentPageLegacy.this.recyclerView_comments.setAdapter(recyclerViewAdapter);
                            CommentPageLegacy.this.recyclerView_comments.setHasFixedSize(true);
                            Log.d(TAG, "Comments recyclerView set");

                        }else{
                            Log.d(TAG, "Comments of pic load failed");
                            Toast.makeText(CommentPageLegacy.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private String getTimeStamp() {
        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();
        return ts;
    }

    private void postComment() {
        String check = commentPost.getText().toString();
        if (check.equals("")){
            commentPost.setError("Comment is Empty!");
            return;
        }

        Map<String, Object> commentinfo = new HashMap<>();
        commentinfo.put("posteruid", mUser.getUid());
        commentinfo.put("imguid", picid);
        commentinfo.put("content", commentPost.getText().toString());
        commentinfo.put("timeStamp", Integer.parseInt(getTimeStamp()));

        mFirestore.collection(COMMENTS).add(commentinfo).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "Comment posted");
                commentPost.getText().clear();
                loadcomments();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Comment Failed");
                Toast.makeText(CommentPageLegacy.this, "Comment posting Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletePost() {
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        if (picpath.equals(profilepicpath)){
            Log.d(TAG, "delete profile pic, resetting profile pic");
            Map<String, Object> updateinfo = new HashMap<>();
            updateinfo.put("displayPicPath", "defaultavatar.jpg");

            DocumentReference documentReference = mFirestore.collection(USERS).document(mUser.getUid());
            documentReference.set(updateinfo, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "user avatar updated");
                    StorageReference storageReference = mStorageRef.child(picpath);
                    storageReference
                            .delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Log.d(TAG, "delete image from cloud storage success, now from collection");
                                        img_batch_delete();
                                    }else{
                                        Log.d(TAG, "delete image from cloud storage failed");
                                        progressDialog.dismiss();
                                        return;
                                    }
                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "user avatar update fail, abort");
                    progressDialog.dismiss();
                    return;
                }
            });
        }else{
            Log.d(TAG, "Deleting non profile pic pic");
            StorageReference storageReference = mStorageRef.child(picpath);
            storageReference
                    .delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.d(TAG, "delete image from cloud storage success, now from collection");
                                img_batch_delete();
                            }else{
                                Log.d(TAG, "delete image from cloud storage failed");
                                progressDialog.dismiss();
                                return;
                            }
                        }
                    });
        }


    }

    private void img_batch_delete() {
        WriteBatch batch = mFirestore.batch();

        DocumentReference photoref = mFirestore.collection(PHOTOS).document(picid);
        batch.delete(photoref);

        CollectionReference commentsref = mFirestore.collection(COMMENTS);
        Query query = commentsref.whereEqualTo("imguid", picid);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot allDocument = task.getResult();

//                populate batch
                for (DocumentSnapshot document : allDocument.getDocuments()){
                    DocumentReference docref = document.getReference();
                    batch.delete(docref);
                }

//                execute batch
                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "all photo records and comments records deleted");
                            Toast.makeText(CommentPageLegacy.this, "Post Deleted", Toast.LENGTH_SHORT).show();

                            Intent pp_refresh = new Intent();
                            pp_refresh.putExtra("do_refresh", true);
                            setResult(Activity.RESULT_OK, pp_refresh);
                            progressDialog.dismiss();
                            finish();

                        }else{
                            Log.d(TAG, "batch delete failed");
                            progressDialog.dismiss();
                            return;
                        }
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failed to retrive associated comments, img already deleted");
                progressDialog.dismiss();
                return;
            }
        });
    }
}