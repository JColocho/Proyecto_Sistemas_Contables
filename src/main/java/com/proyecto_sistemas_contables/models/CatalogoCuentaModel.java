package com.proyecto_sistemas_contables.models;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class CatalogoCuentaModel {
    private int idCuenta;
    private String cuenta;
    private String codigoCuenta;
    private double saldo;
    private String tipoCuenta;
    private String tipoSaldo;
    private int idEmpresa;
    private double cargo;
    private double abono;

    public CatalogoCuentaModel() {
    }

    public CatalogoCuentaModel(int idCuenta, String cuenta, String codigoCuenta, double saldo, String tipoCuenta, String tipoSaldo, int idEmpresa) {
        this.idCuenta = idCuenta;
        this.cuenta = cuenta;
        this.codigoCuenta = codigoCuenta;
        this.saldo = saldo;
        this.tipoCuenta = tipoCuenta;
        this.tipoSaldo = tipoSaldo;
        this.idEmpresa = idEmpresa;
    }

    public CatalogoCuentaModel(String cuenta, String codigoCuenta, double saldo, String tipoCuenta, String tipoSaldo, int idEmpresa) {
        this.cuenta = cuenta;
        this.codigoCuenta = codigoCuenta;
        this.saldo = saldo;
        this.tipoCuenta = tipoCuenta;
        this.tipoSaldo = tipoSaldo;
        this.idEmpresa = idEmpresa;
    }

    public int getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(int idCuenta) {
        this.idCuenta = idCuenta;
    }

    public String getCuenta() {
        return cuenta;
    }

    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public String getCodigoCuenta() {
        return codigoCuenta;
    }

    public void setCodigoCuenta(String codigoCuenta) {
        this.codigoCuenta = codigoCuenta;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public String getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(String tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public String getTipoSaldo() {
        return tipoSaldo;
    }

    public void setTipoSaldo(String tipoSaldo) {
        this.tipoSaldo = tipoSaldo;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public double getCargo() {
        return cargo;
    }

    public void setCargo(double cargo) {
        this.cargo = cargo;
    }

    public double getAbono() {
        return abono;
    }

    public void setAbono(double abono) {
        this.abono = abono;
    }

    public ObservableList<String> obtenerNombreCuentas(int idEmpresa) {
        try{
            ObservableList<String> cuentas = FXCollections.observableArrayList();

            Connection connection = ConexionDB.connection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM tblcatalogocuentas WHERE idEmpresa = " + idEmpresa);
            while (resultSet.next()) {
                cuentas.add(resultSet.getString("cuenta"));
            }

            return cuentas;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public int obtenerIdCuenta(String nombreCuenta, int idEmpresa) {
        String sql = "SELECT idCuenta FROM tblcatalogocuentas WHERE cuenta = ? AND idempresa = ?";
        try (Connection connection = ConexionDB.connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, nombreCuenta);
            statement.setInt(2, idEmpresa);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("idCuenta");
            }
            return -1; // No se encontró la cuenta

        } catch (SQLException e) {
            System.out.println("Error al obtener ID de cuenta: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return codigoCuenta + "\t" + cuenta;
    }
}
