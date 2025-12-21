package com.example.codepractica;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.codepractica.database.AppDatabase;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        AppDatabase db = AppDatabase.getInstance(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Thread(() -> {
            // Esto obliga a Room a abrir el archivo y crear las tablas
            db.listaDao().obtenerTodas();
            Log.d("DB_TEST", "Base de datos inicializada");
        }).start();

        // Configurar botón de salir
        Button btnSalir = findViewById(R.id.btnSalir);
        btnSalir.setOnClickListener(v -> {
            finishAffinity(); // Cierra la aplicación y todas sus actividades
        });
    }
}