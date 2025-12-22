package com.example.codepractica;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepractica.adapters.ProductoAdapter;
import com.example.codepractica.database.AppDatabase;
import com.example.codepractica.database.entities.Producto;

import java.util.ArrayList;
import java.util.List;

public class ProductosActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_DETALLE = 100;
    private RecyclerView recyclerViewProductos;
    private ProductoAdapter productoAdapter;
    private List<Producto> listaProductos;
    private List<Producto> listaProductosFiltrados;
    private EditText etBuscar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos);

        // Referencias a las vistas
        recyclerViewProductos = findViewById(R.id.recyclerViewProductos);
        etBuscar = findViewById(R.id.etBuscar);

        // Configurar el RecyclerView
        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        listaProductos = new ArrayList<>();
        listaProductosFiltrados = new ArrayList<>();
        productoAdapter = new ProductoAdapter(listaProductosFiltrados, this);
        recyclerViewProductos.setAdapter(productoAdapter);

        // Cargar los productos desde la base de datos
        cargarProductos();

        // Configurar el botón de atrás
        findViewById(R.id.btnAtras).setOnClickListener(v -> finish());

        // Configurar la búsqueda
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarProductos(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Configurar barra de navegación inferior
        configurarBarraNavegacion();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Si se eliminó un producto, recargar la lista
        if (requestCode == REQUEST_CODE_DETALLE && resultCode == RESULT_OK) {
            cargarProductos();
        }
    }

    private void cargarProductos() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            listaProductos = db.productoDao().obtenerTodos();
            listaProductosFiltrados.clear();
            listaProductosFiltrados.addAll(listaProductos);
            
            runOnUiThread(() -> productoAdapter.notifyDataSetChanged());
        }).start();
    }

    private void filtrarProductos(String texto) {
        listaProductosFiltrados.clear();
        
        if (texto.isEmpty()) {
            listaProductosFiltrados.addAll(listaProductos);
        } else {
            String textoLower = texto.toLowerCase();
            for (Producto producto : listaProductos) {
                if (producto.nombre.toLowerCase().contains(textoLower) ||
                    (producto.descripcion != null && producto.descripcion.toLowerCase().contains(textoLower))) {
                    listaProductosFiltrados.add(producto);
                }
            }
        }
        
        productoAdapter.notifyDataSetChanged();
    }

    private void configurarBarraNavegacion() {
        // Productos (ya estamos aquí)
        findViewById(R.id.navProductos).setOnClickListener(v -> {
            // Ya estamos en productos, no hacer nada
        });

        // Inventario
        findViewById(R.id.navInventario).setOnClickListener(v -> {
            // TODO: Navegar a InventarioActivity cuando esté creada
            // startActivity(new Intent(this, InventarioActivity.class));
        });

        // Botón de añadir
        findViewById(R.id.navAnadir).setOnClickListener(v -> {
            Intent intent = new Intent(this, CrearProductoActivity.class);
            startActivityForResult(intent, REQUEST_CODE_DETALLE);
        });

        // Lista
        findViewById(R.id.navLista).setOnClickListener(v -> {
            // TODO: Navegar a ListaActivity cuando esté creada
            // startActivity(new Intent(this, ListaActivity.class));
        });

        // Ajustes
        findViewById(R.id.navAjustes).setOnClickListener(v -> {
            // TODO: Navegar a AjustesActivity cuando esté creada
            // startActivity(new Intent(this, AjustesActivity.class));
        });
    }
}
