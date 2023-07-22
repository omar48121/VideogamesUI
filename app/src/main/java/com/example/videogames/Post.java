package com.example.videogames;

public class Post {
    private String userEmail;
    private String content;
    private String imageUrl;
    private String date;

    public Post(String userEmail, String content, String imageUrl, String date) {
        this.userEmail = userEmail;
        this.content = content;
        this.imageUrl = imageUrl;
        this.date = date;
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
}
