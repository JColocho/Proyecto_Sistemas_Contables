package com.proyecto_sistemas_contables.models;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class EmpresaModel {
    private  SimpleIntegerProperty id;
    private  SimpleStringProperty nombre;
    private  SimpleStringProperty nit;
    private  SimpleStringProperty nrc;
    private  SimpleStringProperty direccion;
    private  SimpleStringProperty telefono;
    private  SimpleIntegerProperty idCorreo;
    private  SimpleStringProperty correo;

    public EmpresaModel(int id, String nombre, String nit, String nrc, String direccion,
                        String telefono, int idCorreo, String correo) {
        this.id = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.nit = new SimpleStringProperty(nit);
        this.nrc = new SimpleStringProperty(nrc);
        this.direccion = new SimpleStringProperty(direccion);
        this.telefono = new SimpleStringProperty(telefono);
        this.idCorreo = new SimpleIntegerProperty(idCorreo);
        this.correo = new SimpleStringProperty(correo);
    }

    public EmpresaModel() {

    }

    // Getters
    public int getId() { return id.get(); }
    public String getNombre() { return nombre.get(); }
    public String getNit() { return nit.get(); }
    public String getNrc() { return nrc.get(); }
    public String getDireccion() { return direccion.get(); }
    public String getTelefono() { return telefono.get(); }
    public int getIdCorreo() { return idCorreo.get(); }
    public String getCorreo() { return correo.get(); }

    // Property getters (necesarios para TableView)
    public SimpleIntegerProperty idProperty() { return id; }
    public SimpleStringProperty nombreProperty() { return nombre; }
    public SimpleStringProperty nitProperty() { return nit; }
    public SimpleStringProperty nrcProperty() { return nrc; }
    public SimpleStringProperty direccionProperty() { return direccion; }
    public SimpleStringProperty telefonoProperty() { return telefono; }
    public SimpleStringProperty correoProperty() { return correo; }

    // ========================
    // MÉTODOS DE NEGOCIO
    // ========================

    /**
     * Cargar todas las empresas desde la base de datos
     */
    public static ObservableList<EmpresaModel> cargarEmpresas() throws SQLException {
        ObservableList<EmpresaModel> listaEmpresas = FXCollections.observableArrayList();

        try (Connection conn = ConexionDB.connection()) {
            String sql = """
                    SELECT e.idempresa, e.nombre, e.nit, e.nrc, e.direccion, 
                           e.telefono, e.idcorreo, c.correo
                    FROM tblempresas e
                    LEFT JOIN tblcorreos c ON e.idcorreo = c.idcorreo
                    ORDER BY e.idempresa
                    """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                EmpresaModel empresa = new EmpresaModel(
                        rs.getInt("idempresa"),
                        rs.getString("nombre"),
                        rs.getString("nit"),
                        rs.getString("nrc"),
                        rs.getString("direccion"),
                        rs.getString("telefono"),
                        rs.getInt("idcorreo"),
                        rs.getString("correo")
                );
                listaEmpresas.add(empresa);
            }
        }

        return listaEmpresas;
    }

    /**
     * Guardar una nueva empresa en la base de datos
     */
    public static void guardarNuevaEmpresa(String nombre, String nit, String nrc,
                                           String direccion, String telefono,
                                           String correo) throws SQLException {
        try (Connection conn = ConexionDB.connection()) {
            // Insertar correo y obtener id
            String sqlCorreo = "INSERT INTO tblcorreos (correo) VALUES (?) RETURNING idcorreo";
            PreparedStatement psCorreo = conn.prepareStatement(sqlCorreo);
            psCorreo.setString(1, correo);
            ResultSet rsCorreo = psCorreo.executeQuery();

            int idCorreo = 0;
            if (rsCorreo.next()) {
                idCorreo = rsCorreo.getInt("idcorreo");
            }

            // Insertar empresa
            String sqlEmpresa = """
                    INSERT INTO tblempresas (nombre, nit, nrc, direccion, telefono, idcorreo)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
            PreparedStatement psEmpresa = conn.prepareStatement(sqlEmpresa);
            psEmpresa.setString(1, nombre);
            psEmpresa.setString(2, nit);
            psEmpresa.setString(3, nrc);
            psEmpresa.setString(4, direccion);
            psEmpresa.setString(5, telefono);
            psEmpresa.setInt(6, idCorreo);
            psEmpresa.executeUpdate();
        }
    }

    /**
     * Actualizar una empresa existente
     */
    public void actualizarEmpresa(String nombre, String nit, String nrc,
                                  String direccion, String telefono,
                                  String correo) throws SQLException {
        try (Connection conn = ConexionDB.connection()) {
            // Actualizar correo
            String sqlCorreo = "UPDATE tblcorreos SET correo = ? WHERE idcorreo = ?";
            PreparedStatement psCorreo = conn.prepareStatement(sqlCorreo);
            psCorreo.setString(1, correo);
            psCorreo.setInt(2, this.getIdCorreo());
            psCorreo.executeUpdate();

            // Actualizar empresa
            String sqlEmpresa = """
                    UPDATE tblempresas 
                    SET nombre = ?, nit = ?, nrc = ?, direccion = ?, telefono = ?
                    WHERE idempresa = ?
                    """;
            PreparedStatement psEmpresa = conn.prepareStatement(sqlEmpresa);
            psEmpresa.setString(1, nombre);
            psEmpresa.setString(2, nit);
            psEmpresa.setString(3, nrc);
            psEmpresa.setString(4, direccion);
            psEmpresa.setString(5, telefono);
            psEmpresa.setInt(6, this.getId());
            psEmpresa.executeUpdate();
        }
    }

    /**
     * Eliminar una empresa de la base de datos
     */
    public void eliminarEmpresa() throws SQLException {
        try (Connection conn = ConexionDB.connection()) {
            // Eliminar empresa
            String sqlEmpresa = "DELETE FROM tblempresas WHERE idempresa = ?";
            PreparedStatement psEmpresa = conn.prepareStatement(sqlEmpresa);
            psEmpresa.setInt(1, this.getId());
            psEmpresa.executeUpdate();

            // Eliminar correo asociado
            String sqlCorreo = "DELETE FROM tblcorreos WHERE idcorreo = ?";
            PreparedStatement psCorreo = conn.prepareStatement(sqlCorreo);
            psCorreo.setInt(1, this.getIdCorreo());
            psCorreo.executeUpdate();
        }
    }
    public String idBuscarEmpresa(int id) throws SQLException {
        try (Connection conn = ConexionDB.connection()) {
            // Busca la empresa
            String sqlEmpresa = "SELECT * FROM tblempresas WHERE idempresa = ?";
            PreparedStatement psEmpresa = conn.prepareStatement(sqlEmpresa);
            psEmpresa.setInt(1, id);
            ResultSet resultSet = psEmpresa.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("nombre");
            }

            return "";
        }
    }
}