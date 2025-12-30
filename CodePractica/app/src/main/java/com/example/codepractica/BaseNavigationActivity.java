package com.example.codepractica;

import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Clase base abstracta para actividades con barra de navegación inferior.
 * Proporciona funcionalidad común para configurar la navegación entre pantallas.
 */
public abstract class BaseNavigationActivity extends AppCompatActivity {

    /**
     * Enum para identificar la sección activa de la navegación
     */
    public enum NavigationSection {
        PRODUCTOS,
        INVENTARIO,
        INICIO,
        LISTA_COMPRA,
        RECORDATORIO,
        NONE  // Para pantallas que no corresponden a ninguna sección específica
    }

    /**
     * Método abstracto que cada actividad debe implementar para indicar qué sección está activa
     */
    protected abstract NavigationSection getActiveNavigationSection();

    /**
     * Configura la barra de navegación inferior con los listeners y el estado visual
     */
    protected void configurarBarraNavegacion() {
        // Productos
        findViewById(R.id.navProductos).setOnClickListener(v -> {
            if (getActiveNavigationSection() != NavigationSection.PRODUCTOS) {
                startActivity(new Intent(this, ProductosActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        // Inventario
        findViewById(R.id.navInventario).setOnClickListener(v -> {
            if (getActiveNavigationSection() != NavigationSection.INVENTARIO) {
                Intent intent = new Intent(this, ListasActivity.class);
                intent.putExtra(ListasActivity.EXTRA_TIPO_LISTA, ListasActivity.TIPO_INVENTARIO);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        // Inicio
        findViewById(R.id.navInicio).setOnClickListener(v -> {
            if (getActiveNavigationSection() != NavigationSection.INICIO) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        // Lista de compra
        findViewById(R.id.navLista).setOnClickListener(v -> {
            if (getActiveNavigationSection() != NavigationSection.LISTA_COMPRA) {
                Intent intent = new Intent(this, ListasActivity.class);
                intent.putExtra(ListasActivity.EXTRA_TIPO_LISTA, ListasActivity.TIPO_COMPRA);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        // Recordatorio
        findViewById(R.id.navRecordatorio).setOnClickListener(v -> {
            if (getActiveNavigationSection() != NavigationSection.RECORDATORIO) {
                startActivity(new Intent(this, RecordatorioActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        // Actualizar el estado visual
        actualizarEstadoNavegacion();
    }

    /**
     * Actualiza los colores e iconos de la navegación según la sección activa
     */
    protected void actualizarEstadoNavegacion() {
        int colorGris = getResources().getColor(R.color.gris_claro, null);
        int colorVerde = getResources().getColor(R.color.verde_principal, null);

        // Referencias a los elementos de navegación
        ImageView navProductosIcon = findViewById(R.id.navProductosIcon);
        TextView navProductosText = findViewById(R.id.navProductosText);
        ImageView navInventarioIcon = findViewById(R.id.navInventarioIcon);
        TextView navInventarioText = findViewById(R.id.navInventarioText);
        ImageView navInicioIcon = findViewById(R.id.navInicioIcon);
        TextView navInicioText = findViewById(R.id.navInicioText);
        ImageView navListaIcon = findViewById(R.id.navListaIcon);
        TextView navListaText = findViewById(R.id.navListaText);
        ImageView navRecordatorioIcon = findViewById(R.id.navRecordatorioIcon);
        TextView navRecordatorioText = findViewById(R.id.navRecordatorioText);

        // Resetear todos a gris
        navProductosIcon.setColorFilter(colorGris);
        navProductosText.setTextColor(colorGris);
        navInventarioIcon.setColorFilter(colorGris);
        navInventarioText.setTextColor(colorGris);
        navInicioIcon.setColorFilter(colorGris);
        navInicioText.setTextColor(colorGris);
        navListaIcon.setColorFilter(colorGris);
        navListaText.setTextColor(colorGris);
        navRecordatorioIcon.setColorFilter(colorGris);
        navRecordatorioText.setTextColor(colorGris);

        // Activar el color verde para la sección activa
        switch (getActiveNavigationSection()) {
            case PRODUCTOS:
                navProductosIcon.setColorFilter(colorVerde);
                navProductosText.setTextColor(colorVerde);
                break;
            case INVENTARIO:
                navInventarioIcon.setColorFilter(colorVerde);
                navInventarioText.setTextColor(colorVerde);
                break;
            case INICIO:
                navInicioIcon.setColorFilter(colorVerde);
                navInicioText.setTextColor(colorVerde);
                break;
            case LISTA_COMPRA:
                navListaIcon.setColorFilter(colorVerde);
                navListaText.setTextColor(colorVerde);
                break;
            case RECORDATORIO:
                navRecordatorioIcon.setColorFilter(colorVerde);
                navRecordatorioText.setTextColor(colorVerde);
                break;
            case NONE:
                // No activar ninguno
                break;
        }
    }
}
