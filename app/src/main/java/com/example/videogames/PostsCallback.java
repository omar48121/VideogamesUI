package com.example.videogames;

import java.util.List;

public interface PostsCallback {
    void onPostsLoaded(List<Post> posts);
    void onFailure();
}
