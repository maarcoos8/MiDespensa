# MiDespensa - Documentación del Proyecto

## Descripción
Aplicación Android para gestionar el inventario del hogar, listas de compra y productos con fechas de caducidad. Permite organizar productos en diferentes ubicaciones (Nevera, Despensa, Botiquín) y realizar un seguimiento completo de los mismos.

## Tecnologías
- **Lenguaje**: Java
- **UI**: XML (Android Layouts)
- **Base de Datos**: SQLite con Room Persistence Library
- **Arquitectura**: MVVM (Model-View-ViewModel) con DAOs
- **IDE**: Android Studio

## Estructura del Proyecto

```
MiDespensa/
├── README.md
├── AGENTS.md
└── CodePractica/
    ├── build.gradle.kts
    ├── gradle.properties
    ├── gradlew
    ├── gradlew.bat
    ├── local.properties
    ├── settings.gradle.kts
    ├── gradle/
    │   ├── libs.versions.toml
    │   └── wrapper/
    │       └── gradle-wrapper.properties
    └── app/
        ├── build.gradle.kts
        ├── proguard-rules.pro
        ├── src/
        │   ├── androidTest/
        │   │   └── java/
        │   │       └── com/example/codepractica/
        │   │           └── ExampleInstrumentedTest.java
        │   ├── test/
        │   │   └── java/
        │   │       └── com/example/codepractica/
        │   │           └── ExampleUnitTest.java
        │   └── main/
        │       ├── AndroidManifest.xml
        │       ├── java/
        │       │   └── com/example/codepractica/
        │       │       ├── MainActivity.java
        │       │       ├── ProductosActivity.java
        │       │       ├── DetalleProductoActivity.java
        │       │       ├── adapters/
        │       │       │   └── ProductoAdapter.java
        │       │       └── database/
        │       │           ├── AppDatabase.java
        │       │           ├── DatabaseInitializer.java
        │       │           ├── entities/
        │       │           │   ├── Producto.java
        │       │           │   └── Lista.java
        │       │           └── daos/
        │       │               ├── ProductoDao.java
        │       │               └── ListaDao.java
        │       └── res/
        │           ├── layout/
        │           │   ├── activity_main.xml
        │           │   ├── activity_productos.xml
        │           │   ├── activity_detalle_producto.xml
        │           │   └── item_producto.xml
        │           ├── drawable/
        │           │   ├── ic_home.png
        │           │   ├── ic_inventory.png
        │           │   ├── ic_inventory_scaled.xml
        │           │   ├── ic_shopping_list.png
        │           │   ├── ic_shopping_list_scaled.xml
        │           │   ├── ic_products.png
        │           │   ├── ic_products_scaled.xml
        │           │   ├── ic_exit.png
        │           │   ├── ic_exit_scaled.xml
        │           │   ├── ic_back.xml
        │           │   ├── ic_search.xml
        │           │   ├── ic_add_circle.xml
        │           │   ├── ic_arrow_right.xml
        │           │   ├── ic_settings.xml
        │           │   ├── ic_category.xml
        │           │   ├── ic_shopping_cart.xml
        │           │   ├── ic_quantity.xml
        │           │   ├── ic_calendar.xml
        │           │   ├── ic_kitchen.xml
        │           │   ├── ic_delete.xml
        │           │   ├── ic_launcher_background.xml
        │           │   ├── ic_launcher_foreground.xml
        │           │   └── bg_button_delete.xml
        │           ├── mipmap-*/
        │           │   └── ic_launcher.png
        │           ├── values/
        │           │   ├── strings.xml
        │           │   ├── colors.xml
        │           │   └── themes.xml
        │           ├── values-night/
        │           │   └── themes.xml
        │           └── xml/
        │               ├── backup_rules.xml
        │               └── data_extraction_rules.xml
        └── build/
            └── [archivos generados por Gradle]
```

## Base de Datos SQLite

### Estructura de la Base de Datos
**Nombre**: `despensa_db`

### Tablas

#### 1. `producto`
Almacena información de todos los productos del hogar.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | INTEGER | PRIMARY KEY, autoincremental |
| `nombre` | TEXT | Nombre del producto (NOT NULL) |
| `descripcion` | TEXT | Descripción detallada |
| `unidades` | REAL | Cantidad/unidades disponibles |
| `caducidad` | INTEGER | Fecha de caducidad (timestamp) |
| `imagen` | TEXT | Ruta de la imagen del producto |
| `lista_compra` | INTEGER | FK a `lista.id` (lista de compra) |
| `lista_inventario` | INTEGER | FK a `lista.id` (lista de inventario) |
| `almacenado` | INTEGER | ID de la lista donde está actualmente |

#### 2. `lista`
Almacena las diferentes listas (inventarios y listas de compra).

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | INTEGER | PRIMARY KEY, autoincremental |
| `nombre` | TEXT | Nombre de la lista (NOT NULL) |
| `descripcion` | TEXT | Descripción de la lista |
| `tipo` | TEXT | Tipo: "ListaInventario" o "ListaCompra" (NOT NULL) |
| `imagen` | TEXT | Ruta de la imagen |

### DAOs (Data Access Objects)

#### ProductoDao
- `insertar(Producto)` - Inserta un producto
- `actualizar(Producto)` - Actualiza un producto
- `eliminar(Producto)` - Elimina un producto
- `obtenerTodos()` - Obtiene todos los productos
- `obtenerPorLista(int listaId)` - Obtiene productos de una lista específica
- `moverDeLista(int productoId, int nuevaListaId)` - Mueve un producto entre listas

#### ListaDao
- `insertar(Lista)` - Inserta una lista
- `actualizar(Lista)` - Actualiza una lista
- `eliminar(Lista)` - Elimina una lista
- `obtenerTodas()` - Obtiene todas las listas
- `obtenerPorTipo(String tipo)` - Obtiene listas por tipo

## Pantallas Implementadas

### 1. MainActivity
**Layout**: `activity_main.xml`
- Pantalla principal con botones para navegar
- Botones: Ver Inventarios, Ver Listas de Compra, Ver Productos, Salir
- Inicializa la base de datos con datos de ejemplo

### 2. ProductosActivity
**Layout**: `activity_productos.xml`
- Lista de todos los productos
- Barra de búsqueda en tiempo real
- RecyclerView con ProductoAdapter
- Barra de navegación inferior (Productos, Inventario, Añadir, Lista, Ajustes)

**Item Layout**: `item_producto.xml`
- Imagen del producto
- Nombre del producto
- Categoría/ubicación
- Flecha de navegación

### 3. DetalleProductoActivity
**Layout**: `activity_detalle_producto.xml`
- Vista detallada de un producto
- Imagen grande del producto
- Información completa: nombre, descripción, cantidad, caducidad
- Sección de organización: ubicación y lista de compra
- Botón de eliminar con confirmación
- Botón de editar (preparado para implementación futura)

## Adaptadores

### ProductoAdapter
- Adaptador para RecyclerView
- Vincula datos de productos con vistas
- Maneja clicks para navegar al detalle
- Carga dinámica de categorías desde la BD

## Inicialización de Datos

### DatabaseInitializer
Crea datos de ejemplo al iniciar la aplicación por primera vez:

**Listas creadas**:
- Nevera (ListaInventario)
- Despensa (ListaInventario)
- Botiquín (ListaInventario)

**Productos de ejemplo**:
- Leche Entera (Nevera)
- Huevos L (Nevera)
- Ibuprofeno 600mg (Botiquín)
- Macarrones Gallo (Despensa)
- Aceite de Oliva (Despensa)

## Paleta de Colores

- **Verde Principal**: #10B981 / #2BEE79
- **Verde Claro**: #34D399 / #A7F3D0
- **Azul**: #2196F3
- **Naranja**: #FF9800
- **Rojo**: #F44336
- **Gris Fondo**: #FAFAFA / #F6F8F7
- **Gris Texto**: #6B7280 / #999999

## Características Principales

✅ **Gestión de Productos**
- Crear, leer, actualizar, eliminar (CRUD)
- Búsqueda en tiempo real
- Organización por ubicaciones

✅ **Base de Datos**
- SQLite con Room
- Relaciones entre tablas (Foreign Keys)
- Consultas eficientes con DAOs

✅ **Interfaz de Usuario**
- Diseño moderno con Material Design
- Navegación intuitiva
- Búsqueda y filtrado
- Tarjetas visuales con información clara

✅ **Funcionalidades**
- Seguimiento de fechas de caducidad
- Control de cantidades/unidades
- Asociación con listas de compra
- Eliminación con confirmación

## Requisitos del Sistema

- **Android SDK**: API 21+ (Android 5.0 Lollipop o superior)
- **Build Tools**: Gradle 8.0+
- **Java**: JDK 8 o superior
- **Android Studio**: Arctic Fox o superior

## Dependencias Principales

- Room Persistence Library (para SQLite)
- AndroidX AppCompat
- RecyclerView
- CardView
- Material Components

## Notas para Desarrollo

- Todas las operaciones de BD se ejecutan en hilos secundarios
- Las actualizaciones de UI se hacen en el hilo principal con `runOnUiThread()`
- Se usa `allowMainThreadQueries()` solo para pruebas (eliminar en producción)
- Los nombres de las listas se usan como categorías para los productos
- El campo `almacenado` indica la ubicación actual del producto
