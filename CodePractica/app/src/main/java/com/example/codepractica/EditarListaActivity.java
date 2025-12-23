package com.example.codepractica;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.codepractica.database.AppDatabase;
import com.example.codepractica.database.entities.Lista;

public class EditarListaActivity extends AppCompatActivity {

    public static final String EXTRA_LISTA_ID = "lista_id";
    
    private EditText etNombre;
    private EditText etDescripcion;
    private TextView tvTitulo;
    private int listaId;
    private Lista lista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_lista);

        // Obtener el ID de la lista del Intent
        listaId = getIntent().getIntExtra(EXTRA_LISTA_ID, -1);
        if (listaId == -1) {
            Toast.makeText(this, "Error al cargar la lista", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Referencias a las vistas
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        tvTitulo = findViewById(R.id.tvTitulo);
        Button btnGuardar = findViewById(R.id.btnGuardar);
        ImageButton btnAtras = findViewById(R.id.btnAtras);
        ImageButton btnCancelar = findViewById(R.id.btnCancelar);

        // Cambiar título a "Editar"
        tvTitulo.setText("Editar Lista");

        // Cargar datos de la lista
        cargarDatosLista();

        // Configurar botones de navegación
        btnAtras.setOnClickListener(v -> finish());
        btnCancelar.setOnClickListener(v -> finish());

        // Configurar botón de guardar
        btnGuardar.setOnClickListener(v -> guardarCambios());

        // TODO: Implementar selección de imagen
        findViewById(R.id.cardAnadirFoto).setOnClickListener(v -> {
            Toast.makeText(this, "Seleccionar imagen (próximamente)", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarDatosLista() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            lista = db.listaDao().obtenerTodas().stream()
                    .filter(l -> l.id == listaId)
                    .findFirst()
                    .orElse(null);

            if (lista != null) {
                runOnUiThread(() -> {
                    etNombre.setText(lista.nombre);
                    if (lista.descripcion != null) {
                        etDescripcion.setText(lista.descripcion);
                    }
                    
                    // Actualizar título según el tipo
                    if (ListasActivity.TIPO_INVENTARIO.equals(lista.tipo)) {
                        tvTitulo.setText("Editar Inventario");
                    } else {
                        tvTitulo.setText("Editar Lista");
                    }
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lista no encontrada", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void guardarCambios() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        // Validar que el nombre no esté vacío
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Por favor, introduce un nombre", Toast.LENGTH_SHORT).show();
            etNombre.requestFocus();
            return;
        }

        // Actualizar la lista
        lista.nombre = nombre;
        lista.descripcion = descripcion.isEmpty() ? null : descripcion;
        // lista.imagen se mantiene igual por ahora

        // Guardar en la base de datos
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.listaDao().actualizar(lista);
            
            runOnUiThread(() -> {
                Toast.makeText(this, "Lista actualizada correctamente", Toast.LENGTH_SHORT).show();
                
                // Volver a la pantalla anterior con el resultado OK
                setResult(RESULT_OK);
                finish();
            });
        }).start();
    }
}
