package com.proyecto_sistemas_contables.models;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class UsuarioModel {
    private int idUsuario;
    private String nombreUsuario;
    private String nombre;
    private String apellido;
    private String clave;
    private int idCorreo;
    private int idAcceso;

    private String correo;
    private String nivelAcceso;
    private Boolean activo;


    public UsuarioModel() {
    }

    public UsuarioModel(String nombreUsuario, String nombre, String apellido, String clave, int idCorreo, int idAcceso) {
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.clave = clave;
        this.idCorreo = idCorreo;
        this.idAcceso = idAcceso;
    }

    public UsuarioModel(int idUsuario, String nombreUsuario, String nombre, String apellido, String correo,Boolean activo, String nivelAcceso) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.activo = activo;
        this.nivelAcceso = nivelAcceso;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public int getIdCorreo() {
        return idCorreo;
    }

    public void setIdCorreo(int idCorreo) {
        this.idCorreo = idCorreo;
    }

    public int getIdAcceso() {
        return idAcceso;
    }

    public void setIdAcceso(int idAcceso) {
        this.idAcceso = idAcceso;
    }

    public String getCorreo(){return correo;}
    public void setCorreo(String correo){this.correo = correo;}

    public String getNivelAcceso() { return nivelAcceso; }
    public void setNivelAcceso(String nivelAcceso) { this.nivelAcceso = nivelAcceso; }

    public Boolean isActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    //Metodo para crear usuario (por defecto activo es true, no hace falta colocarlo)
    public void crearUsuario(String nombreUsuario, String nombre, String apellido, String clave, int idCorreo, int idAcceso) {
        try {
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO tblusuarios(nombreusuario, nombre, apellido, idcorreo, idacceso, clave)" +
                    "VALUES(?, ?, ?, ?, ?, ?)");
            statement.setString(1, nombreUsuario);
            statement.setString(2, nombre);
            statement.setString(3, apellido);
            statement.setInt(4, idCorreo);
            statement.setInt(5, idAcceso);
            clave = Encripter.encrypt(clave);
            statement.setString(6, clave);
            System.out.println(statement.executeUpdate()); //todo aqui se muestra un 1 en consola
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    //Metodo para validar el inicio de sesión
    public boolean inicioSesion(String nombreUsuario, String clave) {
        try{
            //Hacemos la conexión con la base de datos
            Connection connection = ConexionDB.connection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM tblusuarios WHERE nombreusuario='" + nombreUsuario + "'");

            //Obtenemos los registros encontrados
            while (resultSet.next()) {
                //Almacenamos la contraseña
                UsuarioModel usuario = new UsuarioModel();
                usuario.setNombreUsuario(resultSet.getString("nombreusuario"));
                usuario.setClave(resultSet.getString("clave"));

                //Validamos si la contraseña ingresa es indentica a la almacenada
                if(Encripter.verificarClave(clave, usuario.getClave())) {
                    return true;
                }
                else{
                    return false;
                }
            }

            resultSet.close();
            //Dado caso no existe el usuario ingresado
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Metodo para validar si el usuario ya existe
    public boolean usuarioExistente(String nombreUsuario) {
        try {
            Connection connection = ConexionDB.connection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM tblusuarios WHERE nombreusuario='" + nombreUsuario + "'");

            while (resultSet.next()) {
                return true;
            }
            resultSet.close();
            return false;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int idUsuarioSesion(String nombreUsuario) {
        try {
            Connection connection = ConexionDB.connection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM tblusuarios WHERE nombreusuario='" + nombreUsuario + "'");

            while (resultSet.next()) {
                return resultSet.getInt("idusuario");
            }
            resultSet.close();
            return -1;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String obtenerNombreUsuario(int idUsuario) {
        try {
            Connection connection = ConexionDB.connection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM tblusuarios WHERE idusuario='" + idUsuario + "'");

            while (resultSet.next()) {
                return resultSet.getString("nombreusuario");
            }
            resultSet.close();
            return "";

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    // método para obtener usuarios (excluye el de la sesión actual)
    public static ObservableList<UsuarioModel> obtenerUsuarios(int idUsuarioSesion) {
        ObservableList<UsuarioModel> lista = FXCollections.observableArrayList();
        String sql = """
        SELECT u.idusuario, u.nombreusuario, u.nombre, u.apellido,\s
               c.correo, u.activo, a.nivelacceso
        FROM tblusuarios u
        INNER JOIN tblcorreos c ON u.idcorreo = c.idcorreo
        INNER JOIN tblaccesos a ON u.idacceso = a.idacceso
        WHERE u.idusuario <> ?
        ORDER BY u.idusuario ASC
    """;

        try (Connection connection = ConexionDB.connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idUsuarioSesion); // Excluir usuario actual

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                lista.add(new UsuarioModel(
                        rs.getInt("idusuario"),
                        rs.getString("nombreusuario"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("correo"),
                        rs.getBoolean("activo"),
                        rs.getString("nivelacceso")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    // metodo para desactivar usuario
    public static void desactivarUsuario(int idUsuario) {
        String sql = "UPDATE tblusuarios SET activo = false WHERE idusuario = ?";

        try (Connection conn = ConexionDB.connection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // metodo para activar usuario
    public static void activarUsuario(int idUsuario) {
        String sql = "UPDATE tblusuarios SET activo = true WHERE idusuario = ?";

        try (Connection conn = ConexionDB.connection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Método para obtener un usuario por su ID
    public static UsuarioModel obtenerUsuarioPorId(int idUsuario) {
        try {
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT u.*, a.nivelacceso, c.correo FROM tblusuarios u " +
                            "INNER JOIN tblaccesos a ON u.idacceso = a.idacceso " +
                            "INNER JOIN tblcorreos c ON u.idcorreo = c.idcorreo " +
                            "WHERE u.idusuario = ?"
            );
            statement.setInt(1, idUsuario);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                UsuarioModel usuario = new UsuarioModel();
                usuario.setIdUsuario(rs.getInt("idusuario"));
                usuario.setNombreUsuario(rs.getString("nombreusuario"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setApellido(rs.getString("apellido"));
                usuario.setCorreo(rs.getString("correo"));
                usuario.setIdCorreo(rs.getInt("idcorreo"));
                usuario.setIdAcceso(rs.getInt("idacceso"));
                usuario.setNivelAcceso(rs.getString("nivelacceso"));
                usuario.setActivo(rs.getBoolean("activo"));
                return usuario;
            }

            return null;
        } catch (SQLException e) {
            System.out.println("Error al obtener usuario: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Método para verificar si un nombre de usuario ya existe
    public static boolean nombreUsuarioExiste(String nombreUsuario) {
        try {
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) as total FROM tblusuarios WHERE nombreusuario = ?"
            );
            statement.setString(1, nombreUsuario);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
            return false;
        } catch (SQLException e) {
            System.out.println("Error al verificar nombre de usuario: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Método para verificar si un correo ya existe
    public static boolean correoExiste(String correo) {
        try {
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) as total FROM tblcorreos WHERE correo = ?"
            );
            statement.setString(1, correo);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
            return false;
        } catch (SQLException e) {
            System.out.println("Error al verificar correo: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Método para actualizar usuario
    public static boolean actualizarUsuario(UsuarioModel usuario, boolean cambiarClave) {
        try {
            Connection connection = ConexionDB.connection();

            // Primero actualizamos el correo en tblcorreos
            PreparedStatement stmtCorreo = connection.prepareStatement(
                    "UPDATE tblcorreos SET correo = ? WHERE idcorreo = ?"
            );
            stmtCorreo.setString(1, usuario.getCorreo());
            stmtCorreo.setInt(2, usuario.getIdCorreo());
            stmtCorreo.executeUpdate();

            // Ahora actualizamos el usuario
            String sql;
            PreparedStatement statement;

            if (cambiarClave) {
                // Encriptar la nueva contraseña
                String claveEncriptada = Encripter.encrypt(usuario.getClave());

                // Actualizar incluyendo la contraseña
                sql = "UPDATE tblusuarios SET nombre = ?, apellido = ?, nombreusuario = ?, " +
                        "clave = ?, idacceso = ? WHERE idusuario = ?";
                statement = connection.prepareStatement(sql);
                statement.setString(1, usuario.getNombre());
                statement.setString(2, usuario.getApellido());
                statement.setString(3, usuario.getNombreUsuario());
                statement.setString(4, claveEncriptada);
                statement.setInt(5, usuario.getIdAcceso());
                statement.setInt(6, usuario.getIdUsuario());
            } else {
                // Actualizar sin cambiar la contraseña
                sql = "UPDATE tblusuarios SET nombre = ?, apellido = ?, nombreusuario = ?, " +
                        "idacceso = ? WHERE idusuario = ?";
                statement = connection.prepareStatement(sql);
                statement.setString(1, usuario.getNombre());
                statement.setString(2, usuario.getApellido());
                statement.setString(3, usuario.getNombreUsuario());
                statement.setInt(4, usuario.getIdAcceso());
                statement.setInt(5, usuario.getIdUsuario());
            }

            int filasAfectadas = statement.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar usuario: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    // Método para verificar si un correo ya existe excluyendo un usuario específico
    public static boolean correoExisteExcluyendoUsuario(String correo, int idUsuarioExcluir) {
        try {
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) as total FROM tblcorreos c " +
                            "INNER JOIN tblusuarios u ON c.idcorreo = u.idcorreo " +
                            "WHERE c.correo = ? AND u.idusuario <> ?"
            );
            statement.setString(1, correo);
            statement.setInt(2, idUsuarioExcluir);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
            return false;
        } catch (SQLException e) {
            System.out.println("Error al verificar correo: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Método para verificar si un nombre de usuario ya existe excluyendo un usuario específico
    public static boolean nombreUsuarioExisteExcluyendoUsuario(String nombreUsuario, int idUsuarioExcluir) {
        try {
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) as total FROM tblusuarios WHERE nombreusuario = ? AND idusuario <> ?"
            );
            statement.setString(1, nombreUsuario);
            statement.setInt(2, idUsuarioExcluir);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
            return false;
        } catch (SQLException e) {
            System.out.println("Error al verificar nombre de usuario: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}