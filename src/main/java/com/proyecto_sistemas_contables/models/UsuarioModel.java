package com.proyecto_sistemas_contables.models;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;

import java.sql.*;

public class UsuarioModel {
    private int idUsuario;
    private String nombreUsuario;
    private String nombre;
    private String apellido;
    private String clave;
    private int idCorreo;
    private int idAcceso;

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

    //Metodo para crear usuario
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
            System.out.println(statement.executeUpdate());
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

}