package com.proyecto_sistemas_contables.models;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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

    public boolean darAcceso(int idAcceso, String claveAcceso) {
        try{
            Connection connection = ConexionDB.connection();
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM tblAccesos WHERE idAcceso=" + idAcceso);

            if (result.next()) {
                if (Encripter.verificarClave(claveAcceso, result.getString("claveAcceso"))) {
                    return true;
                }
                else {
                    return false;
                }
            }
            else {
                return false;
            }

        } catch (SQLException e){
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public int buscarIdAcceso(String nivelAcceso) {
        try{
            Connection connection = ConexionDB.connection();
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM tblAccesos WHERE nivelAcceso = " + nivelAcceso);
            if (result.next()) {
                return result.getInt("idAcceso");
            }
            else {
                return -1;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public static ObservableList<AccesoModel> listaAccesos() {
        ObservableList<AccesoModel> accesos = FXCollections.observableArrayList();
        try{
            Connection connection = ConexionDB.connection();
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM tblAccesos");
            while (result.next()) {
                AccesoModel acceso = new AccesoModel();
                acceso.setIdAcceso(result.getInt("idAcceso"));
                acceso.setNivelAcceso(result.getString("nivelAcceso"));
                accesos.add(acceso);
            }

            return accesos;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public String toString() {
        return this.nivelAcceso;
    }
}
