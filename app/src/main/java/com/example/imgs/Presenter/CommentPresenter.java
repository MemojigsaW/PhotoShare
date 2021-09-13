package com.example.imgs.Presenter;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.imgs.Data.Comment;
import com.example.imgs.Data.Photos;
import com.example.imgs.Data.User;
import com.example.imgs.Interfaces.CommentListener;
import com.example.imgs.Interfaces.GeneralListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class CommentPresenter {
    /***
     * fetch photo info
     * fetch paginate comments
     * use callback to update UI after***/
    private final static String TAG="CommentPresenter";
    private final static int PAGESIZE =10;

    CommentListener mListener;

    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseStorage mStorage;
    StorageReference mStorageRef;

    String mUid;
    Photos mPhoto;

    DocumentSnapshot mCursor;


    public CommentPresenter(CommentListener listener, String uid, Photos photo){
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        mListener = listener;
        mUid = uid;
        mPhoto = photo;

        mCursor = null;
    }

    public void fetchUser(){
        DocumentReference dRef = mFirestore.collection("users").document(mUid);
        dRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                StorageReference sRef = mStorageRef.child(mPhoto.getStorageRef());
                StorageReference uRef = mStorageRef.child(user.getDisplayPicPath());
                user.setsRef(uRef);
                mListener.onLoadUser(user, sRef);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: fetch user of photo failed");
            }
        });
    }

    public void fetchPaginateComment(){
        Query query = mFirestore
                .collection("comments")
                .whereEqualTo("imguid", mPhoto.getId())
                .orderBy("timeStamp", Query.Direction.ASCENDING);

        if (mCursor==null){
            query = query.limit(PAGESIZE);
        }else{
            query = query.startAfter(mCursor).limit(PAGESIZE);
        }

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Comment[] comments = new Comment[queryDocumentSnapshots.size()];
                Task[] tasks = new Task[queryDocumentSnapshots.size()];
                int index = 0;
                for (DocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    Comment n_comment = documentSnapshot.toObject(Comment.class);
                    comments[index] = n_comment;
                    DocumentReference dRef = mFirestore.collection("users").document(n_comment.getPosteruid());
                    tasks[index] = dRef.get();
                    index++;
                    if (index==queryDocumentSnapshots.size()){
                        mCursor = documentSnapshot;
                    }
                }
                Task combinedTasks = Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                    @Override
                    public void onSuccess(List<Object> objects) {
                        int index =0;
                        for (Object object:objects){
                            DocumentSnapshot documentSnapshot = (DocumentSnapshot) object;
                            User n_user = documentSnapshot.toObject(User.class);
                            StorageReference sRef = mStorageRef.child(n_user.getDisplayPicPath());
                            n_user.setsRef(sRef);
                            comments[index].setUser(n_user);
                            index++;
                        }
                        mListener.onLoadPaginateComments(comments);

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed comment query");
            }
        });
    }
}
