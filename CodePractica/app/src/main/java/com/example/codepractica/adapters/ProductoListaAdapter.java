package com.example.codepractica.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepractica.DetalleProductoActivity;
import com.example.codepractica.R;
import com.example.codepractica.database.AppDatabase;
import com.example.codepractica.database.entities.Lista;
import com.example.codepractica.database.entities.Producto;
import com.example.codepractica.utils.FormHelper;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProductoListaAdapter extends RecyclerView.Adapter<ProductoListaAdapter.ProductoViewHolder> {

    private List<Producto> listaProductos;
    private Context context;
    private boolean esInventario;

    public ProductoListaAdapter(List<Producto> listaProductos, Context context, boolean esInventario) {
        this.listaProductos = listaProductos;
        this.context = context;
        this.esInventario = esInventario;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto_lista, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);

        holder.tvNombre.setText(producto.nombre);
        
        // Formatear cantidad
        String textoCantidad;
        if (producto.unidades == (int) producto.unidades) {
            textoCantidad = (int) producto.unidades + " " + 
                           ((int) producto.unidades == 1 ? "unidad" : "unidades");
        } else {
            textoCantidad = producto.unidades + " uds.";
        }
        holder.tvCantidad.setText(textoCantidad);

        // Mostrar checkbox solo en listas de compra (no en inventarios)
        if (!esInventario) {
            holder.checkboxProducto.setVisibility(View.VISIBLE);
            holder.checkboxProducto.setChecked(false);
            
            // Configurar el listener del checkbox
            holder.checkboxProducto.setOnCheckedChangeListener(null); // Limpiar listener anterior
            holder.checkboxProducto.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Deshabilitar más interacciones mientras se procesa
                    holder.checkboxProducto.setEnabled(false);
                    holder.itemView.setEnabled(false);
                    
                    // Esperar 1 segundo y luego mover el producto al inventario
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        moverProductoAInventario(producto, position);
                    }, 1000);
                }
            });
        } else {
            holder.checkboxProducto.setVisibility(View.GONE);
        }

        // Mostrar indicador de caducidad solo si es inventario y tiene fecha de caducidad
        if (esInventario && producto.caducidad != null && producto.caducidad > 0) {
            holder.layoutCaducidad.setVisibility(View.VISIBLE);
            
            // Calcular días hasta la caducidad
            long fechaActual = System.currentTimeMillis();
            long diasHastaCaducidad = TimeUnit.MILLISECONDS.toDays(producto.caducidad - fechaActual);
            
            String textoCaducidad;
            int color;
            
            if (diasHastaCaducidad < 0) {
                // Ya caducado
                textoCaducidad = "Caducado";
                color = 0xFFDC2626; // Rojo oscuro
            } else if (diasHastaCaducidad == 0) {
                // Caduca hoy
                textoCaducidad = "Caduca hoy";
                color = 0xFFEF4444; // Rojo
            } else if (diasHastaCaducidad <= 3) {
                // Caduca en 1-3 días
                textoCaducidad = "Caduca en " + diasHastaCaducidad + (diasHastaCaducidad == 1 ? " día" : " días");
                color = 0xFFF97316; // Naranja
            } else if (diasHastaCaducidad <= 7) {
                // Caduca en 4-7 días
                textoCaducidad = "Caduca en " + diasHastaCaducidad + " días";
                color = 0xFFFBBF24; // Amarillo/Naranja claro
            } else {
                // Caduca en más de 7 días
                textoCaducidad = "Caduca en " + diasHastaCaducidad + " días";
                color = 0xFF10B981; // Verde
            }
            
            holder.tvCaducidad.setText(textoCaducidad);
            holder.tvCaducidad.setTextColor(color);
            holder.viewIndicador.setBackgroundColor(color);
        } else {
            holder.layoutCaducidad.setVisibility(View.GONE);
        }
        
        // Cargar imagen del producto si existe
        if (producto.imagen != null && !producto.imagen.isEmpty()) {
            FormHelper.cargarImagenEnAdapter(context, holder.ivImagen, producto.imagen, R.drawable.ic_products);
        } else {
            holder.ivImagen.setImageResource(R.drawable.ic_products);
            holder.ivImagen.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            holder.ivImagen.setPadding(8, 8, 8, 8);
        }

        // Click en el item para ir al detalle
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleProductoActivity.class);
            intent.putExtra("producto_id", producto.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }
    
    
    private void moverProductoAInventario(Producto producto, int position) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            
            // Verificar si el producto tiene un inventario asignado
            if (producto.lista_inventario <= 0) {
                // Si no tiene inventario asignado, mostrar error
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "Este producto no tiene un inventario asignado", Toast.LENGTH_SHORT).show();
                    // Remover el producto de la lista de compra de todas formas
                    listaProductos.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listaProductos.size());
                });
                return;
            }
            
            // Obtener el nombre del inventario de destino
            Lista inventario = db.listaDao().obtenerTodas().stream()
                    .filter(l -> l.id == producto.lista_inventario)
                    .findFirst()
                    .orElse(null);
            
            String nombreInventario = inventario != null ? inventario.nombre : "inventario";
            
            // Mover el producto al inventario
            producto.almacenado = producto.lista_inventario;
            db.productoDao().actualizar(producto);
            
            // Actualizar la UI en el hilo principal
            new Handler(Looper.getMainLooper()).post(() -> {
                // Remover el producto de la lista
                listaProductos.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, listaProductos.size());
                
                // Mostrar mensaje de confirmación
                Toast.makeText(context, "Producto añadido a " + nombreInventario, Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvCantidad;
        LinearLayout layoutCaducidad;
        TextView tvCaducidad;
        View viewIndicador;
        ImageView ivImagen;
        CheckBox checkboxProducto;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvProductoNombre);
            tvCantidad = itemView.findViewById(R.id.tvProductoCantidad);
            layoutCaducidad = itemView.findViewById(R.id.layoutCaducidad);
            tvCaducidad = itemView.findViewById(R.id.tvCaducidad);
            viewIndicador = itemView.findViewById(R.id.viewIndicadorCaducidad);
            ivImagen = itemView.findViewById(R.id.ivProductoImagen);
            checkboxProducto = itemView.findViewById(R.id.checkboxProducto);
        }
    }
}
