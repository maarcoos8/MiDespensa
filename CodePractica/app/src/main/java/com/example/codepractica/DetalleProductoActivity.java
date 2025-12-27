package com.example.codepractica;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.codepractica.database.AppDatabase;
import com.example.codepractica.database.entities.Lista;
import com.example.codepractica.database.entities.Producto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class DetalleProductoActivity extends AppCompatActivity {

    private Producto producto;
    private ImageView ivProductoImagen;
    private TextView tvNombre, tvDescripcion, tvCantidad, tvCaducidad, tvUbicacion, tvListaCompra, tvListaInventario;
    private Button btnEliminar, btnEditar;
    private ImageButton btnAtras;
    private int ubicacionListaId = -1;
    private int listaCompraId = -1;
    private int listaInventarioId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_producto);

        // Obtener el ID del producto desde el Intent
        int productoId = getIntent().getIntExtra("producto_id", -1);
        if (productoId == -1) {
            Toast.makeText(this, "Error al cargar el producto", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        inicializarVistas();
        cargarProducto(productoId);
        configurarBotones();
    }

    private void inicializarVistas() {
        btnAtras = findViewById(R.id.btnAtras);
        btnEditar = findViewById(R.id.btnEditar);
        ivProductoImagen = findViewById(R.id.ivProductoImagen);
        tvNombre = findViewById(R.id.tvNombre);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvCantidad = findViewById(R.id.tvCantidad);
        tvCaducidad = findViewById(R.id.tvCaducidad);
        tvUbicacion = findViewById(R.id.tvUbicacion);
        tvListaCompra = findViewById(R.id.tvListaCompra);
        tvListaInventario = findViewById(R.id.tvListaInventario);
        btnEliminar = findViewById(R.id.btnEliminar);
    }

    private void cargarProducto(int productoId) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<Producto> productos = db.productoDao().obtenerTodos();
            
            // Buscar el producto por ID
            for (Producto p : productos) {
                if (p.id == productoId) {
                    producto = p;
                    break;
                }
            }

            if (producto == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            // Obtener información de las listas
            String ubicacion = "Sin asignar";
            String listaCompra = "Sin asignar";
            String listaInventario = "Sin asignar";

            if (producto.almacenado > 0) {
                List<Lista> listas = db.listaDao().obtenerTodas();
                for (Lista lista : listas) {
                    if (lista.id == producto.almacenado) {
                        ubicacion = lista.nombre;
                        ubicacionListaId = lista.id;
                        break;
                    }
                }
            }

            if (producto.lista_compra > 0) {
                List<Lista> listas = db.listaDao().obtenerTodas();
                for (Lista lista : listas) {
                    if (lista.id == producto.lista_compra) {
                        listaCompra = lista.nombre;
                        listaCompraId = lista.id;
                        break;
                    }
                }
            }

            if (producto.lista_inventario > 0) {
                List<Lista> listas = db.listaDao().obtenerTodas();
                for (Lista lista : listas) {
                    if (lista.id == producto.lista_inventario) {
                        listaInventario = lista.nombre;
                        listaInventarioId = lista.id;
                        break;
                    }
                }
            }

            String finalUbicacion = ubicacion;
            String finalListaCompra = listaCompra;
            String finalListaInventario = listaInventario;

            runOnUiThread(() -> {
                // Establecer datos en las vistas
                tvNombre.setText(producto.nombre);
                tvDescripcion.setText(producto.descripcion != null ? producto.descripcion : "Sin descripción");
                tvCantidad.setText(String.format(Locale.getDefault(), "%.0f", producto.unidades));
                
                // Formatear fecha de caducidad
                if (producto.caducidad != null && producto.caducidad > 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
                    tvCaducidad.setText(sdf.format(new Date(producto.caducidad)));
                } else {
                    tvCaducidad.setText("Sin fecha");
                }

                tvUbicacion.setText(finalUbicacion);
                tvListaCompra.setText(finalListaCompra);
                tvListaInventario.setText(finalListaInventario);

                // Cargar imagen del producto
                if (producto.imagen != null && !producto.imagen.isEmpty()) {
                    cargarImagen(producto.imagen);
                } else {
                    ivProductoImagen.setImageResource(R.drawable.ic_products);
                    ivProductoImagen.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            });
        }).start();
    }
    
    private void cargarImagen(String imagePath) {
        // Primero intentar cargar como recurso drawable
        try {
            int resourceId = getResources().getIdentifier(imagePath, "drawable", getPackageName());
            if (resourceId != 0) {
                ivProductoImagen.setImageResource(resourceId);
                ivProductoImagen.setScaleType(ImageView.ScaleType.CENTER_CROP);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Si no es un recurso, intentar cargar como URI o ruta de archivo
        if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
            try {
                ivProductoImagen.setImageURI(Uri.parse(imagePath));
                ivProductoImagen.setScaleType(ImageView.ScaleType.CENTER_CROP);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    ivProductoImagen.setImageBitmap(bitmap);
                    ivProductoImagen.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Si todo falla, usar imagen por defecto
        ivProductoImagen.setImageResource(R.drawable.ic_products);
        ivProductoImagen.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    private void configurarBotones() {
        btnAtras.setOnClickListener(v -> finish());

        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, CrearProductoActivity.class);
            intent.putExtra("producto_id", producto.id);
            startActivityForResult(intent, 2); // Código de request 2 para edición
        });

        btnEliminar.setOnClickListener(v -> mostrarDialogoEliminar());

        // Click en tarjeta de cantidad para editar rápidamente
        findViewById(R.id.cardCantidad).setOnClickListener(v -> mostrarDialogoEditarCantidad());

        // Click en tarjeta de caducidad para editar rápidamente
        findViewById(R.id.cardCaducidad).setOnClickListener(v -> mostrarDialogoEditarCaducidad());

        // Click en ubicación
        findViewById(R.id.layoutUbicacion).setOnClickListener(v -> {
            if (ubicacionListaId > 0) {
                Intent intent = new Intent(this, VerListaActivity.class);
                intent.putExtra(VerListaActivity.EXTRA_LISTA_ID, ubicacionListaId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Este producto no tiene ubicación asignada", Toast.LENGTH_SHORT).show();
            }
        });

        // Click en lista de inventario
        findViewById(R.id.layoutListaInventario).setOnClickListener(v -> {
            if (listaInventarioId > 0) {    
                Intent intent = new Intent(this, VerListaActivity.class);
                intent.putExtra(VerListaActivity.EXTRA_LISTA_ID, listaInventarioId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Este producto no tiene lista de inventario asignada", Toast.LENGTH_SHORT).show();
            }
        });

        // Click en lista de compra
        findViewById(R.id.layoutListaCompra).setOnClickListener(v -> {
            if (listaCompraId > 0) {
                Intent intent = new Intent(this, VerListaActivity.class);
                intent.putExtra(VerListaActivity.EXTRA_LISTA_ID, listaCompraId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Este producto no tiene lista de compra asignada", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Estás seguro de que deseas eliminar \"" + producto.nombre + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarProducto())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEditarCantidad() {
        // Crear un EditText para ingresar la nueva cantidad
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf((int) producto.unidades));
        input.setSelection(input.getText().length());
        
        // Configurar el layout del EditText
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        );
        int marginInDp = 24; // 24dp de margen
        float scale = getResources().getDisplayMetrics().density;
        int marginInPx = (int) (marginInDp * scale + 0.5f);
        params.leftMargin = marginInPx;
        params.rightMargin = marginInPx;
        input.setLayoutParams(params);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Editar Cantidad")
                .setMessage("Introduce la nueva cantidad de unidades:")
                .setView(container)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String cantidadStr = input.getText().toString().trim();
                    if (!cantidadStr.isEmpty()) {
                        try {
                            float nuevaCantidad = Float.parseFloat(cantidadStr);
                            if (nuevaCantidad >= 0) {
                                actualizarCantidad(nuevaCantidad);
                            } else {
                                Toast.makeText(this, "La cantidad no puede ser negativa", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
        
        // Mostrar el teclado automáticamente
        input.requestFocus();
        input.postDelayed(() -> {
            android.view.inputmethod.InputMethodManager imm = 
                (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(input, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }

    private void mostrarDialogoEditarCaducidad() {
        // Obtener la fecha actual del producto o usar hoy como predeterminada
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        if (producto.caducidad != null && producto.caducidad > 0) {
            calendar.setTimeInMillis(producto.caducidad);
        }

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                java.util.Calendar nuevaFecha = java.util.Calendar.getInstance();
                nuevaFecha.set(year, month, dayOfMonth);
                actualizarCaducidad(nuevaFecha.getTimeInMillis());
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.setTitle("Editar Fecha de Caducidad");
        datePickerDialog.show();
    }

    private void actualizarCantidad(float nuevaCantidad) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            producto.unidades = nuevaCantidad;
            db.productoDao().actualizar(producto);
            
            runOnUiThread(() -> {
                tvCantidad.setText(String.format(Locale.getDefault(), "%.0f", nuevaCantidad));
                Toast.makeText(this, "Cantidad actualizada", Toast.LENGTH_SHORT).show();
                
                // Si la cantidad llegó a 0 y el producto está en un inventario, preguntar cantidad para lista de compra
                if (nuevaCantidad == 0 && producto.almacenado > 0) {
                    mostrarDialogoAgregarAListaCompra();
                }
            });
        }).start();
    }

    private void mostrarDialogoAgregarAListaCompra() {
        // Verificar si el producto tiene una lista de compra asignada
        if (producto.lista_compra <= 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Sin lista de compra")
                    .setMessage("Este producto no tiene una lista de compra asignada. Edita el producto para asignar una lista primero.")
                    .setPositiveButton("Entendido", (dialog, which) -> {
                        // Restaurar cantidad a 1 si cancela
                        new Thread(() -> {
                            AppDatabase db = AppDatabase.getInstance(this);
                            producto.unidades = 1;
                            db.productoDao().actualizar(producto);
                            runOnUiThread(() -> tvCantidad.setText("1"));
                        }).start();
                    })
                    .show();
            return;
        }
        
        // Obtener el nombre de la lista de compra y mostrar directamente el diálogo de cantidad
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Lista listaCompra = db.listaDao().obtenerTodas().stream()
                    .filter(l -> l.id == producto.lista_compra)
                    .findFirst()
                    .orElse(null);
            
            if (listaCompra == null) {
                runOnUiThread(() -> 
                    Toast.makeText(this, "Error: no se encontró la lista de compra", Toast.LENGTH_SHORT).show()
                );
                return;
            }
            
            runOnUiThread(() -> mostrarDialogoCantidadParaCompra(listaCompra));
        }).start();
    }

    private void mostrarDialogoCantidadParaCompra(Lista listaCompra) {
        // Crear un EditText para ingresar la cantidad a comprar
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Cantidad a comprar");
        input.setText("1");
        input.setSelection(input.getText().length());
        
        // Configurar el layout del EditText
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        );
        int marginInDp = 24;
        float scale = getResources().getDisplayMetrics().density;
        int marginInPx = (int) (marginInDp * scale + 0.5f);
        params.leftMargin = marginInPx;
        params.rightMargin = marginInPx;
        input.setLayoutParams(params);
        container.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Cantidad a comprar")
                .setMessage("¿Cuántas unidades deseas añadir a \"" + listaCompra.nombre + "\"?")
                .setView(container)
                .setCancelable(false)
                .setPositiveButton("Añadir", null)
                .setNegativeButton("Cancelar", (d, which) -> {
                    // Al cancelar, restaurar la cantidad a 1
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(this);
                        producto.unidades = 1;
                        db.productoDao().actualizar(producto);
                        runOnUiThread(() -> {
                            tvCantidad.setText("1");
                            Toast.makeText(this, "Cantidad restaurada a 1", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .create();
        
        dialog.show();
        
        // Configurar el botón de añadir manualmente para validar
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String cantidadStr = input.getText().toString().trim();
            if (cantidadStr.isEmpty()) {
                Toast.makeText(this, "Debes ingresar una cantidad", Toast.LENGTH_SHORT).show();
                input.requestFocus();
                return;
            }
            
            try {
                float cantidad = Float.parseFloat(cantidadStr);
                if (cantidad > 0) {
                    agregarAListaCompra(listaCompra.id, cantidad);
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                    input.requestFocus();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
                input.requestFocus();
            }
        });
        
        // Mostrar el teclado automáticamente
        input.requestFocus();
        input.postDelayed(() -> {
            android.view.inputmethod.InputMethodManager imm = 
                (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(input, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }

    private void agregarAListaCompra(int listaCompraId, float cantidadComprar) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            
            // Actualizar el producto: mover a la lista de compra y actualizar la cantidad
            producto.almacenado = listaCompraId; // Cambiar ubicación a la lista de compra
            producto.unidades = cantidadComprar; // Guardar la cantidad a comprar
            producto.caducidad = null; // Establecer fecha de caducidad a NULL
            db.productoDao().actualizar(producto);
            
            // Obtener el nombre de la lista para el mensaje
            Lista lista = db.listaDao().obtenerTodas().stream()
                    .filter(l -> l.id == listaCompraId)
                    .findFirst()
                    .orElse(null);
            
            String nombreLista = lista != null ? lista.nombre : "lista de compra";
            
            runOnUiThread(() -> {
                // Actualizar la UI
                ubicacionListaId = listaCompraId;
                tvUbicacion.setText(nombreLista);
                tvListaCompra.setText(nombreLista);
                tvCantidad.setText(String.format(Locale.getDefault(), "%.0f", cantidadComprar));
                tvCaducidad.setText("Sin fecha");
                Toast.makeText(this, "Producto movido a " + nombreLista, Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void actualizarCaducidad(long nuevaCaducidad) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            producto.caducidad = nuevaCaducidad;
            db.productoDao().actualizar(producto);
            
            runOnUiThread(() -> {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
                tvCaducidad.setText(sdf.format(new Date(nuevaCaducidad)));
                Toast.makeText(this, "Fecha de caducidad actualizada", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void eliminarProducto() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.productoDao().eliminar(producto);

            runOnUiThread(() -> {
                Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                // Volver a la pantalla anterior
                setResult(RESULT_OK); // Indicar que se eliminó el producto
                finish();
            });
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            // El producto fue editado, recargar los datos
            cargarProducto(producto.id);
        }
    }
}
