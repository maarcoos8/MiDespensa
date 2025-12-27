package com.example.codepractica.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
            cargarImagen(holder.ivImagen, producto.imagen);
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
    
    private void cargarImagen(ImageView imageView, String imagePath) {
        // Primero intentar cargar como recurso drawable
        try {
            int resourceId = context.getResources().getIdentifier(imagePath, "drawable", context.getPackageName());
            if (resourceId != 0) {
                imageView.setImageResource(resourceId);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(0, 0, 0, 0);
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
                imageView.setPadding(0, 0, 0, 0);
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
                    imageView.setPadding(0, 0, 0, 0);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Si todo falla, usar imagen por defecto
        imageView.setImageResource(R.drawable.ic_products);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setPadding(8, 8, 8, 8);
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
