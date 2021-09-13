package com.example.imgs.Presenter;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.imgs.Data.Parameters;
import com.example.imgs.Data.User;
import com.example.imgs.Data.Photos;
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

public class FeedPresenter {
    private final static String TAG = "FeedPresenter";

    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseStorage mStorage;
    StorageReference mStorageRef;

    GeneralListener fragmentListener;

    private final static int PAGESIZE = 10;
    DocumentSnapshot paginateCursor;

    public FeedPresenter(GeneralListener feedloader) {
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        fragmentListener = feedloader;
        paginateCursor = null;
    }

    public void fetchUserProfile() {
//        not snapshot listener
        DocumentReference dRef = mFirestore.collection("users").document(mUser.getUid());
        Query q = mFirestore.collection("photos").whereEqualTo("uid", mUser.getUid());
        Task t1 = dRef.get();
        Task t2 = q.get();
        Task combinedTasks = Tasks.whenAllSuccess(t1, t2).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> objects) {
                DocumentSnapshot documentSnapshot = (DocumentSnapshot) objects.get(0);
                QuerySnapshot querySnapshot = (QuerySnapshot) objects.get(1);

                if (!documentSnapshot.exists()) {
                    Log.e(TAG, "onSuccess: Load user failed");
                    return;
                }

                User n_user = documentSnapshot.toObject(User.class);
                StorageReference sRef = mStorageRef.child(n_user.getDisplayPicPath());
                fragmentListener.onUserProfileLoaded(n_user, querySnapshot.size(), sRef);
            }
        });
    }

    public void resetPaginatecursor(){
        paginateCursor  = null;
    }

    public void fetchPaginateData(int mode) {
        Query query;

        switch (mode) {
            case Parameters.GLOBAL:
                query = mFirestore.collection("photos").orderBy("timeStamp", Query.Direction.DESCENDING);
                break;
            default:
                query = mFirestore.collection("photos").whereEqualTo("uid", mUser.getUid()).orderBy("timeStamp", Query.Direction.DESCENDING);
                break;
        }

        if (paginateCursor==null){
            query = query.limit(PAGESIZE);
        }else{
            query = query.startAfter(paginateCursor).limit(PAGESIZE);
        }

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Photos[] photoArray = new Photos[queryDocumentSnapshots.size()];
                int index = 0;
                for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                    Photos n_photo = documentSnapshot.toObject(Photos.class);
                    n_photo.setsRef(mStorageRef.child(n_photo.getStorageRef()));
                    n_photo.setId(documentSnapshot.getId());
                    photoArray[index] = n_photo;
                    index++;
                    if (index==queryDocumentSnapshots.size()){
                        paginateCursor = documentSnapshot;
                    }
                }

                fragmentListener.onPaginatePhotoLoaded(photoArray);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: The bloody fetch photo call failed");
            }
        });
    }
}
