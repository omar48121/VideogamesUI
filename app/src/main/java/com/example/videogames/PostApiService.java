package com.example.videogames;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface PostApiService {
    @GET("posts")
    Call<List<Post>> getPosts();
}
