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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepractica.DetalleProductoActivity;
import com.example.codepractica.R;
import com.example.codepractica.database.AppDatabase;
import com.example.codepractica.database.entities.Lista;
import com.example.codepractica.database.entities.Producto;

import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private static final int REQUEST_CODE_DETALLE = 100;
    private List<Producto> listaProductos;
    private Context context;

    public ProductoAdapter(List<Producto> listaProductos, Context context) {
        this.listaProductos = listaProductos;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        
        // Establecer el nombre del producto
        holder.tvNombre.setText(producto.nombre);
        
        // Obtener la categoría desde la base de datos
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
        ImageView ivImagen;
        TextView tvNombre;
        TextView tvCategoria;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImagen = itemView.findViewById(R.id.ivProductoImagen);
            tvNombre = itemView.findViewById(R.id.tvProductoNombre);
            tvCategoria = itemView.findViewById(R.id.tvProductoCategoria);
        }
    }
}
