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

public class CrearListaActivity extends AppCompatActivity {

    public static final String EXTRA_TIPO_LISTA = "tipo_lista";
    
    private EditText etNombre;
    private EditText etDescripcion;
    private String tipoLista;
    private TextView tvTitulo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_lista);

        // Obtener el tipo de lista del Intent
        tipoLista = getIntent().getStringExtra(EXTRA_TIPO_LISTA);
        if (tipoLista == null) {
            tipoLista = ListasActivity.TIPO_COMPRA; // Por defecto lista de compra
        }

        // Referencias a las vistas
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        tvTitulo = findViewById(R.id.tvTitulo);
        Button btnGuardar = findViewById(R.id.btnGuardar);
        ImageButton btnAtras = findViewById(R.id.btnAtras);
        ImageButton btnCancelar = findViewById(R.id.btnCancelar);

        // Configurar el título según el tipo
        if (tipoLista.equals(ListasActivity.TIPO_INVENTARIO)) {
            tvTitulo.setText("Crear Inventario");
        } else {
            tvTitulo.setText("Crear Lista");
        }

        // Configurar botones de navegación
        btnAtras.setOnClickListener(v -> finish());
        btnCancelar.setOnClickListener(v -> finish());

        // Configurar botón de guardar
        btnGuardar.setOnClickListener(v -> guardarLista());

        // TODO: Implementar selección de imagen
        findViewById(R.id.cardAnadirFoto).setOnClickListener(v -> {
            Toast.makeText(this, "Seleccionar imagen (próximamente)", Toast.LENGTH_SHORT).show();
        });
    }

    private void guardarLista() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        // Validar que el nombre no esté vacío
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Por favor, introduce un nombre", Toast.LENGTH_SHORT).show();
            etNombre.requestFocus();
            return;
        }

        // Crear la nueva lista
        Lista nuevaLista = new Lista();
        nuevaLista.nombre = nombre;
        nuevaLista.descripcion = descripcion.isEmpty() ? null : descripcion;
        nuevaLista.tipo = tipoLista;
        nuevaLista.imagen = null; // TODO: Implementar cuando se añada selección de imagen

        // Guardar en la base de datos
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            long id = db.listaDao().insertar(nuevaLista);
            
            runOnUiThread(() -> {
                if (id > 0) {
                    Toast.makeText(this, "Lista creada correctamente", Toast.LENGTH_SHORT).show();
                    
                    // Volver a la pantalla de listas con el resultado OK
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Error al crear la lista", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}
