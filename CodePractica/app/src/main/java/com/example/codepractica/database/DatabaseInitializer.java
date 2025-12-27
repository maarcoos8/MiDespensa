package com.example.codepractica.database;

import android.content.Context;

import com.example.codepractica.database.entities.Lista;
import com.example.codepractica.database.entities.Producto;

/**
 * Clase para inicializar la base de datos con datos de ejemplo
 */
public class DatabaseInitializer {

    public static void inicializarDatosEjemplo(Context context) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            
            // Comprobar si ya hay datos
            if (db.listaDao().obtenerTodas().size() > 0) {
                return; // Ya hay datos, no inicializar
            }

            // Crear listas de inventario
            Lista listaNevera = new Lista();
            listaNevera.nombre = "Nevera";
            listaNevera.descripcion = "Productos refrigerados";
            listaNevera.tipo = "ListaInventario";
            listaNevera.imagen = "nevera";
            long idNevera = db.listaDao().insertar(listaNevera);

            Lista listaDespensa = new Lista();
            listaDespensa.nombre = "Despensa";
            listaDespensa.descripcion = "Productos no perecederos";
            listaDespensa.tipo = "ListaInventario";
            listaDespensa.imagen = "despensa";
            long idDespensa = db.listaDao().insertar(listaDespensa);

            Lista listaBotiquin = new Lista();
            listaBotiquin.nombre = "Botiquín";
            listaBotiquin.descripcion = "Medicamentos y productos de salud";
            listaBotiquin.tipo = "ListaInventario";
            listaBotiquin.imagen = "botiquin";
            long idBotiquin = db.listaDao().insertar(listaBotiquin);

            // Crear listas de la compra
            Lista listaCompraNevera = new Lista();
            listaCompraNevera.nombre = "Compra nevera";
            listaCompraNevera.descripcion = "Productos refrigerados";
            listaCompraNevera.tipo = "ListaCompra";
            listaCompraNevera.imagen = "listanevera";
            long idCompraNevera = db.listaDao().insertar(listaCompraNevera);

            Lista listaCompraDespensa = new Lista();
            listaCompraDespensa.nombre = "Compra despensa";
            listaCompraDespensa.descripcion = "Productos no perecederos";
            listaCompraDespensa.tipo = "ListaCompra";
            listaCompraDespensa.imagen = "compradespensa";
            long idCompraDespensa = db.listaDao().insertar(listaCompraDespensa);

            Lista listaCompraFarmacia = new Lista();
            listaCompraFarmacia.nombre = "Compra farmacia";
            listaCompraFarmacia.descripcion = "Medicamentos y productos de salud";
            listaCompraFarmacia.tipo = "ListaCompra";
            listaCompraFarmacia.imagen = "farmacia";
            long idCompraFarmacia = db.listaDao().insertar(listaCompraFarmacia);

            // Crear productos de ejemplo
            
            // Productos de nevera
            Producto lecheEntera = new Producto();
            lecheEntera.nombre = "Leche Entera";
            lecheEntera.descripcion = "Leche entera pasteurizada 1L";
            lecheEntera.unidades = 2;
            lecheEntera.caducidad = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L); // 7 días
            lecheEntera.imagen = "lecheentera";
            lecheEntera.almacenado = (int) idNevera;
            lecheEntera.lista_inventario = (int) idNevera;
            lecheEntera.lista_compra = (int) idCompraNevera;
            db.productoDao().insertar(lecheEntera);

            Producto huevosL = new Producto();
            huevosL.nombre = "Huevos L";
            huevosL.descripcion = "Huevos tamaño L, docena";
            huevosL.unidades = 12;
            huevosL.caducidad = System.currentTimeMillis() + (14 * 24 * 60 * 60 * 1000L); // 14 días
            huevosL.imagen = "huevos";
            huevosL.almacenado = (int) idNevera;
            huevosL.lista_inventario = (int) idNevera;
            huevosL.lista_compra = (int) idCompraNevera;
            db.productoDao().insertar(huevosL);

            // Productos de botiquín
            Producto ibuprofeno = new Producto();
            ibuprofeno.nombre = "Ibuprofeno 600mg";
            ibuprofeno.descripcion = "Ibuprofeno 600mg, caja de 20 comprimidos";
            ibuprofeno.unidades = 20;
            ibuprofeno.caducidad = System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L); // 1 año
            ibuprofeno.imagen = "ibuprofeno";
            ibuprofeno.almacenado = (int) idBotiquin;
            ibuprofeno.lista_inventario = (int) idBotiquin;
            ibuprofeno.lista_compra = (int) idCompraFarmacia;
            db.productoDao().insertar(ibuprofeno);

            // Productos de despensa
            Producto macarrones = new Producto();
            macarrones.nombre = "Macarrones Gallo";
            macarrones.descripcion = "Pasta macarrones 500g";
            macarrones.unidades = 3;
            macarrones.caducidad = System.currentTimeMillis() + (730 * 24 * 60 * 60 * 1000L); // 2 años
            macarrones.imagen = "macarronesgallo";
            macarrones.almacenado = (int) idDespensa;
            macarrones.lista_inventario = (int) idDespensa;
            macarrones.lista_compra = (int) idCompraDespensa;
            db.productoDao().insertar(macarrones);

            Producto aceiteOliva = new Producto();
            aceiteOliva.nombre = "Aceite de Oliva";
            aceiteOliva.descripcion = "Aceite de oliva virgen extra 1L";
            aceiteOliva.unidades = 1;
            aceiteOliva.imagen = "aceite";
            aceiteOliva.almacenado = (int) idDespensa;
            aceiteOliva.lista_inventario = (int) idDespensa;
            aceiteOliva.lista_compra = (int) idCompraDespensa;
            db.productoDao().insertar(aceiteOliva);

        }).start();
    }
}
