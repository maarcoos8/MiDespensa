package com.example.codepractica;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.codepractica.database.AppDatabase;
import com.example.codepractica.database.DatabaseInitializer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Eliminar la base de datos para forzar la reinicialización
         this.deleteDatabase("despensa_db");

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
            
            // Inicializar datos de ejemplo si la base de datos está vacía
            DatabaseInitializer.inicializarDatosEjemplo(this);                                                                                            
        }).start();

        // Configurar botón de salir
        Button btnSalir = findViewById(R.id.btnSalir);
        btnSalir.setOnClickListener(v -> {
            finishAffinity(); // Cierra la aplicación y todas sus activZidades
        });

        // Configurar botón de ver productos
        Button btnVerProductos = findViewById(R.id.btnVerProductos);
        btnVerProductos.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProductosActivity.class);
            startActivity(intent);
        });

        // Configurar botón de ver inventarios
        Button btnVerInventarios = findViewById(R.id.btnVerInventarios);
        btnVerInventarios.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListasActivity.class);
            intent.putExtra(ListasActivity.EXTRA_TIPO_LISTA, ListasActivity.TIPO_INVENTARIO);
            startActivity(intent);
        });

        // Configurar botón de ver listas de compra
        Button btnVerListas = findViewById(R.id.btnVerListas);
        btnVerListas.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListasActivity.class);
            intent.putExtra(ListasActivity.EXTRA_TIPO_LISTA, ListasActivity.TIPO_COMPRA);
            startActivity(intent);
        });

        // Configurar botón de recordatorio
        Button btnRecordatorio = findViewById(R.id.btnRecordatorio);
        btnRecordatorio.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RecordatorioActivity.class);
            startActivity(intent);
        });
    }
}