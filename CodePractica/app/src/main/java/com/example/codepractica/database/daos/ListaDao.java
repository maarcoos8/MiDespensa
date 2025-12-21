package com.example.codepractica.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.codepractica.database.entities.Lista;

import java.util.List;

@Dao
public interface ListaDao {
    @Insert
    long insertar(Lista lista);

    @Update
    void actualizar(Lista lista);

    @Delete
    void eliminar(Lista lista);

    @Query("SELECT * FROM lista")
    List<Lista> obtenerTodas();

    @Query("SELECT * FROM lista WHERE tipo = :tipo")
    List<Lista> obtenerPorTipo(String tipo);
}