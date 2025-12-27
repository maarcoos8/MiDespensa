package com.example.codepractica;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepractica.adapters.ListaAdapter;
import com.example.codepractica.database.AppDatabase;
import com.example.codepractica.database.entities.Lista;
import com.example.codepractica.database.entities.Producto;


import java.util.ArrayList;
import java.util.List;

public class ListasActivity extends AppCompatActivity {

    public static final String EXTRA_TIPO_LISTA = "tipo_lista";
    public static final String TIPO_COMPRA = "ListaCompra";
    public static final String TIPO_INVENTARIO = "ListaInventario";

    private RecyclerView recyclerViewListas;
    private ListaAdapter listaAdapter;
    private List<Lista> listaListas;
    private List<Lista> listaListasFiltradas;
    private List<Integer> cantidadProductos;
    private List<Integer> cantidadProductosFiltrados;
    private String tipoLista;
    private TextView tvTitulo;
    private android.widget.EditText etBuscar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listas);

        // Obtener el tipo de lista del Intent
        tipoLista = getIntent().getStringExtra(EXTRA_TIPO_LISTA);
        if (tipoLista == null) {
            tipoLista = TIPO_COMPRA; // Por defecto mostrar listas de compra
        }

        // Referencias a las vistas
        recyclerViewListas = findViewById(R.id.recyclerViewListas);
        tvTitulo = findViewById(R.id.tvTituloListas);
        etBuscar = findViewById(R.id.etBuscar);

        // Configurar el título según el tipo
        if (tipoLista.equals(TIPO_INVENTARIO)) {
            tvTitulo.setText("Inventarios");
        } else {
            tvTitulo.setText("Listas de la Compra");
        }

        // Configurar el RecyclerView
        recyclerViewListas.setLayoutManager(new LinearLayoutManager(this));
        listaListas = new ArrayList<>();
        listaListasFiltradas = new ArrayList<>();
        cantidadProductos = new ArrayList<>();
        cantidadProductosFiltrados = new ArrayList<>();
        listaAdapter = new ListaAdapter(listaListasFiltradas, cantidadProductosFiltrados, this);
        recyclerViewListas.setAdapter(listaAdapter);

        // Configurar el listener del adaptador
        listaAdapter.setOnListaClickListener(new ListaAdapter.OnListaClickListener() {
            @Override
            public void onListaClick(Lista lista) {
                Intent intent = new Intent(ListasActivity.this, VerListaActivity.class);
                intent.putExtra(VerListaActivity.EXTRA_LISTA_ID, lista.id);
                startActivity(intent);
            }

            @Override
            public void onMenuClick(Lista lista, View view) {
                mostrarMenuOpciones(lista, view);
            }
        });

        // Cargar las listas desde la base de datos
        cargarListas();

        // Configurar el botón de atrás
        findViewById(R.id.btnAtras).setOnClickListener(v -> finish());

        // Configurar la búsqueda
        etBuscar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarListas(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        // Configurar el botón flotante para añadir nueva lista
        findViewById(R.id.fabAnadirLista).setOnClickListener(v -> {
            Intent intent = new Intent(this, CrearListaActivity.class);
            intent.putExtra(CrearListaActivity.EXTRA_TIPO_LISTA, tipoLista);
            startActivity(intent);
        });

        // Configurar barra de navegación inferior
        configurarBarraNavegacion();
    }

    private void cargarListas() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            
            // Obtener listas del tipo especificado
            List<Lista> listas = db.listaDao().obtenerPorTipo(tipoLista);
            List<Integer> cantidades = new ArrayList<>();
            
            // Para cada lista, contar cuántos productos tiene
            for (Lista lista : listas) {
                int cantidad = db.productoDao().obtenerPorLista(lista.id).size();
                cantidades.add(cantidad);
            }
            
            // Actualizar las listas en el hilo principal
            runOnUiThread(() -> {
                listaListas.clear();
                listaListas.addAll(listas);
                cantidadProductos.clear();
                cantidadProductos.addAll(cantidades);
                listaListasFiltradas.clear();
                listaListasFiltradas.addAll(listaListas);
                cantidadProductosFiltrados.clear();
                cantidadProductosFiltrados.addAll(cantidadProductos);
                listaAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void filtrarListas(String texto) {
        listaListasFiltradas.clear();
        cantidadProductosFiltrados.clear();
        
        if (texto.isEmpty()) {
            listaListasFiltradas.addAll(listaListas);
            cantidadProductosFiltrados.addAll(cantidadProductos);
        } else {
            String textoLower = texto.toLowerCase();
            for (int i = 0; i < listaListas.size(); i++) {
                Lista lista = listaListas.get(i);
                if (lista.nombre.toLowerCase().contains(textoLower) ||
                    (lista.descripcion != null && lista.descripcion.toLowerCase().contains(textoLower))) {
                    listaListasFiltradas.add(lista);
                    cantidadProductosFiltrados.add(cantidadProductos.get(i));
                }
            }
        }
        
        listaAdapter.notifyDataSetChanged();
    }

    private void mostrarMenuOpciones(Lista lista, View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.menu_lista_opciones, popupMenu.getMenu());
        
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.menu_editar) {
                Intent intent = new Intent(this, EditarListaActivity.class);
                intent.putExtra(EditarListaActivity.EXTRA_LISTA_ID, lista.id);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_eliminar) {
                // Mostrar diálogo de confirmación antes de eliminar
                new android.app.AlertDialog.Builder(this)
                    .setTitle("Eliminar lista")
                    .setMessage("¿Estás seguro de que quieres eliminar \"" + lista.nombre + "\"?\n\nSe eliminarán también todos los productos de esta lista.")
                    .setPositiveButton("Eliminar", (dialog, which) -> eliminarLista(lista))
                    .setNegativeButton("Cancelar", null)
                    .show();
                return true;
            }
            
            return false;
        });
        
        popupMenu.show();
    }

    private void eliminarLista(Lista lista) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            
            // Primero obtener todos los productos de esta lista
            List<Producto> productosAEliminar = db.productoDao().obtenerPorLista(lista.id);
            
            // Eliminar todos los productos de la lista
            for (Producto producto : productosAEliminar) {
                db.productoDao().eliminar(producto);
            }
            
            // Luego eliminar la lista
            db.listaDao().eliminar(lista);
            
            runOnUiThread(() -> {
                String mensaje = productosAEliminar.isEmpty() 
                    ? "Lista eliminada correctamente"
                    : "Lista eliminada con " + productosAEliminar.size() + " producto(s)";
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                // Recargar las listas
                cargarListas();
            });
        }).start();
    }

    private void configurarBarraNavegacion() {
        // Productos
        findViewById(R.id.navProductos).setOnClickListener(v -> {
            startActivity(new Intent(this, ProductosActivity.class));
            finish();
        });

        // Inventario
        findViewById(R.id.navInventario).setOnClickListener(v -> {
            if (!tipoLista.equals(TIPO_INVENTARIO)) {
                Intent intent = new Intent(this, ListasActivity.class);
                intent.putExtra(EXTRA_TIPO_LISTA, TIPO_INVENTARIO);
                startActivity(intent);
                finish();
            }
        });

        // Lista
        findViewById(R.id.navLista).setOnClickListener(v -> {
            if (!tipoLista.equals(TIPO_COMPRA)) {
                Intent intent = new Intent(this, ListasActivity.class);
                intent.putExtra(EXTRA_TIPO_LISTA, TIPO_COMPRA);
                startActivity(intent);
                finish();
            }
        });

        // Ajustes
        findViewById(R.id.navAjustes).setOnClickListener(v -> {
            // TODO: Navegar a AjustesActivity cuando esté creada
            Toast.makeText(this, "Ajustes", Toast.LENGTH_SHORT).show();
        });

        // Actualizar el estado visual de la navegación
        actualizarEstadoNavegacion();
    }

    private void actualizarEstadoNavegacion() {
        // Obtener referencias a los iconos y textos
        android.widget.ImageView navInventarioIcon = findViewById(R.id.navInventarioIcon);
        android.widget.TextView navInventarioText = findViewById(R.id.navInventarioText);
        android.widget.ImageView navListaIcon = findViewById(R.id.navListaIcon);
        android.widget.TextView navListaText = findViewById(R.id.navListaText);

        // Color gris por defecto
        int colorGris = getResources().getColor(R.color.gray_light, null);
        int colorVerde = getResources().getColor(R.color.verde_principal, null);

        // Activar el correspondiente en verde
        if (tipoLista.equals(TIPO_INVENTARIO)) {
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
        // Recargar listas al volver a la actividad
        cargarListas();
    }
}
