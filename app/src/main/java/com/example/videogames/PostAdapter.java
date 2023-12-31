package com.example.videogames;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private Context context;
    private Home homeActivity;
    private List<String> likedPosts;
    private SharedPreferences sharedPreferences;

    public PostAdapter(Context context, SharedPreferences sharedPreferences, Home homeActivity) {
        this.context = context;
        this.posts = new ArrayList<>();
        this.sharedPreferences = sharedPreferences;
        this.homeActivity = homeActivity;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        String friendlyTime = getFriendlyTime(post.getDate());
        holder.textViewDate.setText(friendlyTime);
        holder.textViewAvatar.setText(String.valueOf(post.getUserEmail().charAt(0)).toUpperCase());
        holder.textViewName.setText(post.getUserFullName());
        holder.textViewContent.setText(post.getContent());
        //holder.textViewDate.setText(post.getDate());
        holder.textViewLikes.setText(String.valueOf(post.getLikes()));
        holder.buttonHeart.setEnabled(!post.isLikesButtonDisabled());
        Glide.with(holder.itemView)
                .load(post.getImageUrl())
                .into(holder.imageViewPost);

        final boolean[] isLiked = {isPostLikedByUser(post, likedPosts)};
        final int[] likesCount = {post.getLikes()};
        if (isLiked[0]) {
            holder.buttonHeart.setBackgroundResource(R.drawable.ic_heart_full);
        } else {
            holder.buttonHeart.setBackgroundResource(R.drawable.ic_heart_empty);
        }

        if (Objects.equals(post.getUserEmail(), getLoggedInUserEmail())) {
            holder.buttonEdit.setVisibility(View.VISIBLE);
        } else {
            holder.buttonEdit.setVisibility(View.GONE);
        }

        holder.buttonEdit.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(context, android.R.style.Theme_Material_NoActionBar);
            dialog.setContentView(R.layout.dialog_edit_post);

            TextView content = dialog.findViewById(R.id.editPostContent);
            ImageView image = dialog.findViewById(R.id.imagePreviewEditor);
            FloatingActionButton fabDone = dialog.findViewById(R.id.fabDone);
            Button buttonDeletePost = dialog.findViewById(R.id.buttonDeletePost);

            content.setText(post.getContent());
            Glide.with(context)
                    .load(post.getImageUrl())
                    .into(image);
            
            buttonDeletePost.setOnClickListener(v2 -> {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(ApiConfig.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                PostApiService apiInterface = retrofit.create(PostApiService.class);
                Call<Void> call = apiInterface.deletePost(post.getDate());

                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Toast.makeText(v.getContext(), "Publicación eliminada", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        homeActivity.getExamplePosts();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(v.getContext(), "No se pudo eliminar", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            fabDone.setOnClickListener(v1 -> {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(ApiConfig.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                PostApiService apiInterface = retrofit.create(PostApiService.class);
                Call<Void> call = apiInterface.updatePost(post.getDate(), content.getText().toString());

                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(v.getContext(), "Publicación actualizada", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                            if (homeActivity != null) {
                                homeActivity.getExamplePosts();
                            }
                        } else {
                            Toast.makeText(v.getContext(), "Ocurrió un error al guardar", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(v.getContext(), "Error del servidor", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            TextView textClose = dialog.findViewById(R.id.textCloseEditor);
            textClose.setOnClickListener(v1 -> dialog.dismiss());

            dialog.show();
        });

        holder.buttonHeart.setOnClickListener(v -> {
            if (!post.isLikesButtonDisabled()) {
                post.setLikesButtonDisabled(true);
                holder.buttonHeart.setEnabled(false);

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(ApiConfig.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                PostApiService apiInterface = retrofit.create(PostApiService.class);
                Call<Void> call;

                if (isLiked[0]) {
                    call = apiInterface.decreaseCounter(getLoggedInUserEmail(), post.getDate());
                } else {
                    call = apiInterface.increaseCounter(getLoggedInUserEmail(), post.getDate());
                }

                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        post.setLikesButtonDisabled(false);
                        holder.buttonHeart.setEnabled(true);

                        if (response.isSuccessful()) {
                            if (isLiked[0]) {
                                holder.buttonHeart.setBackgroundResource(R.drawable.ic_heart_empty);
                                holder.textViewLikes.setText(String.valueOf(likesCount[0] - 1));
                                likesCount[0]--;
                            } else {
                                holder.buttonHeart.setBackgroundResource(R.drawable.ic_heart_full);
                                holder.textViewLikes.setText(String.valueOf(likesCount[0] + 1));
                                likesCount[0]++;
                            }
                            isLiked[0] = !isLiked[0];
                        } else {
                            Toast.makeText(v.getContext(), "Error al actualizar me gusta", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        post.setLikesButtonDisabled(false);
                        holder.buttonHeart.setEnabled(true);
                        Toast.makeText(v.getContext(), "Fallo en req al server", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        holder.imageViewPost.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.dialog_image);

            ImageView imageViewDialog = dialog.findViewById(R.id.imageViewDialog);

            Glide.with(context)
                    .load(post.getImageUrl())
                    .into(imageViewDialog);

            imageViewDialog.setOnClickListener(v1 -> dialog.dismiss());

            dialog.show();
        });

        holder.textViewAvatar.setOnClickListener(v -> showUserProfile(v, post));

        holder.textViewName.setOnClickListener(v -> showUserProfile(v, post));

        holder.buttonComment.setOnClickListener(v -> {
            Intent intent = new Intent(context, Comments.class);
            intent.putExtra("postId", post.getDate());
            context.startActivity(intent);
        });
    }

    private void showUserProfile(View v, Post post) {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Material_NoActionBar);
        dialog.setContentView(R.layout.dialog_profile);

        TextView textViewFullName = dialog.findViewById(R.id.textViewFullname);
        TextView textViewEmail = dialog.findViewById(R.id.textViewEmail);
        TextView textViewBirthDate = dialog.findViewById(R.id.textViewBirthDate);
        TextView textViewAvatar = dialog.findViewById(R.id.textViewAvatar);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UserApiService userApiService = retrofit.create(UserApiService.class);
        Call<UserModel> call = userApiService.getByEmail(post.getUserEmail());

        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(new Gson().toJson(response.body()));
                        String fullName = jsonObject.getString("name") + " " + jsonObject.getString("lastName");
                        String email = jsonObject.getString("email");
                        String birthDate = jsonObject.getString("birthDate");

                        textViewAvatar.setText(String.valueOf(post.getUserEmail().charAt(0)).toUpperCase());
                        textViewFullName.setText(fullName);
                        textViewEmail.setText("Correo: " + email);
                        textViewBirthDate.setText("Fecha de nacimiento: " + birthDate.substring(0, 12));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Toast.makeText(v.getContext(), "Error del servidor", Toast.LENGTH_SHORT).show();
            }
        });

        TextView textClose = dialog.findViewById(R.id.textClose);
        textClose.setOnClickListener(v1 -> dialog.dismiss());

        dialog.show();
    }

    private String getLoggedInUserEmail() {
        return sharedPreferences.getString("email", "");
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public void setLikedPosts(List<String> likedPosts) {
        this.likedPosts = likedPosts;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textViewAvatar;
        TextView textViewName;
        TextView textViewContent;
        TextView textViewDate;
        ImageView imageViewPost;
        ImageButton buttonHeart;
        ImageButton buttonComment;
        ImageButton buttonEdit;
        TextView textViewLikes;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAvatar = itemView.findViewById(R.id.textViewAvatar);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewLikes = itemView.findViewById(R.id.textViewLikes);
            imageViewPost = itemView.findViewById(R.id.imageViewPost);
            buttonHeart = itemView.findViewById(R.id.buttonHeart);
            buttonComment = itemView.findViewById(R.id.buttonComment);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
        }
    }

    private String getFriendlyTime(String dateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date = sdf.parse(dateTime);

            long timeMillis = date.getTime();
            long currentTimeMillis = System.currentTimeMillis();
            long diffMillis = currentTimeMillis - timeMillis;

            int minutes = (int) (diffMillis / (60 * 1000));
            int hours = (int) (diffMillis / (60 * 60 * 1000));
            int days = (int) (diffMillis / (24 * 60 * 60 * 1000));

            if (minutes < 1) {
                return "Hace un momento";
            } else if (minutes < 60) {
                return "Hace " + minutes + " minutos";
            } else if (hours < 24) {
                return "Hace " + hours + " horas";
            } else {
                return "Hace " + days + " días";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    private boolean isPostLikedByUser(Post post, List<String> likedPosts) {
        return likedPosts.contains(post.getDate());
    }
}
