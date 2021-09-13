package com.example.imgs.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imgs.Activity.CommentPage;
import com.example.imgs.Activity.ProfilePageLegacy;
import com.example.imgs.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.LinkedHashMap;
import java.util.Set;

public class feed_rc_adapterLegacy extends RecyclerView.Adapter<feed_rc_adapterLegacy.MyViewHolder> {
    private LinkedHashMap<String,String> listPath;
    private String[] keyarray;
    boolean personalfeed;
    private String profilepicpath;
    private Context mContext;
    public static final String TAG = ProfilePageLegacy.class.getSimpleName();

    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;

//    constructor
    public feed_rc_adapterLegacy(LinkedHashMap<String, String> input, boolean personalfeed, Context context, String s_profilepicpath){
        this.listPath = input;
        this.personalfeed = personalfeed;
        this.mContext = context;
        this.profilepicpath = s_profilepicpath;
        Set<String> keySet = input.keySet();
        this.keyarray = keySet.toArray(new String[keySet.size()]);

        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (personalfeed){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pp_rv_personal_single_view, parent, false);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pp_rv_global_single_view, parent, false);
        }
        MyViewHolder myViewHolder = new MyViewHolder(view);

        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = myViewHolder.getAdapterPosition();
                String key = feed_rc_adapterLegacy.this.keyarray[position];
                String path = feed_rc_adapterLegacy.this.listPath.get(key);

                Intent LaunchCommentPage = new Intent(mContext, CommentPage.class);
                LaunchCommentPage.putExtra("imguid", key);
                LaunchCommentPage.putExtra("path", path);
                LaunchCommentPage.putExtra("profilepicpath", profilepicpath);
                LaunchCommentPage.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                AppCompatActivity source = (AppCompatActivity) mContext;
                source.startActivityForResult(LaunchCommentPage, ProfilePageLegacy.COMMENT_PAGE);
            }
        });

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        String key = this.keyarray[position];
        String current_path = this.listPath.get(key);
        ImageView current_IV = holder.IVsingle;

        StorageReference refpath = mStorageRef.child(current_path);
        final long BYTE_LIMIT = 1024 *1024;

        refpath.getBytes(BYTE_LIMIT)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        current_IV.setImageBitmap(bmp);
//                        Log.d(TAG, "Load rc pic success");
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
        return this.listPath.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView IVsingle;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            if (personalfeed) {
                IVsingle = itemView.findViewById(R.id.rc_personal_singleView);
            } else {
                IVsingle = itemView.findViewById(R.id.rc_global_singleView);
            }
        }
    }
}
