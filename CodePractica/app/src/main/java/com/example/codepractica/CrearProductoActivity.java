package com.example.codepractica;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.codepractica.database.AppDatabase;
import com.example.codepractica.database.entities.Lista;
import com.example.codepractica.database.entities.Producto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CrearProductoActivity extends AppCompatActivity {

    private EditText etNombre, etDescripcion, etUnidades;
    private TextView tvCaducidad, tvInventarioSeleccionado, tvListaCompraSeleccionada;
    private Spinner spinnerInventario, spinnerListaCompra;
    private Button btnGuardar, btnInventario, btnListaCompra;
    private ImageButton btnAtras, btnCancelar;
    
    private long caducidadTimestamp = 0;
    private List<Lista> listaInventarios;
    private List<Lista> listaCompras;
    private int inventarioSeleccionadoId = -1; // -1 indica que no hay selección
    private int listaCompraSeleccionadaId = -1; // -1 indica que no hay selección
    private boolean almacenadoEnInventario = true; // Por defecto en inventario (pero no válido hasta seleccionar)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_producto);

        inicializarVistas();
        cargarListas();
        configurarBotones();
    }

    private void inicializarVistas() {
        btnAtras = findViewById(R.id.btnAtras);
        btnCancelar = findViewById(R.id.btnCancelar);
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
    }

    private void cargarListas() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            listaInventarios = db.listaDao().obtenerPorTipo("ListaInventario");
            listaCompras = db.listaDao().obtenerPorTipo("ListaCompra");

            runOnUiThread(() -> {
                // Configurar spinner de inventarios
                List<String> nombresInventarios = new ArrayList<>();
                nombresInventarios.add("Selecciona un inventario"); // Opción por defecto
                for (Lista lista : listaInventarios) {
                    nombresInventarios.add(lista.nombre);
                }
                ArrayAdapter<String> adapterInventario = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, nombresInventarios
                );
                adapterInventario.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerInventario.setAdapter(adapterInventario);
                spinnerInventario.setSelection(0); // Seleccionar la opción "Selecciona..."

                // Configurar spinner de listas de compra
                List<String> nombresListas = new ArrayList<>();
                nombresListas.add("Selecciona una lista"); // Opción por defecto
                for (Lista lista : listaCompras) {
                    nombresListas.add(lista.nombre);
                }
                ArrayAdapter<String> adapterLista = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, nombresListas
                );
                adapterLista.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerListaCompra.setAdapter(adapterLista);
                spinnerListaCompra.setSelection(0); // Seleccionar la opción "Selecciona..."

                // No establecer valores por defecto
                tvInventarioSeleccionado.setText("Selecciona un inventario");
                tvListaCompraSeleccionada.setText("Selecciona una lista");
            });
        }).start();
    }

    private void configurarBotones() {
        btnAtras.setOnClickListener(v -> finish());
        btnCancelar.setOnClickListener(v -> finish());

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

        // Estado inicial
        actualizarEstadoBotones();
    }

    private void actualizarEstadoBotones() {
        if (almacenadoEnInventario) {
            btnInventario.setBackgroundResource(R.drawable.bg_button_almacenado_selected);
            btnListaCompra.setBackgroundResource(R.drawable.bg_button_almacenado);
        } else {
            btnInventario.setBackgroundResource(R.drawable.bg_button_almacenado);
            btnListaCompra.setBackgroundResource(R.drawable.bg_button_almacenado_selected);
        }
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

        // Validar que se haya seleccionado al menos un inventario o una lista de compra
        if (inventarioSeleccionadoId == -1 && listaCompraSeleccionadaId == -1) {
            mostrarDialogoError("Error", "Por favor, selecciona al menos un inventario o una lista de compra");
            return;
        }

        // Validar según dónde se almacenará
        if (almacenadoEnInventario) {
            // Si se almacena en inventario, debe tener un inventario seleccionado
            if (inventarioSeleccionadoId == -1) {
                mostrarDialogoError("Error", "Por favor, selecciona un inventario para almacenar el producto");
                return;
            }
        } else {
            // Si se almacena en lista de compra, debe tener una lista seleccionada
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

        // Crear producto
        Producto nuevoProducto = new Producto();
        nuevoProducto.nombre = nombre;
        nuevoProducto.descripcion = descripcion.isEmpty() ? null : descripcion;
        nuevoProducto.unidades = unidades;
        nuevoProducto.caducidad = caducidadTimestamp > 0 ? caducidadTimestamp : null;
        nuevoProducto.imagen = "";
        
        // Asignar inventario y lista de compra (pueden ser -1 si no se seleccionó)
        nuevoProducto.lista_inventario = inventarioSeleccionadoId > 0 ? inventarioSeleccionadoId : 0;
        nuevoProducto.lista_compra = listaCompraSeleccionadaId > 0 ? listaCompraSeleccionadaId : 0;
        
        // Determinar dónde está almacenado
        if (almacenadoEnInventario) {
            nuevoProducto.almacenado = inventarioSeleccionadoId;
        } else {
            nuevoProducto.almacenado = listaCompraSeleccionadaId;
        }

        // Guardar en base de datos
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.productoDao().insertar(nuevoProducto);

            runOnUiThread(() -> {
                mostrarDialogoExito();
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

    private void mostrarDialogoExito() {
        new AlertDialog.Builder(this)
                .setTitle("Éxito")
                .setMessage("Producto creado correctamente")
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    setResult(RESULT_OK);
                    finish();
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(false)
                .show();
    }
}
