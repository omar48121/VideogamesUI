package com.example.videogames;

public class Comment {
    private String postId;
    private String userEmail;
    private String userFullName;
    private String text;
    private String date;

    public Comment(String postId, String userEmail, String userFullName, String text, String date) {
        this.postId = postId;
        this.userEmail = userEmail;
        this.userFullName = userFullName;
        this.text = text;
        this.date = date;
    }


    public String getPostId() {
        return postId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getFullName() { return userFullName; }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }
}
