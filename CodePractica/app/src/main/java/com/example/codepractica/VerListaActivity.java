package com.example.codepractica;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepractica.adapters.ProductoListaAdapter;
import com.example.codepractica.database.AppDatabase;
import com.example.codepractica.database.entities.Lista;
import com.example.codepractica.database.entities.Producto;

import java.util.ArrayList;
import java.util.List;

public class VerListaActivity extends AppCompatActivity {

    public static final String EXTRA_LISTA_ID = "lista_id";
    
    private RecyclerView recyclerViewProductos;
    private ProductoListaAdapter productoAdapter;
    private List<Producto> listaProductos;
    private TextView tvTitulo;
    private ImageView ivListaImagen;
    private TextView tvListaDescripcion;
    private android.widget.LinearLayout layoutInfoLista;
    private int listaId;
    private Lista lista;
    private boolean esInventario;
    private android.widget.LinearLayout layoutVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_lista);

        // Obtener el ID de la lista del Intent
        listaId = getIntent().getIntExtra(EXTRA_LISTA_ID, -1);
        if (listaId == -1) {
            Toast.makeText(this, "Error al cargar la lista", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Referencias a las vistas
        recyclerViewProductos = findViewById(R.id.recyclerViewProductos);
        tvTitulo = findViewById(R.id.tvTituloLista);
        layoutVacio = findViewById(R.id.layoutVacio);
        ivListaImagen = findViewById(R.id.ivListaImagen);
        tvListaDescripcion = findViewById(R.id.tvListaDescripcion);
        layoutInfoLista = findViewById(R.id.layoutInfoLista);

        // Cargar información de la lista
        cargarInformacionLista();

        // Configurar el RecyclerView
        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        listaProductos = new ArrayList<>();

        // Cargar los productos de la lista
        cargarProductos();

        // Configurar botón de atrás
        findViewById(R.id.btnAtras).setOnClickListener(v -> finish());

        // Configurar botón de editar
        findViewById(R.id.btnEditar).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditarListaActivity.class);
            intent.putExtra(EditarListaActivity.EXTRA_LISTA_ID, listaId);
            startActivity(intent);
        });

        // Configurar botón flotante para añadir producto
        findViewById(R.id.fabAnadirProducto).setOnClickListener(v -> {
            Intent intent = new Intent(this, CrearProductoActivity.class);
            intent.putExtra("lista_id", listaId);
            startActivity(intent);
        });

        // Configurar barra de navegación
        configurarBarraNavegacion();
    }

    private void cargarInformacionLista() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            lista = db.listaDao().obtenerTodas().stream()
                    .filter(l -> l.id == listaId)
                    .findFirst()
                    .orElse(null);

            if (lista != null) {
                esInventario = ListasActivity.TIPO_INVENTARIO.equals(lista.tipo);
                runOnUiThread(() -> {
                    tvTitulo.setText(lista.nombre);
                    
                    // Verificar si hay imagen o descripción para mostrar
                    boolean tieneImagen = lista.imagen != null && !lista.imagen.trim().isEmpty();
                    boolean tieneDescripcion = lista.descripcion != null && !lista.descripcion.trim().isEmpty();
                    
                    // Mostrar siempre el layout si hay descripción o si queremos mostrar imagen (incluso por defecto)
                    if (tieneDescripcion || true) {  // Siempre visible para mostrar imagen
                        layoutInfoLista.setVisibility(android.view.View.VISIBLE);
                        
                        // Cargar imagen si existe, o mostrar icono por defecto
                        if (tieneImagen) {
                            cargarImagen(ivListaImagen, lista.imagen);
                        } else {
                            // Mostrar icono por defecto según el tipo
                            if (esInventario) {
                                ivListaImagen.setImageResource(R.drawable.ic_inventory);
                            } else {
                                ivListaImagen.setImageResource(R.drawable.ic_shopping_list);
                            }
                            ivListaImagen.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        }
                        
                        // Mostrar descripción si existe
                        if (tieneDescripcion) {
                            tvListaDescripcion.setVisibility(android.view.View.VISIBLE);
                            tvListaDescripcion.setText(lista.descripcion);
                        } else {
                            tvListaDescripcion.setVisibility(android.view.View.GONE);
                        }
                    }
                });
                
                // Actualizar navegación según el tipo
                runOnUiThread(this::actualizarEstadoNavegacion);
            }
        }).start();
    }

    private void cargarProductos() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<Producto> productos = db.productoDao().obtenerPorLista(listaId);
            
            runOnUiThread(() -> {
                listaProductos.clear();
                listaProductos.addAll(productos);
                
                // Mostrar u ocultar el mensaje de lista vacía
                if (listaProductos.isEmpty()) {
                    recyclerViewProductos.setVisibility(android.view.View.GONE);
                    layoutVacio.setVisibility(android.view.View.VISIBLE);
                } else {
                    recyclerViewProductos.setVisibility(android.view.View.VISIBLE);
                    layoutVacio.setVisibility(android.view.View.GONE);
                }
                
                // Solo crear el adaptador si no existe
                if (productoAdapter == null) {
                    productoAdapter = new ProductoListaAdapter(listaProductos, this, esInventario);
                    recyclerViewProductos.setAdapter(productoAdapter);
                } else {
                    productoAdapter.notifyDataSetChanged();
                }
            });
        }).start();
    }

    private void configurarBarraNavegacion() {
        // Productos
        findViewById(R.id.navProductos).setOnClickListener(v -> {
            startActivity(new Intent(this, ProductosActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        // Inventario
        findViewById(R.id.navInventario).setOnClickListener(v -> {
            Intent intent = new Intent(this, ListasActivity.class);
            intent.putExtra(ListasActivity.EXTRA_TIPO_LISTA, ListasActivity.TIPO_INVENTARIO);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        // Inicio
        findViewById(R.id.navInicio).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        // Lista
        findViewById(R.id.navLista).setOnClickListener(v -> {
            Intent intent = new Intent(this, ListasActivity.class);
            intent.putExtra(ListasActivity.EXTRA_TIPO_LISTA, ListasActivity.TIPO_COMPRA);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        // Recordatorio
        findViewById(R.id.navRecordatorio).setOnClickListener(v -> {
            startActivity(new Intent(this, RecordatorioActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });
    }

    private void actualizarEstadoNavegacion() {
        // Obtener referencias a los iconos y textos
        ImageView navInventarioIcon = findViewById(R.id.navInventarioIcon);
        TextView navInventarioText = findViewById(R.id.navInventarioText);
        ImageView navListaIcon = findViewById(R.id.navListaIcon);
        TextView navListaText = findViewById(R.id.navListaText);

        // Color gris por defecto
        int colorGris = getResources().getColor(R.color.gray_light, null);
        int colorVerde = getResources().getColor(R.color.verde_principal, null);

        // Activar el correspondiente en verde
        if (esInventario) {
            // Inventario en verde, Lista en gris
            navInventarioIcon.setColorFilter(colorVerde);
            navInventarioText.setTextColor(colorVerde);
            navListaIcon.setColorFilter(colorGris);
            navListaText.setTextColor(colorGris);
        } else {
            // Lista en verde, Inventario en gris
            navInventarioIcon.setColorFilter(colorGris);
            navInventarioText.setTextColor(colorGris);
            navListaIcon.setColorFilter(colorVerde);
            navListaText.setTextColor(colorVerde);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar información de la lista por si se editó
        cargarInformacionLista();
        // Recargar productos al volver a la actividad
        cargarProductos();
    }
    
    private void cargarImagen(ImageView imageView, String imagePath) {
        // Primero intentar cargar como recurso drawable
        try {
            int resourceId = getResources().getIdentifier(imagePath, "drawable", getPackageName());
            if (resourceId != 0) {
                imageView.setImageResource(resourceId);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Si no es un recurso, intentar cargar como URI o ruta de archivo
        if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
            try {
                imageView.setImageURI(Uri.parse(imagePath));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Si todo falla, dejar la imagen por defecto del layout
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }
}
