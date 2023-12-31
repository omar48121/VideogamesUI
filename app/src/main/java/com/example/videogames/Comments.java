package com.example.videogames;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Comments extends AppCompatActivity {

    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    TextView textViewCloseComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(this);
        recyclerViewComments.setAdapter(commentAdapter);
        textViewCloseComments = findViewById(R.id.textCloseComments);

        swipeRefreshLayout.setOnRefreshListener(this::getComments);

        String postId = getIntent().getStringExtra("postId");

        getComments();

        FloatingActionButton fabCreatePost = findViewById(R.id.fabCreatePost);
        fabCreatePost.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        fabCreatePost.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Comments.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_comment, null);
            builder.setView(dialogView);

            EditText editTextContent = dialogView.findViewById(R.id.editTextComment);
            editTextContent.requestFocus();

            builder.setPositiveButton("Comentar", (dialog, which) -> {
                String content = editTextContent.getText().toString();
                String email = getLoggedInUserEmail();
                String fullName = getLoggedInUserFullName();

                if (content.isEmpty()) {
                    Toast.makeText(Comments.this, "Ingresa el comentario", Toast.LENGTH_SHORT).show();
                } else {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(ApiConfig.BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    PostApiService apiInterface = retrofit.create(PostApiService.class);

                    Call<Void> call = apiInterface.createComment(email, content, postId, fullName);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                getComments();
                                Toast.makeText(Comments.this, "Comentario creado", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(Comments.this, "Error al crear el comentario", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(Comments.this, "Error en solicitud al server", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                }
            });
            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            negativeButton.setTextAppearance(R.style.NegativeButtonTextStyle);
        });

        textViewCloseComments.setOnClickListener(v -> {
            finish();
        });
    }

    private void getComments() {
        String postId = getIntent().getStringExtra("postId");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PostApiService postApiService = retrofit.create(PostApiService.class);

        Call<List<Comment>> call = postApiService.getByDate(postId);
        call.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful()) {
                    swipeRefreshLayout.setRefreshing(false);

                    List<Comment> comments = response.body();
                    commentAdapter.setComments(comments);
                    commentAdapter.notifyDataSetChanged();

                    TextView textViewNoComments = findViewById(R.id.textViewNoComments);
                    ImageView marioImage = findViewById(R.id.marioImage);
                    if (comments.isEmpty()) {
                        marioImage.setVisibility(View.VISIBLE);
                        textViewNoComments.setVisibility(View.VISIBLE);
                    } else {
                        textViewNoComments.setVisibility(View.GONE);
                        marioImage.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(Comments.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                Toast.makeText(Comments.this, "(getExampleP) Fallo en la solicitud al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getLoggedInUserEmail() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString("email", "");
    }

    public String getLoggedInUserFullName() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString("fullName", "");
    }
}