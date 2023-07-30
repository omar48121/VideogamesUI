package com.example.videogames;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Register extends AppCompatActivity {

    EditText editTextName;
    EditText editTextLastName;
    EditText editTextEmail;
    EditText editTextDate;
    EditText editTextPassword;
    Button buttonConfirmRegister;
    Button buttonBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextName = findViewById(R.id.editTextName);
        editTextLastName = findViewById(R.id.editTextLastname);
        editTextEmail = findViewById(R.id.editTextTextEmail);
        editTextDate = findViewById(R.id.editTextBirthDate);
        editTextPassword = findViewById(R.id.editTextTextPassword);
        buttonBackToLogin = findViewById(R.id.btnBackToLogin);
        buttonConfirmRegister = findViewById(R.id.btnConfirmRegister);

        buttonBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(Register.this, MainActivity.class));
            finish();
        });

        buttonConfirmRegister.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String lastName = editTextLastName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String birthDate = editTextDate.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (name.isEmpty()) {
                editTextName.setError("Ingresa tu nombre");
                return;
            }

            if (lastName.isEmpty()) {
                editTextLastName.setError("Ingresa tu apellido");
                return;
            }

            if (email.isEmpty()) {
                editTextEmail.setError("Ingresa tu correo electrónico");
                return;
            }

            if (password.isEmpty()) {
                editTextPassword.setError("Ingresa tu contraseña");
                return;
            }

            if (password.length() < 4) {
                editTextPassword.setError("Ingresa al menos 4 carácteres");
                return;
            }

            if (birthDate.isEmpty()) {
                editTextDate.setError("Ingresa tu fecha de nacimiento");
                return;
            }

            if (!birthDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                Toast.makeText(Register.this, "El formato de la fecha debe ser yyyy-mm-dd", Toast.LENGTH_SHORT).show();
                return;
            }

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:3000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            UserApiService registerApiService = retrofit.create(UserApiService.class);
            Call<Void> call = registerApiService.registerUser(name, lastName, email, birthDate, password);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(Register.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                        editTextName.setText("");
                        editTextLastName.setText("");
                        editTextEmail.setText("");
                        editTextDate.setText("");
                        editTextPassword.setText("");
                    } else {
                        if (response.code() == 409) {
                            Toast.makeText(Register.this, "El correo ya fue registrado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Register.this, "Error en el servidor", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(Register.this, "Error en la conexión", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}