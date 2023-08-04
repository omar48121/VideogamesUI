package com.example.videogames;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

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

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;
    private Context context;

    public CommentAdapter(Context context) {
        this.context = context;
        this.comments = new ArrayList<>();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        String friendlyTime = getFriendlyTime(comment.getDate());
        holder.textViewAvatar.setText(String.valueOf(comment.getUserEmail().charAt(0)).toUpperCase());
        holder.textViewName.setText(comment.getUserEmail());
        //holder.textViewDate.setText(comment.getDate());
        holder.textViewText.setText(comment.getText());
        holder.textViewDate.setText(friendlyTime);

        holder.textViewAvatar.setOnClickListener(v -> showUserProfile(v, comment));

        holder.textViewName.setOnClickListener(v -> showUserProfile(v, comment));
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView textViewAvatar;
        TextView textViewName;
        TextView textViewText;
        TextView textViewDate;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAvatar = itemView.findViewById(R.id.textViewAvatar);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewText = itemView.findViewById(R.id.textViewContent);
            textViewDate = itemView.findViewById(R.id.textViewDate);
        }
    }

    private void showUserProfile(View v, Comment comment) {
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
        Call<UserModel> call = userApiService.getByEmail(comment.getUserEmail());

        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(new Gson().toJson(response.body()));
                        String fullName = jsonObject.getString("name") + " " + jsonObject.getString("lastName");
                        String email = jsonObject.getString("email");
                        String birthDate = jsonObject.getString("birthDate");

                        textViewAvatar.setText(String.valueOf(comment.getUserEmail().charAt(0)).toUpperCase());
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

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
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
