package com.example.videogames;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface UserApiService {
    @GET("users")
    Call<List<UserModel>> getUsers();
}
