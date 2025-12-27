package com.example.codepractica.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepractica.R;
import com.example.codepractica.database.entities.Lista;

import java.util.List;

public class ListaAdapter extends RecyclerView.Adapter<ListaAdapter.ListaViewHolder> {

    private List<Lista> listaListas;
    private List<Integer> cantidadProductos;
    private Context context;
    private OnListaClickListener listener;

    public interface OnListaClickListener {
        void onListaClick(Lista lista);
        void onMenuClick(Lista lista, View view);
    }

    public ListaAdapter(List<Lista> listaListas, List<Integer> cantidadProductos, Context context) {
        this.listaListas = listaListas;
        this.cantidadProductos = cantidadProductos;
        this.context = context;
    }

    public void setOnListaClickListener(OnListaClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lista, parent, false);
        return new ListaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListaViewHolder holder, int position) {
        Lista lista = listaListas.get(position);
        int cantidad = cantidadProductos.get(position);

        holder.tvNombre.setText(lista.nombre);
        
        // Formatear el texto de cantidad
        String textoArticulos = cantidad + (cantidad == 1 ? " artículo" : " artículos");
        holder.tvCantidad.setText(textoArticulos);

        // Configurar la imagen según el tipo de lista
        if (lista.imagen != null && !lista.imagen.isEmpty()) {
            cargarImagen(holder.ivImagen, lista.imagen);
        } else {
            // Mostrar icono por defecto según el tipo
            if ("ListaInventario".equals(lista.tipo)) {
                holder.ivImagen.setImageResource(R.drawable.ic_inventory);
            } else {
                holder.ivImagen.setImageResource(R.drawable.ic_shopping_list);
            }
            holder.ivImagen.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        // Click en el item completo
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onListaClick(lista);
            }
        });

        // Click en el botón de menú
        holder.btnMenu.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMenuClick(lista, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaListas.size();
    }
    
    private void cargarImagen(ImageView imageView, String imagePath) {
        if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
            try {
                imageView.setImageURI(Uri.parse(imagePath));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.ic_shopping_list);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        } else {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    imageView.setImageResource(R.drawable.ic_shopping_list);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.ic_shopping_list);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        }
    }

    static class ListaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvCantidad;
        ImageButton btnMenu;
        android.widget.ImageView ivImagen;

        public ListaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvListaNombre);
            tvCantidad = itemView.findViewById(R.id.tvListaCantidad);
            btnMenu = itemView.findViewById(R.id.btnMenuLista);
            ivImagen = itemView.findViewById(R.id.ivImagenLista);
        }
    }
}
