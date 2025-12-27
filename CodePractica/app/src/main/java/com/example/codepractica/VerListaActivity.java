package com.example.codepractica;

import android.content.Intent;
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
    private int listaId;
    private Lista lista;
    private boolean esInventario;

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
                runOnUiThread(() -> tvTitulo.setText(lista.nombre));
                
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
        });

        // Inventario
        findViewById(R.id.navInventario).setOnClickListener(v -> {
            Intent intent = new Intent(this, ListasActivity.class);
            intent.putExtra(ListasActivity.EXTRA_TIPO_LISTA, ListasActivity.TIPO_INVENTARIO);
            startActivity(intent);
        });

        // Lista
        findViewById(R.id.navLista).setOnClickListener(v -> {
            Intent intent = new Intent(this, ListasActivity.class);
            intent.putExtra(ListasActivity.EXTRA_TIPO_LISTA, ListasActivity.TIPO_COMPRA);
            startActivity(intent);
        });

        // Ajustes
        findViewById(R.id.navAjustes).setOnClickListener(v -> {
            Toast.makeText(this, "Ajustes", Toast.LENGTH_SHORT).show();
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
}
