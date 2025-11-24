package com.proyecto_sistemas_contables.models;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DetallePartidaModel {
    private int idDetalle;
    private int idPartida;
    private int idCuenta;
    private String Cuenta;
    private Double cargo;
    private Double abono;

    public DetallePartidaModel() {
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(int idPartida) {
        this.idPartida = idPartida;
    }

    public int getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(int idCuenta) {
        this.idCuenta = idCuenta;
    }

    public Double getCargo() {
        return cargo;
    }

    public void setCargo(Double cargo) {
        this.cargo = cargo;
    }

    public Double getAbono() {
        return abono;
    }

    public void setAbono(Double abono) {
        this.abono = abono;
    }

    public String getCuenta() {
        return Cuenta;
    }

    public void setCuenta(String cuenta) {
        Cuenta = cuenta;
    }

    public void agregarDetalle(int idPartida, int idCuenta, Double cargo, Double abono) {
        try{
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO tbldetallepartida(idpartida,idcuenta,cargo,abono) VALUES(?,?,?,?)");
            statement.setInt(1, idPartida);
            statement.setInt(2, idCuenta);
            statement.setDouble(3, cargo);
            statement.setDouble(4, abono);
            statement.executeUpdate();

        } catch (SQLException e){
            System.out.println(e.getMessage());
        }

    }
    public static ObservableList<DetallePartidaModel> obtenerDetallePartida(int idPartida) {
        ObservableList<DetallePartidaModel> lista = FXCollections.observableArrayList();
        try{
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("SELECT " +
                    "dp.iddetalle, dp.idpartida, dp.idcuenta, c.cuenta, dp.cargo, dp.abono " +
                    "FROM tbldetallepartida dp INNER JOIN tblcatalogocuentas c ON dp.idcuenta = c.idcuenta WHERE dp.idpartida = ?");
            statement.setInt(1, idPartida);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                DetallePartidaModel detallePartidaModel = new DetallePartidaModel();
                detallePartidaModel.setIdDetalle(resultSet.getInt("iddetalle"));
                detallePartidaModel.setIdPartida(resultSet.getInt("idpartida"));
                detallePartidaModel.setIdCuenta(resultSet.getInt("idcuenta"));
                detallePartidaModel.setCuenta(resultSet.getString("cuenta"));
                detallePartidaModel.setCargo(resultSet.getDouble("cargo"));
                detallePartidaModel.setAbono(resultSet.getDouble("abono"));
                lista.add(detallePartidaModel);
            }
            return lista;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static ObservableList<DetallePartidaModel> obtenerDetallePorCuenta(int idCuenta, int idEmpresaSesion) {
        ObservableList<DetallePartidaModel> lista = FXCollections.observableArrayList();
        try{
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("SELECT " +
                    "dp.iddetalle, dp.idpartida, dp.idcuenta, c.cuenta, dp.cargo, dp.abono " +
                    "FROM tbldetallepartida dp INNER JOIN tblcatalogocuentas c ON dp.idcuenta = c.idcuenta WHERE dp.idcuenta = ? AND c.idempresa = ?");
            statement.setInt(1, idCuenta);
            statement.setInt(2, idEmpresaSesion);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                DetallePartidaModel detallePartidaModel = new DetallePartidaModel();
                detallePartidaModel.setIdDetalle(resultSet.getInt("iddetalle"));
                detallePartidaModel.setIdPartida(resultSet.getInt("idpartida"));
                detallePartidaModel.setIdCuenta(resultSet.getInt("idcuenta"));
                detallePartidaModel.setCuenta(resultSet.getString("cuenta"));
                detallePartidaModel.setCargo(resultSet.getDouble("cargo"));
                detallePartidaModel.setAbono(resultSet.getDouble("abono"));
                lista.add(detallePartidaModel);
            }
            return lista;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
