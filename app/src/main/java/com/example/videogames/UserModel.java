package com.example.videogames;

import java.util.Date;
import java.util.List;

public class UserModel {
    private String name;
    private String lastName;
    private Date birthDate;
    private List<String> likedPosts;
    private String email;
    private String password;

    public UserModel(String name, String lastName, Date birthDate, List<String> likedPosts, String email, String password) {
        this.name = name;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.likedPosts = likedPosts;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public List<String> getLikedPosts() {
        return likedPosts;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}

