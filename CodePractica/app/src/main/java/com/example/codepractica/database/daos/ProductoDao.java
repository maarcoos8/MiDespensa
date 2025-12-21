package com.example.codepractica.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.codepractica.database.entities.Producto;

import java.util.List;

@Dao
public interface ProductoDao {

    @Insert
    void insertar(Producto producto);

    @Update
    void actualizar(Producto producto);

    @Delete
    void eliminar(Producto producto);

    // Obtener todos los productos
    @Query("SELECT * FROM producto")
    List<Producto> obtenerTodos();

    // Obtener productos que están actualmente en una lista específica (Compra o Inventario)
    @Query("SELECT * FROM producto WHERE almacenado = :listaId")
    List<Producto> obtenerPorLista(int listaId);

    // EL MODO "MOVER": Cambia el estado de almacenado de un producto
    @Query("UPDATE producto SET almacenado = :nuevaListaId WHERE id = :productoId")
    void moverDeLista(int productoId, int nuevaListaId);
}