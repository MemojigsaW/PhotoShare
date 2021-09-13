package com.example.imgs.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.imgs.Data.Photos;
import com.example.imgs.Interfaces.AdapterListener;
import com.example.imgs.R;
import com.google.protobuf.StringValue;
import com.google.protobuf.Value;

import java.util.ArrayList;
import java.util.Arrays;

public class FeedImgAdapter extends RecyclerView.Adapter<FeedImgAdapter.FeedItemVH> {
    ArrayList<Photos> datalist;
    Context mContext;
    AdapterListener mListener;

    public FeedImgAdapter(Context context, AdapterListener listener){
        datalist = new ArrayList<Photos>();
        mContext = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public FeedItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view=  inflater.inflate(R.layout.feeditem, parent, false);
        return new FeedItemVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedItemVH holder, int position) {
        Photos data = datalist.get(position);
        holder.bind(data);
        if (position==getItemCount()-1){
            mListener.onScrollEnd();
        }
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }


    public void loadMoreData(Photos[] photos){
        datalist.addAll(Arrays.asList(photos));
        this.notifyItemRangeInserted(datalist.size()-photos.length, datalist.size()-1);
    }

    public void clearData(){
        datalist.clear();
        this.notifyDataSetChanged();
    }

    public class FeedItemVH extends RecyclerView.ViewHolder{
        ImageView feedImage;

        public FeedItemVH(@NonNull View itemView) {
            super(itemView);
            feedImage =itemView.findViewById(R.id.feeditem);
        }

        public void bind(Photos photo){
            Glide.with(mContext).load(photo.getsRef()).placeholder(R.drawable.default_user)
                    .into(feedImage);
            feedImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClickStartActivity(photo);
                }
            });
        }
    }
}
