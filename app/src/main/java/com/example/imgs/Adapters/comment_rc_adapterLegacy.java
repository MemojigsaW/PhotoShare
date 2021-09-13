package com.example.imgs.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imgs.Activity.CommentPage;
import com.example.imgs.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.LinkedHashMap;
import java.util.Set;

public class comment_rc_adapterLegacy extends RecyclerView.Adapter<comment_rc_adapterLegacy.CommentViewHolder> {
    private String TAG = CommentPage.TAG;
    private LinkedHashMap<String, String[]> uid_to_content;
    private String[] uidarray;

    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private FirebaseFirestore mFirestore;

    private static final String USERS = "users";
    private static final String PHOTOS = "photos";
    private static final String COMMENTS = "comments";

//    constructor
    public comment_rc_adapterLegacy(LinkedHashMap<String, String[]> input){
        this.uid_to_content = input;
        Set<String> keyset = input.keySet();
        uidarray = keyset.toArray(new String[keyset.size()]);

        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();
        mFirestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cp_rv_single_comment, parent, false);
        CommentViewHolder commentViewHolder = new CommentViewHolder(view);
        return commentViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        String commentid = uidarray[position];
        String posterid =uid_to_content.get(commentid)[0];
        String content = uid_to_content.get(commentid)[1];

        ImageView avatar = holder.commenteravatar;
        TextView username = holder.username;
        TextView comment = holder.comment;

        DocumentReference docref = mFirestore.collection(USERS).document(posterid);
        docref
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d(TAG, "Get user info success");
                        String commenter = documentSnapshot.get("username").toString();
                        String profilepath = documentSnapshot.get("displayPicPath").toString();
                        comment.setText(content);
                        username.setText(commenter);
                        loadavatar(profilepath, avatar);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Get user info failed");
                    }
                });
    }

    private void loadavatar(String profilepath, ImageView avatar) {
        StorageReference storageReference = mStorageRef.child(profilepath);
        final long BYTE_LIMIT = 1024*1024;

        storageReference
                .getBytes(BYTE_LIMIT)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        avatar.setImageBitmap(Bitmap.createScaledBitmap(bmp, avatar.getWidth(), avatar.getHeight(), false));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Load rc pic failed");
                    }
                });
    }

    @Override
    public int getItemCount() {
        return uid_to_content.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder{
        ImageView commenteravatar;
        TextView username, comment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commenteravatar = itemView.findViewById(R.id.commenteravatar);
            username = itemView.findViewById(R.id.commentername);
            comment = itemView.findViewById(R.id.commentcontent);
            comment.setMovementMethod(new ScrollingMovementMethod());
        }
    }


}
