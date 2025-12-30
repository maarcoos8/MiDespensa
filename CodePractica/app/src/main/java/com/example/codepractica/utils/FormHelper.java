package com.example.codepractica.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.codepractica.R;

/**
 * Clase helper con métodos reutilizables para las actividades de creación/edición
 */
public class FormHelper {

    /**
     * Muestra una imagen en un ImageView desde diferentes fuentes
     */
    public static void mostrarImagen(Context context, ImageView imageView, LinearLayout layoutPlaceholder, String imagePath) {
        imageView.setVisibility(View.VISIBLE);
        layoutPlaceholder.setVisibility(View.GONE);
        
        // Primero intentar cargar como recurso drawable
        try {
            int resourceId = context.getResources().getIdentifier(imagePath, "drawable", context.getPackageName());
            if (resourceId != 0) {
                imageView.setImageResource(resourceId);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Si no es un recurso, intentar cargar como URI o ruta de archivo
        if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
            imageView.setImageURI(Uri.parse(imagePath));
        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    /**
     * Carga una imagen en un ImageView para RecyclerViews (con manejo de errores y escala)
     */
    public static void cargarImagenEnAdapter(Context context, ImageView imageView, String imagePath, int defaultImageResource) {
        // Primero intentar cargar como recurso drawable
        try {
            int resourceId = context.getResources().getIdentifier(imagePath, "drawable", context.getPackageName());
            if (resourceId != 0) {
                imageView.setImageResource(resourceId);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
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
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Si todo falla, usar imagen por defecto
        imageView.setImageResource(defaultImageResource);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    /**
     * Oculta una imagen y muestra el placeholder
     */
    public static void ocultarImagen(ImageView imageView, LinearLayout layoutPlaceholder) {
        imageView.setVisibility(View.GONE);
        layoutPlaceholder.setVisibility(View.VISIBLE);
        imageView.setImageDrawable(null);
    }

    /**
     * Muestra un diálogo de confirmación al salir sin guardar
     */
    public static void mostrarDialogoSalir(AppCompatActivity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Salir sin guardar")
                .setMessage("Los cambios no se harán efectivos si sales ahora. ¿Estás seguro de que quieres volver atrás?")
                .setPositiveButton("Salir", (dialog, which) -> activity.finish())
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
