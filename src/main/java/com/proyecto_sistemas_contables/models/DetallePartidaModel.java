package com.proyecto_sistemas_contables.models;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DetallePartidaModel {
    private int idDetalle;
    private int idPartida;
    private int idCuenta;
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
}
