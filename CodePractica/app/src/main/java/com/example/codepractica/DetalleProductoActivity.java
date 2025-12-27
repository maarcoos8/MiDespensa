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
        if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
            try {
                ivProductoImagen.setImageURI(Uri.parse(imagePath));
                ivProductoImagen.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                ivProductoImagen.setImageResource(R.drawable.ic_products);
                ivProductoImagen.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        } else {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    ivProductoImagen.setImageBitmap(bitmap);
                    ivProductoImagen.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    ivProductoImagen.setImageResource(R.drawable.ic_products);
                    ivProductoImagen.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            } catch (Exception e) {
                ivProductoImagen.setImageResource(R.drawable.ic_products);
                ivProductoImagen.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        }
    }

    private void configurarBotones() {
        btnAtras.setOnClickListener(v -> finish());

        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, CrearProductoActivity.class);
            intent.putExtra("producto_id", producto.id);
            startActivityForResult(intent, 2); // Código de request 2 para edición
        });

        btnEliminar.setOnClickListener(v -> mostrarDialogoEliminar());

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
