package com.example.imgs.Data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.storage.StorageReference;

public class Photos implements Parcelable {
    private String caption;
    private String storageRef;
    private int timeStamp;
    private String uid;
    private StorageReference sRef;
    private String id;

    public Photos (){};

    public Photos(String caption, String storageRef, int timeStamp, String uid) {
        this.caption = caption;
        this.storageRef = storageRef;
        this.timeStamp = timeStamp;
        this.uid = uid;
    }

    public String getCaption() {
        return caption;
    }

    public String getStorageRef() {
        return storageRef;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public String getUid() {
        return uid;
    }

    public StorageReference getsRef() {
        return sRef;
    }

    public void setsRef(StorageReference sRef) {
        this.sRef = sRef;
    }

    protected Photos(Parcel in) {
        caption = in.readString();
        storageRef = in.readString();
        timeStamp = in.readInt();
        uid = in.readString();
        id = in.readString();
    }

    public static final Creator<Photos> CREATOR = new Creator<Photos>() {
        @Override
        public Photos createFromParcel(Parcel in) {
            return new Photos(in);
        }

        @Override
        public Photos[] newArray(int size) {
            return new Photos[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(caption);
        dest.writeString(storageRef);
        dest.writeInt(timeStamp);
        dest.writeString(uid);
        dest.writeString(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
