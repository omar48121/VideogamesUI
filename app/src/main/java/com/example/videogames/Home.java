package com.example.videogames;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Home extends AppCompatActivity {

    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar el adaptador sin datos
        postAdapter = new PostAdapter(this);
        recyclerViewPosts.setAdapter(postAdapter);

        // Configurar el SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Lógica para actualizar la lista de posts
                // Aquí puedes realizar una nueva solicitud al servidor para obtener los últimos posts
                // y luego actualizar la lista en postAdapter.setPosts(nuevosPosts);
                // Por ejemplo, postAdapter.setPosts(nuevosPosts); y luego postAdapter.notifyDataSetChanged();
                // En este ejemplo, simplemente detenemos el SwipeRefreshLayout después de 2 segundos.
                swipeRefreshLayout.setRefreshing(false);

                // Llamar a la función para obtener los posts
                getExamplePosts();
            }
        });

        FloatingActionButton fabCreatePost = findViewById(R.id.fabCreatePost);
        fabCreatePost.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        fabCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para crear un nuevo post
                Toast.makeText(Home.this, "Crear un nuevo post", Toast.LENGTH_SHORT).show();
            }
        });

        // Llamar a la función para obtener los posts
        getExamplePosts();
    }

    private void getExamplePosts() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PostApiService postApiService = retrofit.create(PostApiService.class);

        Call<List<Post>> call = postApiService.getPosts();
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful()) {
                    List<Post> posts = response.body();
                    if (posts != null) {
                        postAdapter.setPosts(posts);
                        postAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(Home.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Toast.makeText(Home.this, "Fallo en la solicitud al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}



/*List<Post> posts = new ArrayList<>();
        posts.add(new Post("Omar Rodríguez", "Contenido del post número dos", "https://loremflickr.com/360/640", "Hace un momento"));
        posts.add(new Post("Agustín Jaime", "Contenido del post 1", "https://loremflickr.com/640/360", "Hace un momento"));
        posts.add(new Post("Omar Rodríguez", "Esta semana estará disponible en PlayStation 5 el nuevo juego de sony", "https://loremflickr.com/1920/1200", "Hace 3 minutos"));
        posts.add(new Post("Dany Dorianth", "Contenido del post 3", "https://images.unsplash.com/photo-1493711662062-fa541adb3fc8?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=870&q=80", "Hace una hora"));
        // Agregar más posts aquí...
        return posts;*/