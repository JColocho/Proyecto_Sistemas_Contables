# Pasos para ejecución del proyecto
## 📋 Requisitos previos

Antes de ejecutar el proyecto debe tener instalado lo siguiente:
- [Java Development Kit (JDK) 21](https://www.oracle.com/java/technologies/downloads/#java21)
- [JavaFX SDK (si tu IDE no lo incluye)](https://openjfx.io/)
- [PostgreSQL 14 o superior](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads)
- IDE recomendado: IntelliJ IDEA, NetBeans o Eclipse con soporte para JavaFX.

### ⚙️ Configuración de la base de datos

1. **Crea la base de datos vacía en PostgreSQL:**
   ```sql
   CREATE DATABASE db_sistema_contable;

2. **Restaura el respaldo incluido en el repositorio**

   Dentro del proyecto encontrarás una carpeta llamada database con un archivo sql llamado db_sistema_contable, para restaurarlo debes hacer lo siguiente:

    - Abre pgAdmin.
    - Haz clic derecho sobre la base de datos creada anteriormente y selecciona "Restore".
    - Selecciona el archivo sql que se encuentra en el proyecto.
    - Presiona Restore


### 🌍 Configuración de conexión

 - **En la clase de conexión `ConexionDB.java`, asegúrate de tener los valores correctos para conectar tu aplicación con PostgreSQL:**
   ```java
   private static final String URL = "jdbc:postgresql://localhost:5432/db_sistema_contable";
   private static final String USER = "tu_usuario";
   private static final String PASSWORD = "tu_contraseña";
 
### 🚀 Ejecución del proyecto
**Desde un IDE (recomendado)**
- Abrir el proyecto con el IDE instalado
- Ejecuta la clase principal llamada "Main" que se encuentra en src/main/java/com/proyecto_sistemas_contables/Main.java

### 🖥️ Ejecución de la aplicación
Al estar en ejecución el proyecto se abrirá el formulario de inicio de sesión pero puede crearse un nuevo usuario y con eso estará listo para usar las funciones del proyecto.

### Nota
El archivo clavesAccesos.txt están las claves de acceso para la creación de un nuevo usuario

### Autores
- Jairo Alexander Argueta Alvarenga
- Josué Iván Molina Romero
- Javier Orlando Colocho Bolainez

### Repositorio
[Sistema Contable](https://github.com/JColocho/Proyecto_Sistemas_Contables)

### Aclaraciones
Por motivos de tiempo solo se hizo registro de partidas con un usuario predefinido y una empresa predefinida con su propio catálogo de cuentas, 
permite registrar partidas y el login si está completo con su respectivo registro de usuario, generación de reporte de Libro Diario y Mayor.
