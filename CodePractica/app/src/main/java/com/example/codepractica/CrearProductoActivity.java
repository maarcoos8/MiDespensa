package com.example.codepractica;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.example.codepractica.database.AppDatabase;
import com.example.codepractica.database.entities.Lista;
import com.example.codepractica.database.entities.Producto;
import com.example.codepractica.utils.ImageHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CrearProductoActivity extends AppCompatActivity {

    private EditText etNombre, etDescripcion, etUnidades;
    private TextView tvCaducidad, tvInventarioSeleccionado, tvListaCompraSeleccionada, tvTitulo;
    private Spinner spinnerInventario, spinnerListaCompra;
    private Button btnGuardar;
    private MaterialButton btnInventario, btnListaCompra;
    private ImageButton btnAtras;
    private CardView cardAnadirFoto;
    private ImageView ivFotoProducto;
    private LinearLayout layoutPlaceholder;
    
    private long caducidadTimestamp = 0;
    private List<Lista> listaInventarios;
    private List<Lista> listaCompras;
    private int inventarioSeleccionadoId = -1;
    private int listaCompraSeleccionadaId = -1;
    private boolean almacenadoEnInventario = true;
    private String imagenPath = null;
    
    // Variables para modo edición
    private boolean modoEdicion = false;
    private int productoIdEditar = -1;
    private Producto productoOriginal = null;
    
    // Launchers para imagen
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private ImageHelper imageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_producto);

        // Verificar si es modo edición
        productoIdEditar = getIntent().getIntExtra("producto_id", -1);
        modoEdicion = productoIdEditar != -1;

        inicializarLaunchers();
        inicializarVistas();
        cargarListas();
        configurarBotones();
        
        // Si es modo edición, cargar los datos del producto
        if (modoEdicion) {
            cargarDatosProducto();
        }
    }
    
    private void inicializarLaunchers() {
        // Launcher para la cámara
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        imageHelper.handleCameraResult();
                    }
                });
        
        // Launcher para la galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageHelper.handleGalleryResult(result.getData());
                    }
                });
        
        // Launcher para permisos
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        imageHelper.showImageSourceDialog(imagenPath != null);
                    } else {
                        Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
                    }
                });
        
        // Inicializar ImageHelper
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

    private void inicializarVistas() {
        btnAtras = findViewById(R.id.btnAtras);
        tvTitulo = findViewById(R.id.tvTitulo);
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etUnidades = findViewById(R.id.etUnidades);
        tvCaducidad = findViewById(R.id.tvCaducidad);
        spinnerInventario = findViewById(R.id.spinnerInventario);
        spinnerListaCompra = findViewById(R.id.spinnerListaCompra);
        tvInventarioSeleccionado = findViewById(R.id.tvInventarioSeleccionado);
        tvListaCompraSeleccionada = findViewById(R.id.tvListaCompraSeleccionada);
        btnInventario = findViewById(R.id.btnInventario);
        btnListaCompra = findViewById(R.id.btnListaCompra);
        btnGuardar = findViewById(R.id.btnGuardar);
        cardAnadirFoto = findViewById(R.id.cardAnadirFoto);
        ivFotoProducto = findViewById(R.id.ivFotoProducto);
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder);
        
        // Cambiar título según el modo
        if (modoEdicion) {
            tvTitulo.setText("Editar Producto");
            btnGuardar.setText("Actualizar");
        }
    }

    private void cargarListas() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            listaInventarios = db.listaDao().obtenerPorTipo("ListaInventario");
            listaCompras = db.listaDao().obtenerPorTipo("ListaCompra");

            runOnUiThread(() -> {
                // Configurar spinner de inventarios
                List<String> nombresInventarios = new ArrayList<>();
                nombresInventarios.add("Seleccionar"); // Opción por defecto
                for (Lista lista : listaInventarios) {
                    nombresInventarios.add(lista.nombre);
                }
                ArrayAdapter<String> adapterInventario = new ArrayAdapter<>(
                    this, R.layout.spinner_item, nombresInventarios
                );
                adapterInventario.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinnerInventario.setAdapter(adapterInventario);
                spinnerInventario.setSelection(0); // Seleccionar la opción "Selecciona..."

                // Configurar spinner de listas de compra
                List<String> nombresListas = new ArrayList<>();
                nombresListas.add("Seleccionar"); // Opción por defecto
                for (Lista lista : listaCompras) {
                    nombresListas.add(lista.nombre);
                }
                ArrayAdapter<String> adapterLista = new ArrayAdapter<>(
                    this, R.layout.spinner_item, nombresListas
                );
                adapterLista.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinnerListaCompra.setAdapter(adapterLista);
                spinnerListaCompra.setSelection(0); // Seleccionar la opción "Selecciona..."

                // Establecer valores según el modo
                if (modoEdicion && productoOriginal != null) {
                    // Modo edición: seleccionar las listas del producto
                    for (int i = 0; i < listaInventarios.size(); i++) {
                        if (listaInventarios.get(i).id == productoOriginal.lista_inventario) {
                            spinnerInventario.setSelection(i + 1); // +1 por el placeholder
                            tvInventarioSeleccionado.setText(listaInventarios.get(i).nombre);
                            break;
                        }
                    }
                    
                    for (int i = 0; i < listaCompras.size(); i++) {
                        if (listaCompras.get(i).id == productoOriginal.lista_compra) {
                            spinnerListaCompra.setSelection(i + 1); // +1 por el placeholder
                            tvListaCompraSeleccionada.setText(listaCompras.get(i).nombre);
                            break;
                        }
                    }
                } else {
                    // Modo creación: no establecer valores por defecto
                    tvInventarioSeleccionado.setText("Selecciona un inventario");
                    tvListaCompraSeleccionada.setText("Selecciona una lista");
                }
            });
        }).start();
    }

    private void configurarBotones() {
        btnAtras.setOnClickListener(v -> mostrarDialogoSalir());

        // Selector de caducidad
        findViewById(R.id.layoutCaducidad).setOnClickListener(v -> mostrarSelectorFecha());

        // Listener para cambios en spinners
        spinnerInventario.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position == 0) {
                    // Posición 0 es "Selecciona un inventario"
                    inventarioSeleccionadoId = -1;
                    tvInventarioSeleccionado.setText("Selecciona un inventario");
                } else if (!listaInventarios.isEmpty() && position - 1 < listaInventarios.size()) {
                    // position - 1 porque la primera posición es el placeholder
                    inventarioSeleccionadoId = listaInventarios.get(position - 1).id;
                    tvInventarioSeleccionado.setText(listaInventarios.get(position - 1).nombre);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        spinnerListaCompra.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position == 0) {
                    // Posición 0 es "Selecciona una lista"
                    listaCompraSeleccionadaId = -1;
                    tvListaCompraSeleccionada.setText("Selecciona una lista");
                } else if (!listaCompras.isEmpty() && position - 1 < listaCompras.size()) {
                    // position - 1 porque la primera posición es el placeholder
                    listaCompraSeleccionadaId = listaCompras.get(position - 1).id;
                    tvListaCompraSeleccionada.setText(listaCompras.get(position - 1).nombre);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Botones de almacenado
        btnInventario.setOnClickListener(v -> {
            almacenadoEnInventario = true;
            actualizarEstadoBotones();
        });

        btnListaCompra.setOnClickListener(v -> {
            almacenadoEnInventario = false;
            actualizarEstadoBotones();
        });

        // Botón guardar
        btnGuardar.setOnClickListener(v -> guardarProducto());

        // Click en la foto
        cardAnadirFoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            } else {
                imageHelper.showImageSourceDialog(imagenPath != null);
            }
        });

        // Estado inicial
        actualizarEstadoBotones();
    }

    private void actualizarEstadoBotones() {
        // Actualizar el estado checked de los botones
        btnInventario.setChecked(almacenadoEnInventario);
        btnListaCompra.setChecked(!almacenadoEnInventario);
    }

    private void cargarDatosProducto() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<Producto> productos = db.productoDao().obtenerTodos();
            
            // Buscar el producto por ID
            for (Producto p : productos) {
                if (p.id == productoIdEditar) {
                    productoOriginal = p;
                    break;
                }
            }
            
            if (productoOriginal == null) {
                runOnUiThread(() -> {
                    mostrarDialogoError("Error", "Producto no encontrado");
                    finish();
                });
                return;
            }
            
            runOnUiThread(() -> {
                // Cargar datos en los campos
                etNombre.setText(productoOriginal.nombre);
                etDescripcion.setText(productoOriginal.descripcion != null ? productoOriginal.descripcion : "");
                etUnidades.setText(String.valueOf(productoOriginal.unidades));
                
                // Cargar imagen si existe
                if (productoOriginal.imagen != null && !productoOriginal.imagen.isEmpty()) {
                    imagenPath = productoOriginal.imagen;
                    mostrarImagen(imagenPath);
                }
                
                // Cargar fecha de caducidad
                if (productoOriginal.caducidad != null && productoOriginal.caducidad > 0) {
                    caducidadTimestamp = productoOriginal.caducidad;
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    tvCaducidad.setText(sdf.format(new java.util.Date(productoOriginal.caducidad)));
                    tvCaducidad.setTextColor(getResources().getColor(android.R.color.black));
                }
                
                // Establecer las listas seleccionadas (se hará después de cargar las listas)
                inventarioSeleccionadoId = productoOriginal.lista_inventario;
                listaCompraSeleccionadaId = productoOriginal.lista_compra;
                
                // Determinar dónde está almacenado
                if (productoOriginal.almacenado == productoOriginal.lista_inventario) {
                    almacenadoEnInventario = true;
                } else {
                    almacenadoEnInventario = false;
                }
                actualizarEstadoBotones();
            });
        }).start();
    }

    private void mostrarSelectorFecha() {
        Calendar calendar = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                Calendar fechaSeleccionada = Calendar.getInstance();
                fechaSeleccionada.set(year, month, dayOfMonth);
                caducidadTimestamp = fechaSeleccionada.getTimeInMillis();
                
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                tvCaducidad.setText(sdf.format(fechaSeleccionada.getTime()));
                tvCaducidad.setTextColor(getResources().getColor(android.R.color.black));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }

    private void guardarProducto() {
        // Validar campos con AlertDialog
        String nombre = etNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            mostrarDialogoError("Error", "Por favor, ingresa el nombre del producto");
            return;
        }

        // VALIDACIONES OBLIGATORIAS: Debe tener ambas listas seleccionadas
        if (inventarioSeleccionadoId == -1) {
            mostrarDialogoError("Error de Validación", "Debes seleccionar obligatoriamente una lista de inventario para el producto");
            return;
        }

        if (listaCompraSeleccionadaId == -1) {
            mostrarDialogoError("Error de Validación", "Debes seleccionar obligatoriamente una lista de compra para el producto");
            return;
        }

        // Validar según dónde se almacenará
        if (almacenadoEnInventario) {
            // Si se almacena en inventario, verificar que haya un inventario seleccionado (ya validado arriba)
            if (inventarioSeleccionadoId == -1) {
                mostrarDialogoError("Error", "Por favor, selecciona un inventario para almacenar el producto");
                return;
            }
        } else {
            // Si se almacena en lista de compra, verificar que haya una lista seleccionada (ya validado arriba)
            if (listaCompraSeleccionadaId == -1) {
                mostrarDialogoError("Error", "Por favor, selecciona una lista de compra para almacenar el producto");
                return;
            }
        }

        String descripcion = etDescripcion.getText().toString().trim();
        String unidadesStr = etUnidades.getText().toString().trim();
        
        if (unidadesStr.isEmpty()) {
            mostrarDialogoError("Error", "Por favor, ingresa la cantidad de unidades");
            return;
        }
        
        float unidades;
        try {
            unidades = Float.parseFloat(unidadesStr);
            if (unidades <= 0) {
                mostrarDialogoError("Error", "La cantidad de unidades debe ser mayor que 0");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarDialogoError("Error", "Por favor, ingresa un número válido para las unidades");
            return;
        }

        // Crear o actualizar producto
        Producto producto;
        if (modoEdicion) {
            // Modo edición: usar el producto original y actualizar sus valores
            producto = productoOriginal;
        } else {
            // Modo creación: crear nuevo producto
            producto = new Producto();
        }
        
        producto.nombre = nombre;
        producto.descripcion = descripcion.isEmpty() ? null : descripcion;
        producto.unidades = unidades;
        producto.caducidad = caducidadTimestamp > 0 ? caducidadTimestamp : null;
        producto.imagen = imagenPath; // Guardar la ruta de la imagen
        
        // Asignar inventario y lista de compra
        producto.lista_inventario = inventarioSeleccionadoId > 0 ? inventarioSeleccionadoId : 0;
        producto.lista_compra = listaCompraSeleccionadaId > 0 ? listaCompraSeleccionadaId : 0;
        
        // Determinar dónde está almacenado
        if (almacenadoEnInventario) {
            producto.almacenado = inventarioSeleccionadoId;
        } else {
            producto.almacenado = listaCompraSeleccionadaId;
        }

        // Guardar en base de datos
        Producto finalProducto = producto;
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            
            if (modoEdicion) {
                db.productoDao().actualizar(finalProducto);
            } else {
                db.productoDao().insertar(finalProducto);
            }

            runOnUiThread(() -> {
                String mensaje = modoEdicion ? "Producto actualizado" : "Producto creado";
                Toast.makeText(CrearProductoActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        }).start();
    }

    private void mostrarDialogoError(String titulo, String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    
    private void mostrarImagen(String imagePath) {
        ivFotoProducto.setVisibility(View.VISIBLE);
        layoutPlaceholder.setVisibility(View.GONE);
        
        // Primero intentar cargar como recurso drawable
        try {
            int resourceId = getResources().getIdentifier(imagePath, "drawable", getPackageName());
            if (resourceId != 0) {
                ivFotoProducto.setImageResource(resourceId);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Si no es un recurso, intentar cargar como URI o ruta de archivo
        if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
            // Es una URI
            ivFotoProducto.setImageURI(Uri.parse(imagePath));
        } else {
            // Es una ruta de archivo
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                ivFotoProducto.setImageBitmap(bitmap);
            }
        }
    }
    
    private void ocultarImagen() {
        ivFotoProducto.setVisibility(View.GONE);
        layoutPlaceholder.setVisibility(View.VISIBLE);
        ivFotoProducto.setImageDrawable(null);
    }
    
    private void mostrarDialogoSalir() {
        new AlertDialog.Builder(this)
                .setTitle("Salir sin guardar")
                .setMessage("Los cambios no se harán efectivos si sales ahora. ¿Estás seguro de que quieres volver atrás?")
                .setPositiveButton("Salir", (dialog, which) -> finish())
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    
    @Override
    public void onBackPressed() {
        mostrarDialogoSalir();
    }
}
