package com.example.videogames;

public class Post {
    private String userEmail;
    private String content;
    private String imageUrl;
    private String date;
    private int likes;
    private boolean likesButtonDisabled;


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

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDate() {
        return date;
    }

    public int getLikes() {
        return likes;
    }

    public boolean isLikesButtonDisabled() {
        return likesButtonDisabled;
    }

    public void setLikesButtonDisabled(boolean likesButtonDisabled) {
        this.likesButtonDisabled = likesButtonDisabled;
    }
}
