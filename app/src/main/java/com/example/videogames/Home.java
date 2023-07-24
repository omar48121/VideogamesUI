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
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
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

        // Inicializar el adaptador sin datos
        postAdapter = new PostAdapter(this);
        recyclerViewPosts.setAdapter(postAdapter);

        // Verificar si ya se tiene el permiso de la cámara
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
        }
        // Verificar si el permiso ya ha sido concedido
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

                        // Ocultar el botón "Seleccionar imagen"
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

                // Obtener referencias a los elementos del diálogo
                EditText editTextContent = dialogView.findViewById(R.id.editTextContent);
                buttonUploadImage = dialogView.findViewById(R.id.buttonUploadImage);
                previewImage = dialogView.findViewById(R.id.imagePreviewX);

                // Configurar el botón de cargar imagen (si es necesario)
                buttonUploadImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Crear un arreglo de opciones para el diálogo
                        final CharSequence[] options = {"Cámara", "Galería", "Cancelar"};

                        // Crear el diálogo de opciones
                        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                        builder.setTitle("Seleccionar una opción");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (options[item].equals("Cámara")) {
                                    dispatchTakePictureIntent();
                                } else if (options[item].equals("Galería")) {
                                    // Abrir la galería para seleccionar una imagen
                                    Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    pickPhotoLauncher.launch(pickPhotoIntent);
                                } else if (options[item].equals("Cancelar")) {
                                    // Cerrar el diálogo sin hacer nada
                                    dialog.dismiss();
                                }
                            }
                        });
                        builder.show();
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

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(Home.this, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // La foto fue tomada con éxito, aquí puedes obtener la imagen
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