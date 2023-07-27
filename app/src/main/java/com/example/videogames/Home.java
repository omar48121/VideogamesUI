package com.example.videogames;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Home extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Uri selectedImageUri;
    ActivityResultLauncher<Intent> pickPhotoLauncher;
    Button buttonUploadImage;
    ImageView previewImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        postAdapter = new PostAdapter(this, sharedPreferences);
        recyclerViewPosts.setAdapter(postAdapter);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        pickPhotoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        selectedImageUri = data.getData();

                        if (previewImage != null) {
                            previewImage.setVisibility(View.VISIBLE);
                            previewImage.setImageURI(selectedImageUri);
                        } else {
                            Toast.makeText(this, "Error al cargar imagen (null)", Toast.LENGTH_SHORT).show();
                        }

                        buttonUploadImage.setVisibility(View.GONE);
                    }
                });

        /* swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                getExamplePosts();
            }
        }); */

        FloatingActionButton fabCreatePost = findViewById(R.id.fabCreatePost);
        fabCreatePost.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        fabCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedImageUri = null;
                AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_post, null);
                builder.setView(dialogView);

                EditText editTextContent = dialogView.findViewById(R.id.editTextContent);
                buttonUploadImage = dialogView.findViewById(R.id.buttonUploadImage);
                previewImage = dialogView.findViewById(R.id.imagePreviewX);

                buttonUploadImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final CharSequence[] options = {"Galería", "Cancelar"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                        builder.setTitle("Seleccionar una opción");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (options[item].equals("si")) {
                                    //dispatchTakePictureIntent();
                                } else if (options[item].equals("Galería")) {
                                    Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    pickPhotoLauncher.launch(pickPhotoIntent);
                                } else if (options[item].equals("Cancelar")) {
                                    dialog.dismiss();
                                }
                            }
                        });
                        builder.show();
                    }
                });

                builder.setPositiveButton("Publicar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String content = editTextContent.getText().toString();
                        String email = getLoggedInUserEmail();
                        String imageUrl;

                        if (selectedImageUri != null) {
                            imageUrl = selectedImageUri.toString();
                        } else {
                            imageUrl = "";
                        }

                        if (content.isEmpty()) {
                            Toast.makeText(Home.this, "Ingresa el texto de la publicación", Toast.LENGTH_SHORT).show();
                        } else if (imageUrl.isEmpty()) {
                            Toast.makeText(Home.this, "Selecciona una imagen", Toast.LENGTH_SHORT).show();
                        } else {
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
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(Home.this, "Error al crear el post", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(Home.this, "Error en solicitud al server", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            });
                        }
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
                        String userEmail = getLoggedInUserEmail();
                        getLikedPosts(userEmail, posts);
                    }
                } else {
                    Toast.makeText(Home.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Toast.makeText(Home.this, "(getExampleP) Fallo en la solicitud al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLikedPosts(String userEmail, List<Post> posts) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UserApiService userApiService = retrofit.create(UserApiService.class);

        Call<List<UserModel>> call = userApiService.getUsers();
        call.enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful()) {
                    List<UserModel> users = response.body();
                    if (users != null) {
                        // Buscar el usuario que coincida con el correo electrónico
                        List<String> likedPosts = null;
                        for (UserModel user : users) {
                            if (user.getEmail().equals(userEmail)) {
                                likedPosts = user.getLikedPosts();
                                break;
                            }
                        }

                        // Verificar que likedPosts no sea nulo antes de pasarlo al adaptador
                        if (likedPosts != null) {
                            postAdapter.setPosts(posts);
                            postAdapter.setLikedPosts(likedPosts);
                            postAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    Toast.makeText(Home.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Toast.makeText(Home.this, "(getLikedP) Fallo en la solicitud al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getLoggedInUserEmail() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString("email", "");
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    /*private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(Home.this, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            selectedImageUri = getImageUri(Home.this, imageBitmap);

            if (previewImage != null) {
                previewImage.setVisibility(View.VISIBLE);
                previewImage.setImageURI(selectedImageUri);
            } else {
                Toast.makeText(this, "Error al cargar imagen (null)", Toast.LENGTH_SHORT).show();
            }

            buttonUploadImage.setVisibility(View.GONE);
        }
    }
}