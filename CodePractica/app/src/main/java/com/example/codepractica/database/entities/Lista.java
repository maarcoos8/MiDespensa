package com.example.codepractica.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lista")
public class Lista {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String nombre;

    public String descripcion;

    @NonNull
    public String tipo; // Representa el enum 'tipoLista'
    /*
        ListaCompra -> Para definir que es una lista de la compra
        ListaInventario -> Para definir que es una lista de inventario
     */

    public String imagen;
}