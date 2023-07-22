package com.example.videogames;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;

        import androidx.annotation.NonNull;
        import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private Context context;

    public PostAdapter(Context context) {
        this.context = context;
        this.posts = new ArrayList<>();
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
        //String friendlyTime = getFriendlyTime(post.getDate());
        holder.textViewAvatar.setText(String.valueOf(post.getUserEmail().charAt(0)));
        holder.textViewName.setText(post.getUserEmail());
        holder.textViewContent.setText(post.getContent());
        holder.textViewDate.setText(post.getDate());
        //holder.textViewDate.setText(friendlyTime);
        Glide.with(holder.itemView)
                .load(post.getImageUrl()) // Reemplaza post.getImageUrl() con la URL de la imagen del post
                .into(holder.imageViewPost);

        holder.imageViewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                dialog.setContentView(R.layout.dialog_image);

                ImageView imageViewDialog = dialog.findViewById(R.id.imageViewDialog);

                Glide.with(context)
                        .load(post.getImageUrl())
                        .into(imageViewDialog);

                imageViewDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        TextView textViewAvatar;
        TextView textViewName;
        TextView textViewContent;
        TextView textViewDate;
        ImageView imageViewPost;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAvatar = itemView.findViewById(R.id.textViewAvatar);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            imageViewPost = itemView.findViewById(R.id.imageViewPost);
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

}
