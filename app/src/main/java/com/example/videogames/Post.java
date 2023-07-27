package com.example.videogames;

import java.util.List;

public class Post {
    private String userEmail;
    private String content;
    private String imageUrl;
    private String date;
    private  int likes;
    private boolean liked;


    public Post(String userEmail, String content, String imageUrl, String date) {
        this.userEmail = userEmail;
        this.content = content;
        this.imageUrl = imageUrl;
        this.date = date;
        this.likes = 0;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getContent() {
        return content;
    }

//    public String getImageResource() {
//        return imageUrl;
//    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDate() {
        return date;
    }

    public void incrementCounter() {
        likes = likes + 1;
    }

    public int getLikes() {
        return likes;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
