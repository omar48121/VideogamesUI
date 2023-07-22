package com.example.videogames;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    private Uri selectedImageUri;

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
                swipeRefreshLayout.setRefreshing(false);
                getExamplePosts();
            }
        });

        FloatingActionButton fabCreatePost = findViewById(R.id.fabCreatePost);
        fabCreatePost.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        fabCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para crear un nuevo post
                // Crear el diálogo personalizado
                AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_post, null);
                builder.setView(dialogView);

                // Obtener referencias a los elementos del diálogo
                EditText editTextContent = dialogView.findViewById(R.id.editTextContent);
                Button buttonUploadImage = dialogView.findViewById(R.id.buttonUploadImage);

                // Configurar el botón de cargar imagen (si es necesario)
                buttonUploadImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                // Configurar los botones del diálogo
                builder.setPositiveButton("Publicar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String content = editTextContent.getText().toString();
                        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                        String email = sharedPreferences.getString("email", "");
                        String imageUrl;

                        if (selectedImageUri != null) {
                            imageUrl = selectedImageUri.toString();
                        } else {
                            imageUrl = "https://loremflickr.com/360/650";
                        }

                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("http://10.0.2.2:3000/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        PostApiService apiInterface = retrofit.create(PostApiService.class);

                        Call<Void> call = apiInterface.createPost(content, email, imageUrl);
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    getExamplePosts();
                                    Toast.makeText(Home.this, "Post creado", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Home.this, "Post no creado", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(Home.this, "Error en solicitud al server", Toast.LENGTH_SHORT).show();
                            }
                        });

                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                // Mostrar el diálogo
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                negativeButton.setTextAppearance(R.style.NegativeButtonTextStyle);
            }
        });

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