package com.proyecto_sistemas_contables.models;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UsuarioModel {
    private int idUsuario;
    private String nombreUsuario;
    private String nombre;
    private String apellido;
    private String clave;
    private int idCorreo;
    private int idAcceso;

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

    public void crearUsuario(String nombreUsuario, String nombre, String apellido, String clave, int idCorreo, int idAcceso) {
        try {
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO tblusuarios(nombreusuario, nombre, apellido, idcorreo, idaccceso, clave)" +
                    "VALUES(?, ?, ?, ?, ?, ?)");
            statement.setString(1, nombreUsuario);
            statement.setString(2, nombre);
            statement.setString(3, apellido);
            statement.setInt(4, idCorreo);
            statement.setInt(5, idAcceso);
            statement.setString(6, clave);
            System.out.println(statement.executeUpdate());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}