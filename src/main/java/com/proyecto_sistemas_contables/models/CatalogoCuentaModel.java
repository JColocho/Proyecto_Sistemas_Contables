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

    //Metodo para obtener el lista de nombre del catalogo de cuentas
    public ObservableList<String> obtenerNombreCuentas(int idEmpresa) {
        try{
            //Colección con todas la cuentas
            ObservableList<String> cuentas = FXCollections.observableArrayList();

            //Conexión con la base de datos
            Connection connection = ConexionDB.connection();
            Statement statement = connection.createStatement();

            //Consulta en la base de datos
            ResultSet resultSet = statement.executeQuery("SELECT * FROM tblcatalogocuentas WHERE idEmpresa = " + idEmpresa);

            //Obtener los datos encontrados en la base de datos
            while (resultSet.next()) {
                cuentas.add(resultSet.getString("cuenta"));
            }

            //Retornar la colección
            return cuentas;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Metodo para obtener el id de la cuenta mendiante el nombre
    public int obtenerIdCuenta(String nombreCuenta, int idEmpresa) {
        //Consulta para la base de datos para encontrar el id de la cuenta
        String sql = "SELECT idCuenta FROM tblcatalogocuentas WHERE cuenta = ? AND idempresa = ?";

        //Conexión con la base de datos
        try (Connection connection = ConexionDB.connection();
             //Preparar la consulta para la base de datos
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, nombreCuenta);
            statement.setInt(2, idEmpresa);

            //Ejecutar la consulta
            ResultSet resultSet = statement.executeQuery();
            //Obtener el resultado de la consulta
            if (resultSet.next()) {
                //retornar el resultado
                return resultSet.getInt("idCuenta");
            }
            return -1;

        } catch (SQLException e) {
            System.out.println("Error al obtener ID de cuenta: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    //Metodo para obtener todo el catalogo de cuentas de la empresa en especifico
    public ObservableList<CatalogoCuentaModel> obtenerCatalogoCuentas(int idEmpresa) {
        try{
            ObservableList<CatalogoCuentaModel> cuentas = FXCollections.observableArrayList();

            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tblcatalogocuentas WHERE idEmpresa = " + idEmpresa);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                CatalogoCuentaModel cuentaModel = new CatalogoCuentaModel();
                cuentaModel.setIdCuenta(resultSet.getInt("idCuenta"));
                cuentaModel.setIdEmpresa(resultSet.getInt("idEmpresa"));
                cuentaModel.setCuenta(resultSet.getString("cuenta"));
                cuentaModel.setCodigoCuenta(resultSet.getString("codigo"));
                cuentaModel.setTipoCuenta(resultSet.getString("tipoCuenta"));
                cuentaModel.setTipoSaldo(resultSet.getString("tipoSaldo"));
                cuentaModel.setSaldo(resultSet.getDouble("saldo"));
                cuentas.add(cuentaModel);
            }

            return cuentas;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    //Metodo para obtener todo el catalogo de cuentas según el tipo de la cuenta
    public ObservableList<CatalogoCuentaModel> obtenerCatalogoCuentasPorTipo(String tipoCuenta, int idEmpresa) {
        try{
            ObservableList<CatalogoCuentaModel> cuentas = FXCollections.observableArrayList();

            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tblcatalogocuentas WHERE idEmpresa = ? AND tipoCuenta ILIKE '%" + tipoCuenta + "%'");
            statement.setInt(1, idEmpresa);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                CatalogoCuentaModel cuentaModel = new CatalogoCuentaModel();
                cuentaModel.setIdCuenta(resultSet.getInt("idCuenta"));
                cuentaModel.setIdEmpresa(resultSet.getInt("idEmpresa"));
                cuentaModel.setCuenta(resultSet.getString("cuenta"));
                cuentaModel.setCodigoCuenta(resultSet.getString("codigo"));
                cuentaModel.setTipoCuenta(resultSet.getString("tipoCuenta"));
                cuentaModel.setTipoSaldo(resultSet.getString("tipoSaldo"));
                cuentaModel.setSaldo(resultSet.getDouble("saldo"));
                cuentas.add(cuentaModel);
            }

            return cuentas;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    //Metodo para eliminar una cuenta del catalogo
    public void eliminarCuenta(int idCuenta, int idEmpresa) {
        try{
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM tblcatalogocuentas WHERE idCuenta = ? AND idempresa = ?");
            statement.setInt(1, idCuenta);
            statement.setInt(2, idEmpresa);
            statement.executeUpdate();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Metodo para validar si el cuenta ya existe
    public boolean cuentaExiste(String cuenta, int idEmpresa) {
        try{
            Connection connection = ConexionDB.connection();

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tblcatalogocuentas WHERE Cuenta = ? AND idempresa = ?");
            statement.setString(1, cuenta);
            statement.setInt(2, idEmpresa);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Metodo para validar si el codigo de la cuenta ya existe
    public boolean codigoExiste(String codigoCuenta, int idEmpresa) {
        try{
            Connection connection = ConexionDB.connection();

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tblcatalogocuentas WHERE codigo = ? AND idempresa = ?");
            statement.setString(1, codigoCuenta);
            statement.setInt(2, idEmpresa);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Metodo para insertar una cuenta al catalogo en la base de datos
    public void crearCuenta(CatalogoCuentaModel cuentaModel) {
        try{
            Connection connection = ConexionDB.connection();

            PreparedStatement statement = connection.prepareStatement("INSERT INTO tblcatalogocuentas (cuenta, codigo, tipocuenta, idempresa) VALUES (?, ?, ?, ?)");
            statement.setString(1, cuentaModel.getCuenta());
            statement.setString(2, cuentaModel.getCodigoCuenta());
            statement.setString(3, cuentaModel.getTipoCuenta());
            statement.setInt(4, cuentaModel.getIdEmpresa());
            statement.executeUpdate();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Metodo para actualizar una cuenta del cátalogo
    public void editarCuenta(int idCuenta, CatalogoCuentaModel cuentaModel) {
        try{
            Connection connection = ConexionDB.connection();

            PreparedStatement statement = connection.prepareStatement("UPDATE tblcatalogocuentas SET " +
                    "cuenta = ?, codigo = ?, tipocuenta = ? WHERE idCuenta = ?");

            statement.setString(1, cuentaModel.getCuenta());
            statement.setString(2, cuentaModel.getCodigoCuenta());
            statement.setString(3, cuentaModel.getTipoCuenta());
            statement.setInt(4, idCuenta);

            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Metodo para buscar cuenta según similutud del nombre de cuenta
    public ObservableList<CatalogoCuentaModel> obtenerCatalogoCuentasSimilitud(String nombreCuenta, int idEmpresa) {
        try{
            ObservableList<CatalogoCuentaModel> cuentas = FXCollections.observableArrayList();
            Connection connection = ConexionDB.connection();

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tblcatalogocuentas " +
                    "WHERE idEmpresa = '" + idEmpresa + "' AND cuenta ILIKE '%" + nombreCuenta + "%'");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                CatalogoCuentaModel cuentaModel = new CatalogoCuentaModel();
                cuentaModel.setIdCuenta(resultSet.getInt("idCuenta"));
                cuentaModel.setIdEmpresa(resultSet.getInt("idEmpresa"));
                cuentaModel.setCuenta(resultSet.getString("cuenta"));
                cuentaModel.setCodigoCuenta(resultSet.getString("codigo"));
                cuentaModel.setTipoCuenta(resultSet.getString("tipoCuenta"));
                cuentaModel.setTipoSaldo(resultSet.getString("tipoSaldo"));
                cuentaModel.setSaldo(resultSet.getDouble("saldo"));

                cuentas.add(cuentaModel);
            }

            return cuentas;

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Metodo para calcular el saldo de las cuentas
    public void actualizarSaldosCuentas(int idEmpresa) {
        try {
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("UPDATE tblcatalogocuentas AS c\n" +
                    "SET \n" +
                    "    saldo = ABS(COALESCE(m.saldo, 0)),   -- saldo POSITIVO\n" +
                    "    tiposaldo = CASE\n" +
                    "        WHEN m.saldo > 0 THEN 'DEUDOR'\n" +
                    "        WHEN m.saldo < 0 THEN 'ACREEDOR'\n" +
                    "        ELSE ''\n" +
                    "    END\n" +
                    "FROM (\n" +
                    "    SELECT \n" +
                    "        c2.idcuenta,\n" +
                    "        -- Saldo contable real (puede ser negativo)\n" +
                    "        COALESCE(SUM(dp.cargo), 0) - COALESCE(SUM(dp.abono), 0) AS saldo\n" +
                    "    FROM tblcatalogocuentas c2\n" +
                    "    LEFT JOIN tbldetallepartida dp ON c2.idcuenta = dp.idcuenta\n" +
                    "    LEFT JOIN tblpartidas p ON dp.idpartida = p.idpartida\n" +
                    "        AND p.idempresa = c2.idempresa\n" +
                    "    WHERE c2.idempresa = ?\n" +
                    "    GROUP BY c2.idcuenta\n" +
                    ") AS m\n" +
                    "WHERE c.idcuenta = m.idcuenta\n" +
                    "  AND c.idempresa = ?;");
            statement.setInt(1, idEmpresa);
            statement.setInt(2, idEmpresa);
            statement.executeUpdate();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Metodo para saber si la cuenta ya estat en uso
    public boolean cuentaEnUso(int idcuenta) {
        try{
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tbldetallepartida WHERE idCuenta = ?");
            statement.setInt(1, idcuenta);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
            return false;

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return codigoCuenta + "\t" + cuenta;
    }
}
