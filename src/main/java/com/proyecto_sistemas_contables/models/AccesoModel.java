package com.proyecto_sistemas_contables.models;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;

import java.sql.*;

public class AccesoModel {
    private int idAcceso;
    private String nivelAcceso;
    private String claveAcceso;

    public int getIdAcceso() {
        return idAcceso;
    }

    public void setIdAcceso(int idAcceso) {
        this.idAcceso = idAcceso;
    }

    public String getNivelAcceso() {
        return nivelAcceso;
    }

    public void setNivelAcceso(String nivelAcceso) {
        this.nivelAcceso = nivelAcceso;
    }

    public String getClaveAcceso() {
        return claveAcceso;
    }

    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    public boolean darAcceso(String nivelAcceso, String claveAcceso) {
        try{
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tblAccesos WHERE nivelAcceso = ? AND claveAcceso = ?");
            statement.setString(1, nivelAcceso);
            statement.setString(2, claveAcceso);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return true;
            }
            else {
                return false;
            }

        } catch (SQLException e){
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return this.nivelAcceso;
    }
}
