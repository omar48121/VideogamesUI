package com.example.videogames;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface PostApiService {
    @GET("posts")
    Call<List<Post>> getPosts();

    @FormUrlEncoded
    @POST("posts")
    Call<Void> createPost(
            @Field("content") String content,
            @Field("userEmail") String email,
            @Field("imageUrl") String imageUrl
    );
}
