package com.proyecto_sistemas_contables.models;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;

import java.sql.*;

public class CorreoModel {
    private int idCorreo;
    private String correo;

    public CorreoModel(int idCorreo, String correo) {
        this.idCorreo = idCorreo;
        this.correo = correo;
    }

    public int getIdCorreo() {
        return idCorreo;
    }

    public void setIdCorreo(int idCorreo) {
        this.idCorreo = idCorreo;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void crearCorreo() {
        try{
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO tblcorreos (correo) VALUES (?)");
            statement.setString(1, correo);
            System.out.println(statement.executeUpdate());
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public int buscarIdCorreo(String correo) {
        try{
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tblcorreos WHERE correo = ?");
            statement.setString(1, correo);
            ResultSet result = statement.executeQuery();
            if(result.next()){
                return result.getInt("idCorreo");
            }
            else {
                return -1;
            }

        }
        catch (SQLException e){
            System.out.println(e.getMessage());
            return -1;
        }
    }
}
