package com.example.codepractica.adapters;

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
import com.example.codepractica.database.entities.Producto;

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
            cargarImagen(holder.ivImagen, producto.imagen);
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
    
    private void cargarImagen(ImageView imageView, String imagePath) {
        if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
            try {
                imageView.setImageURI(Uri.parse(imagePath));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.ic_products);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setPadding(8, 8, 8, 8);
            }
        } else {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setPadding(0, 0, 0, 0);
                } else {
                    imageView.setImageResource(R.drawable.ic_products);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    imageView.setPadding(8, 8, 8, 8);
                }
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.ic_products);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setPadding(8, 8, 8, 8);
            }
        }
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvCantidad;
        LinearLayout layoutCaducidad;
        TextView tvCaducidad;
        View viewIndicador;
        ImageView ivImagen;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvProductoNombre);
            tvCantidad = itemView.findViewById(R.id.tvProductoCantidad);
            layoutCaducidad = itemView.findViewById(R.id.layoutCaducidad);
            tvCaducidad = itemView.findViewById(R.id.tvCaducidad);
            viewIndicador = itemView.findViewById(R.id.viewIndicadorCaducidad);
            ivImagen = itemView.findViewById(R.id.ivProductoImagen);
        }
    }
}
