package com.example.codepractica;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.codepractica.database.AppDatabase;
import com.example.codepractica.database.entities.Lista;
import com.example.codepractica.utils.ImageHelper;

public class EditarListaActivity extends AppCompatActivity {

    public static final String EXTRA_LISTA_ID = "lista_id";
    
    private EditText etNombre;
    private EditText etDescripcion;
    private TextView tvTitulo;
    private int listaId;
    private Lista lista;
    private CardView cardAnadirFoto;
    private ImageView ivImagenLista;
    private LinearLayout layoutPlaceholder;
    private String imagenPath = null;
    
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private ImageHelper imageHelper;

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

        inicializarLaunchers();
        
        // Referencias a las vistas
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        tvTitulo = findViewById(R.id.tvTitulo);
        cardAnadirFoto = findViewById(R.id.cardAnadirFoto);
        ivImagenLista = findViewById(R.id.ivImagenLista);
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder);
        Button btnGuardar = findViewById(R.id.btnGuardar);
        ImageButton btnAtras = findViewById(R.id.btnAtras);

        // Cambiar título a "Editar"
        tvTitulo.setText("Editar Lista");

        // Cargar datos de la lista
        cargarDatosLista();

        // Configurar botones de navegación
        btnAtras.setOnClickListener(v -> finish());

        // Configurar botón de guardar
        btnGuardar.setOnClickListener(v -> guardarCambios());

        // Click en la foto
        cardAnadirFoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            } else {
                imageHelper.showImageSourceDialog(imagenPath != null);
            }
        });
    }
    
    private void inicializarLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        imageHelper.handleCameraResult();
                    }
                });
        
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageHelper.handleGalleryResult(result.getData());
                    }
                });
        
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        imageHelper.showImageSourceDialog(imagenPath != null);
                    } else {
                        Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
                    }
                });
        
        imageHelper = new ImageHelper(this, cameraLauncher, galleryLauncher, permissionLauncher,
                new ImageHelper.ImageSelectionCallback() {
                    @Override
                    public void onImageSelected(String imagePath) {
                        imagenPath = imagePath;
                        mostrarImagen(imagePath);
                    }

                    @Override
                    public void onImageDeleted() {
                        imagenPath = null;
                        ocultarImagen();
                    }
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
                    
                    // Cargar imagen si existe
                    if (lista.imagen != null && !lista.imagen.isEmpty()) {
                        imagenPath = lista.imagen;
                        mostrarImagen(imagenPath);
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
        lista.imagen = imagenPath; // Actualizar imagen

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
    
    private void mostrarImagen(String imagePath) {
        ivImagenLista.setVisibility(View.VISIBLE);
        layoutPlaceholder.setVisibility(View.GONE);
        
        if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
            ivImagenLista.setImageURI(Uri.parse(imagePath));
        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                ivImagenLista.setImageBitmap(bitmap);
            }
        }
    }
    
    private void ocultarImagen() {
        ivImagenLista.setVisibility(View.GONE);
        layoutPlaceholder.setVisibility(View.VISIBLE);
        ivImagenLista.setImageDrawable(null);
    }
}
