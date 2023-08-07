package com.example.videogames;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class addgames extends AppCompatActivity {
    Toolbar Menu;
    Button cancelar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addgames);

       // Menu = findViewById(R.id.menugames);

        cancelar = findViewById(R.id.btncancelar);

        setSupportActionBar(Menu);

        cancelar.setOnClickListener(v -> {
            Intent intent = new Intent(this, menugames.class);
            startActivity(intent);
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_cerrarsession) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            // Realiza acciones cuando se seleccione el ítem del menú
            return true;
        }
        if (id == R.id.menu_videogame) {
            Intent intent = new Intent(this, Home.class);
            startActivity(intent);
            // Realiza acciones cuando se seleccione el ítem del menú
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}