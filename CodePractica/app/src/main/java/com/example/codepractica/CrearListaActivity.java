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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.codepractica.database.AppDatabase;
import com.example.codepractica.database.entities.Lista;
import com.example.codepractica.utils.FormHelper;
import com.example.codepractica.utils.ImageHelper;

public class CrearListaActivity extends AppCompatActivity {

    public static final String EXTRA_TIPO_LISTA = "tipo_lista";
    public static final String EXTRA_LISTA_ID = "lista_id";
    
    private EditText etNombre;
    private EditText etDescripcion;
    private String tipoLista;
    private TextView tvTitulo;
    
    // Variables para modo edición
    private boolean modoEdicion = false;
    private int listaIdEditar = -1;
    private Lista listaOriginal = null;
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

        // Verificar si es modo edición
        listaIdEditar = getIntent().getIntExtra(EXTRA_LISTA_ID, -1);
        modoEdicion = listaIdEditar != -1;

        // Obtener el tipo de lista del Intent
        tipoLista = getIntent().getStringExtra(EXTRA_TIPO_LISTA);
        if (tipoLista == null && !modoEdicion) {
            tipoLista = ListasActivity.TIPO_COMPRA;
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

        // Configurar el título y texto del botón según el modo
        if (modoEdicion) {
            tvTitulo.setText("Editar Lista");
            btnGuardar.setText("Actualizar");
            // Cargar datos de la lista
            cargarDatosLista();
        } else {
            // Configurar el título según el tipo
            if (tipoLista.equals(ListasActivity.TIPO_INVENTARIO)) {
                tvTitulo.setText("Crear Inventario");
            } else {
                tvTitulo.setText("Crear Lista");
            }
            btnGuardar.setText("Guardar");
        }

        // Configurar botones de navegación
        btnAtras.setOnClickListener(v -> FormHelper.mostrarDialogoSalir(this));

        // Configurar botón de guardar
        btnGuardar.setOnClickListener(v -> guardarLista());

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
                        FormHelper.mostrarImagen(CrearListaActivity.this, ivImagenLista, layoutPlaceholder, imagePath);
                    }

                    @Override
                    public void onImageDeleted() {
                        imagenPath = null;
                        FormHelper.ocultarImagen(ivImagenLista, layoutPlaceholder);
                    }
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

        Lista lista;
        if (modoEdicion) {
            // Modo edición: usar la lista original y actualizar sus valores
            lista = listaOriginal;
            lista.nombre = nombre;
            lista.descripcion = descripcion.isEmpty() ? null : descripcion;
            lista.imagen = imagenPath;
        } else {
            // Modo creación: crear nueva lista
            lista = new Lista();
            lista.nombre = nombre;
            lista.descripcion = descripcion.isEmpty() ? null : descripcion;
            lista.tipo = tipoLista;
            lista.imagen = imagenPath;
        }

        // Guardar en la base de datos
        Lista finalLista = lista;
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            
            if (modoEdicion) {
                db.listaDao().actualizar(finalLista);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lista actualizada correctamente", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } else {
                long id = db.listaDao().insertar(finalLista);
                runOnUiThread(() -> {
                    if (id > 0) {
                        Toast.makeText(this, "Lista creada correctamente", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Error al crear la lista", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private void cargarDatosLista() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            listaOriginal = db.listaDao().obtenerTodas().stream()
                    .filter(l -> l.id == listaIdEditar)
                    .findFirst()
                    .orElse(null);

            if (listaOriginal != null) {
                runOnUiThread(() -> {
                    etNombre.setText(listaOriginal.nombre);
                    if (listaOriginal.descripcion != null) {
                        etDescripcion.setText(listaOriginal.descripcion);
                    }
                    
                    // Cargar imagen si existe
                    if (listaOriginal.imagen != null && !listaOriginal.imagen.isEmpty()) {
                        imagenPath = listaOriginal.imagen;
                        FormHelper.mostrarImagen(CrearListaActivity.this, ivImagenLista, layoutPlaceholder, imagenPath);
                    }
                    
                    // Actualizar título según el tipo
                    if (ListasActivity.TIPO_INVENTARIO.equals(listaOriginal.tipo)) {
                        tvTitulo.setText("Editar Inventario");
                    } else {
                        tvTitulo.setText("Editar Lista");
                    }
                    
                    // Guardar el tipo de lista
                    tipoLista = listaOriginal.tipo;
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lista no encontrada", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }
    
    @Override
    public void onBackPressed() {
        FormHelper.mostrarDialogoSalir(this);
    }
}
