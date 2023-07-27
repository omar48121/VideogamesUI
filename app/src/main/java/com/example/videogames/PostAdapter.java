package com.example.videogames;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private Context context;
    private List<String> likedPosts;
    private SharedPreferences sharedPreferences;

    public PostAdapter(Context context, SharedPreferences sharedPreferences) {
        this.context = context;
        this.posts = new ArrayList<>();
        this.sharedPreferences = sharedPreferences;
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
        holder.textViewAvatar.setText(String.valueOf(post.getUserEmail().charAt(0)));
        holder.textViewName.setText(post.getUserEmail());
        holder.textViewContent.setText(post.getContent());
        holder.textViewDate.setText(post.getDate());
        //holder.textViewDate.setText(friendlyTime);
        holder.textViewLikes.setText(String.valueOf(post.getLikes()));
        Glide.with(holder.itemView)
                .load(post.getImageUrl()) // Reemplaza post.getImageUrl() con la URL de la imagen del post
                .into(holder.imageViewPost);
        // set icons on load
        boolean isLiked = isPostLikedByUser(post, likedPosts);
        if (isLiked) {
            holder.buttonHeart.setBackgroundResource(R.drawable.ic_heart_full);
        } else {
            holder.buttonHeart.setBackgroundResource(R.drawable.ic_heart_empty);
        }

        // update icons on click
        holder.buttonHeart.setOnClickListener(v -> {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:3000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            PostApiService apiInterface = retrofit.create(PostApiService.class);
            Call<Void> call;

            if (isLiked) {
                call = apiInterface.decreaseCounter(getLoggedInUserEmail(), post.getDate());
                Toast.makeText(context, "already liked", Toast.LENGTH_SHORT).show();
                holder.buttonHeart.setBackgroundResource(R.drawable.ic_heart_empty);
            } else {
                call = apiInterface.increaseCounter(getLoggedInUserEmail(), post.getDate());
                Toast.makeText(context, "no liked", Toast.LENGTH_SHORT).show();
                holder.buttonHeart.setBackgroundResource(R.drawable.ic_heart_full);
            }

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(v.getContext(), "respuesta exitosa", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(v.getContext(), "respuesta fallida", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(v.getContext(), "Fallo en req al server", Toast.LENGTH_SHORT).show();
                }
            });
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
        }
    }

    private String getFriendlyTime(String dateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date = sdf.parse(dateTime);

            long timeMillis = date.getTime();
            long currentTimeMillis = System.currentTimeMillis();
            long diffMillis = currentTimeMillis - timeMillis;

            // Calcular la diferencia de tiempo en minutos, horas y días
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
