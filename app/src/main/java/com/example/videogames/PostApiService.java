package com.example.videogames;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface PostApiService {
    @GET("posts")
    Call<List<Post>> getPosts();

    @FormUrlEncoded
    @POST("comments")
    Call<Void> createComment(
            @Field("userEmail") String email,
            @Field("text") String content,
            @Field("postId") String imageUrl,
            @Field("userFullName") String fullName
    );

    @FormUrlEncoded
    @POST("comments/search")
    Call<List<Comment>> getByDate(
            @Field("postId") String postId
    );

    @FormUrlEncoded
    @POST("posts")
    Call<Void> createPost(
            @Field("content") String content,
            @Field("userEmail") String email,
            @Field("imageUrl") String imageUrl,
            @Field("userFullName") String userFullName
    );

    @FormUrlEncoded
    @PUT("posts")
    Call<Void> updatePost(
            @Field("postId") String postId,
            @Field("content") String content
    );

    @FormUrlEncoded
    @POST("posts/remove")
    Call<Void> deletePost(
            @Field("date") String postId
    );

    @FormUrlEncoded
    @POST("posts/like")
    Call<Void> increaseCounter(
            @Field("userEmail") String userEmail,
            @Field("postId") String postId
    );

    @FormUrlEncoded
    @POST("posts/dislike")
    Call<Void> decreaseCounter(
            @Field("userEmail") String userEmail,
            @Field("postId") String postId
    );
}
