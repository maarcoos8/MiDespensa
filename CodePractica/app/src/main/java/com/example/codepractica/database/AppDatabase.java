package com.example.codepractica.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.codepractica.database.entities.Lista;
import com.example.codepractica.database.entities.Producto;
import com.example.codepractica.database.daos.ProductoDao;
import com.example.codepractica.database.daos.ListaDao;


// Definimos las entidades (tablas) y la versión de la base de datos
@Database(entities = {Producto.class, Lista.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    // Nombre que tendrá el archivo de la base de datos en el móvil
    private static final String DATABASE_NAME = "despensa_db";
    private static AppDatabase instance;

    // Aquí declaramos los DAOs que darán acceso a las tablas
    public abstract ProductoDao productoDao();
    public abstract ListaDao listaDao();

    // Método Singleton para obtener la instancia de la base de datos
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration() // Útil durante el desarrollo
                    .allowMainThreadQueries() // Solo para pruebas rápidas en la uni
                    .build();
        }
        return instance;
    }
}