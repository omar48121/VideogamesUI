package com.example.videogames;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface UserApiService {
    @GET("users")
    Call<List<UserModel>> getUsers();

    @FormUrlEncoded
    @POST("users/search")
    Call<UserModel> getByEmail(
            @Field("email") String email
    );

    @FormUrlEncoded
    @POST("users/register")
    Call<Void> registerUser(
            @Field("name") String name,
            @Field("lastName") String lastName,
            @Field("email") String email,
            @Field("birthDate") String birthDate,
            @Field("password") String password
    );
}
