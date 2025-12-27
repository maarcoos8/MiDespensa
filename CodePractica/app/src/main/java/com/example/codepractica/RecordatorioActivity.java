package com.example.codepractica;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepractica.adapters.ProductoRecordatorioAdapter;
import com.example.codepractica.database.AppDatabase;
import com.example.codepractica.database.entities.Producto;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecordatorioActivity extends AppCompatActivity {

    private RecyclerView recyclerViewProductos;
    private ProductoRecordatorioAdapter productoAdapter;
    private List<Producto> listaProductos;
    private LinearLayout layoutVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordatorio);

        // Referencias a las vistas
        recyclerViewProductos = findViewById(R.id.recyclerViewProductos);
        layoutVacio = findViewById(R.id.layoutVacio);

        // Configurar el RecyclerView
        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        listaProductos = new ArrayList<>();
        productoAdapter = new ProductoRecordatorioAdapter(listaProductos, this);
        recyclerViewProductos.setAdapter(productoAdapter);

        // Cargar productos próximos a caducar
        cargarProductosProximosCaducar();

        // Configurar el botón de atrás
        findViewById(R.id.btnAtras).setOnClickListener(v -> finish());

        // Configurar barra de navegación inferior
        configurarBarraNavegacion();
    }

    private void cargarProductosProximosCaducar() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<Producto> todosProductos = db.productoDao().obtenerTodos();
            
            // Calcular la fecha límite (hoy + 7 días)
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 7);
            long fechaLimite = calendar.getTimeInMillis();
            long fechaActual = System.currentTimeMillis();
            
            // Filtrar productos que caducan en menos de 7 días
            List<Producto> productosProximos = new ArrayList<>();
            for (Producto producto : todosProductos) {
                if (producto.caducidad != null && producto.caducidad > 0) {
                    // Solo productos que no han caducado aún y caducan en menos de 7 días
                    if (producto.caducidad >= fechaActual && producto.caducidad <= fechaLimite) {
                        productosProximos.add(producto);
                    }
                }
            }
            
            // Ordenar por fecha de caducidad (de menor a mayor - los que caducan primero aparecen primero)
            Collections.sort(productosProximos, new Comparator<Producto>() {
                @Override
                public int compare(Producto p1, Producto p2) {
                    return Long.compare(p1.caducidad, p2.caducidad);
                }
            });
            
            runOnUiThread(() -> {
                listaProductos.clear();
                listaProductos.addAll(productosProximos);
                productoAdapter.notifyDataSetChanged();
                
                // Mostrar u ocultar el mensaje de lista vacía
                if (listaProductos.isEmpty()) {
                    recyclerViewProductos.setVisibility(View.GONE);
                    layoutVacio.setVisibility(View.VISIBLE);
                } else {
                    recyclerViewProductos.setVisibility(View.VISIBLE);
                    layoutVacio.setVisibility(View.GONE);
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

        // Recordatorio (ya estamos aquí)
        findViewById(R.id.navRecordatorio).setOnClickListener(v -> {
            // Ya estamos en recordatorio, no hacer nada
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar productos al volver a la actividad
        cargarProductosProximosCaducar();
    }
}
