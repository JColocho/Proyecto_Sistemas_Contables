package com.proyecto_sistemas_contables.Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String url = "jdbc:postgresql://localhost:5432/db_sistema_contable";
    private static final String USER = "postgres";
    private static final String PASS = "2004";

    public static Connection connection(){
        try {
            Connection conectar = DriverManager.getConnection(url,USER,PASS);
            return conectar;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}