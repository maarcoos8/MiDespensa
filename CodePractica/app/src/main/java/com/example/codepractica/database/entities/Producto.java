package com.example.codepractica.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "producto",
        foreignKeys = {
                @ForeignKey(entity = Lista.class,
                        parentColumns = "id",
                        childColumns = "lista_compra"),
                @ForeignKey(entity = Lista.class,
                        parentColumns = "id",
                        childColumns = "lista_inventario")
        })
public class Producto {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String nombre;

    public String descripcion;

    public float unidades;

    public Long caducidad; // Los timestamps se manejan como Long en Room

    public String imagen;

    public int lista_compra; // Clave ajena a lista(id)

    public int lista_inventario; // Clave ajena a lista(id)

    public int almacenado; // Indica el ID de la lista donde está actualmente
}
