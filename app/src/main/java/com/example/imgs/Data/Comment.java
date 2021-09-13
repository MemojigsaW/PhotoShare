package com.example.imgs.Data;

public class Comment {
    private String content;
    private String imguid;
    private String posteruid;
    private int timeStamp;
    private User user;

    public Comment (){}

    public Comment(String content, String imguid, String posteruid, int timeStamp) {
        this.content = content;
        this.imguid = imguid;
        this.posteruid = posteruid;
        this.timeStamp = timeStamp;
    }

    public String getContent() {
        return content;
    }

    public String getImguid() {
        return imguid;
    }

    public String getPosteruid() {
        return posteruid;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
