package com.example.videogames;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class addgames extends AppCompatActivity {

    Button cancelar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addgames);
        cancelar = findViewById(R.id.btncancelar);

        cancelar.setOnClickListener(v -> {
            Intent intent = new Intent(this, menugames.class);
            startActivity(intent);
        });


    }
}