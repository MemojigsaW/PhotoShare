package com.example.imgs.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.imgs.Data.Comment;
import com.example.imgs.Data.User;
import com.example.imgs.Interfaces.AdapterListener;
import com.example.imgs.R;

import java.util.ArrayList;
import java.util.Arrays;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.VHcomment> {
    ArrayList<Comment> datalist =new ArrayList<>();
    Context mContext;
    AdapterListener mListener;

    public CommentAdapter(Context context, AdapterListener listener){
        mContext = context;
        mListener = listener;
    }

    public void doAppendData(Comment[] n_comments){
        datalist.addAll(Arrays.asList(n_comments));
        this.notifyItemRangeInserted(datalist.size()-n_comments.length, datalist.size()-1);

    }

    @NonNull
    @Override
    public VHcomment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.commentitem, parent, false);
        return new VHcomment(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VHcomment holder, int position) {
        holder.bind(datalist.get(position));
        if (position==getItemCount()-1){
            mListener.onScrollEnd();
        }
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    public class VHcomment extends RecyclerView.ViewHolder {
        ImageView commenterAvatar;
        TextView commenterName, commenterText;

        public VHcomment(@NonNull View itemView) {
            super(itemView);
            commenterAvatar = itemView.findViewById(R.id.commenterUserAvatar);
            commenterName = itemView.findViewById(R.id.commenterUserName);
            commenterText = itemView.findViewById(R.id.commenterUserText);
        }

        public void bind(Comment comment){
            User user = comment.getUser();
            Glide.with(mContext).load(user.getsRef()).into(commenterAvatar);
            commenterName.setText(user.getUsername());
            commenterText.setText(comment.getContent());
        }
    }
}
