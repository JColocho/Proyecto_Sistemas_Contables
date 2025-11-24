package com.proyecto_sistemas_contables.models;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class AccesoModel {
    private int idAcceso;
    private String nivelAcceso;
    private String claveAcceso;

    // ========== CONSTANTES DE ROLES ==========
    public static final int ROL_ADMINISTRADOR = 1;
    public static final int ROL_CONTADOR = 2;
    public static final int ROL_AUDITOR = 3;

    // Constructores
    public AccesoModel() {
    }

    // Getters y Setters
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

    // Para mostrar en ComboBox
    @Override
    public String toString() {
        return nivelAcceso;
    }

    //Obtiene la lista de todos los niveles de acceso para mostrar en ComboBox
    public ObservableList<AccesoModel> listaAccesos() {
        ObservableList<AccesoModel> lista = FXCollections.observableArrayList();

        try {
            Connection connection = ConexionDB.connection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM tblaccesos ORDER BY idacceso");

            while (rs.next()) {
                AccesoModel acceso = new AccesoModel();
                acceso.setIdAcceso(rs.getInt("idacceso"));
                acceso.setNivelAcceso(rs.getString("nivelacceso"));
                acceso.setClaveAcceso(rs.getString("claveacceso"));
                lista.add(acceso);
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error al obtener lista de accesos: " + e.getMessage());
        }

        return lista;
    }

    //MÉTODOS NUEVOS PARA SISTEMA DE PERMISOS

    //Obtiene el nivel de acceso (rol) de un usuario
    public static int obtenerNivelAccesoUsuario(int idUsuario) {
        String sql = "SELECT idacceso FROM tblusuarios WHERE idusuario = ?";

        try (Connection conn = ConexionDB.connection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("idacceso");
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener nivel de acceso: " + e.getMessage());
        }

        return -1;
    }

    //MÉTODO DE VALIDACIÓN DE PERMISOS
    public static boolean puedeGestionarAuditorias(int idUsuario) {
        int nivel = obtenerNivelAccesoUsuario(idUsuario);
        return nivel == ROL_ADMINISTRADOR || nivel == ROL_AUDITOR;
    }
}