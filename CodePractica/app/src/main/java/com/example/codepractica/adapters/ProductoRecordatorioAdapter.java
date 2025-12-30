package com.example.codepractica.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepractica.DetalleProductoActivity;
import com.example.codepractica.R;
import com.example.codepractica.database.AppDatabase;
import com.example.codepractica.database.entities.Lista;
import com.example.codepractica.database.entities.Producto;
import com.example.codepractica.utils.FormHelper;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProductoRecordatorioAdapter extends RecyclerView.Adapter<ProductoRecordatorioAdapter.ProductoViewHolder> {

    private static final int REQUEST_CODE_DETALLE = 100;
    private List<Producto> listaProductos;
    private Context context;

    public ProductoRecordatorioAdapter(List<Producto> listaProductos, Context context) {
        this.listaProductos = listaProductos;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto_recordatorio, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        
        // Establecer el nombre del producto
        holder.tvNombre.setText(producto.nombre);
        
        // Obtener la categoría/inventario desde la base de datos
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            String categoria = "Sin categoría";
            
            if (producto.almacenado > 0) {
                try {
                    // Intentar obtener el nombre de la lista donde está almacenado
                    List<Lista> listas = db.listaDao().obtenerTodas();
                    for (Lista lista : listas) {
                        if (lista.id == producto.almacenado) {
                            categoria = lista.nombre;
                            break;
                        }
                    }
                } catch (Exception e) {
                    categoria = "Sin categoría";
                }
            }
            
            String categoriaFinal = categoria;
            ((android.app.Activity) context).runOnUiThread(() -> {
                holder.tvCategoria.setText(categoriaFinal);
            });
        }).start();
        
        // Mostrar indicador de caducidad
        if (producto.caducidad != null && producto.caducidad > 0) {
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
            // Mostrar imagen por defecto
            holder.ivImagen.setImageResource(R.drawable.ic_products);
            holder.ivImagen.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            holder.ivImagen.setPadding(8, 8, 8, 8);
        }
        
        // Configurar click listener para navegar al detalle
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleProductoActivity.class);
            intent.putExtra("producto_id", producto.id);
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, REQUEST_CODE_DETALLE);
            } else {
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }
    
    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagen;
        TextView tvNombre;
        TextView tvCategoria;
        LinearLayout layoutCaducidad;
        TextView tvCaducidad;
        View viewIndicador;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImagen = itemView.findViewById(R.id.ivProductoImagen);
            tvNombre = itemView.findViewById(R.id.tvProductoNombre);
            tvCategoria = itemView.findViewById(R.id.tvProductoCategoria);
            layoutCaducidad = itemView.findViewById(R.id.layoutCaducidad);
            tvCaducidad = itemView.findViewById(R.id.tvCaducidad);
            viewIndicador = itemView.findViewById(R.id.viewIndicadorCaducidad);
        }
    }
}
