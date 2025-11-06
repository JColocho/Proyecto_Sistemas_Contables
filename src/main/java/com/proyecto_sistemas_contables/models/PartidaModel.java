package com.proyecto_sistemas_contables.models;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.sql.*;

public class PartidaModel {
    private int idPartida;
    private Date fecha;
    private int asiento;
    private String concepto;
    private String tipoDocumento;
    private String numeroDocumento;
    private int idUsuario;
    private int idEmpresa;
    private String nombreUsuario;

    public PartidaModel() {
    }

    public PartidaModel(int idpartida, Date fecha, String concepto, String nombreusuario) {
        this.idPartida = idpartida;
        this.fecha = fecha;
        this.concepto = concepto;
        this.nombreUsuario = nombreusuario;
    }

    public int getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(int idPartida) {
        this.idPartida = idPartida;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getAsiento() {
        return asiento;
    }

    public void setAsiento(int asiento) {
        this.asiento = asiento;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public int agregarPartida(PartidaModel partida) {
        try{
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO tblpartidas " +
                    "(fecha, concepto, tipodocumento, numerodocumento, idusuario, idempresa) " +
                    "VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            statement.setDate(1, partida.getFecha());
            statement.setString(2, partida.getConcepto());
            statement.setString(3, partida.getTipoDocumento());
            statement.setString(4, partida.getNumeroDocumento());
            statement.setInt(5, partida.getIdUsuario());
            statement.setInt(6, partida.getIdEmpresa());

            int filasAfectadas = statement.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = statement.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }

            return -1;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static ObservableList<PartidaModel> obtenerPartidas(Date fechaInicio, Date fechaFin) {
        ObservableList<PartidaModel> lista = FXCollections.observableArrayList();
        String sql;

        boolean tieneRango = (fechaInicio != null && fechaFin != null);

        if (tieneRango) {
            sql = """
                SELECT p.idpartida, p.fecha, p.concepto, u.nombreusuario
                FROM tblpartidas p
                INNER JOIN tblusuarios u ON p.idusuario = u.idusuario
                WHERE p.fecha BETWEEN ? AND ?
                ORDER BY p.fecha DESC
            """;
        } else {
            sql = """
                SELECT p.idpartida, p.fecha, p.concepto, u.nombreusuario
                FROM tblpartidas p
                INNER JOIN tblusuarios u ON p.idusuario = u.idusuario
                ORDER BY p.fecha DESC
                LIMIT 10
            """;
        }

        try (Connection connection = ConexionDB.connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (tieneRango) {
                statement.setDate(1, new java.sql.Date(fechaInicio.getTime()));
                statement.setDate(2, new java.sql.Date(fechaFin.getTime()));
            }

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                lista.add(new PartidaModel(
                        rs.getInt("idpartida"),
                        rs.getDate("fecha"),
                        rs.getString("concepto"),
                        rs.getString("nombreusuario")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public PartidaModel obtenerPartida(int idPartida) {

        try {
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tblpartidas WHERE idpartida = ?");
            statement.setInt(1, idPartida);
            ResultSet rs = statement.executeQuery();
            PartidaModel partida = new PartidaModel();
            while (rs.next()) {

                partida.setIdPartida(idPartida);
                partida.setFecha(rs.getDate("fecha"));
                partida.setAsiento(rs.getInt("asiento"));
                partida.setConcepto(rs.getString("concepto"));
                partida.setTipoDocumento(rs.getString("tipodocumento"));
                partida.setNumeroDocumento(rs.getString("numerodocumento"));
                partida.setIdUsuario(rs.getInt("idusuario"));
                partida.setIdEmpresa(rs.getInt("idempresa"));
            }

            return partida;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public boolean numeroDocExistente(String numeroDoc, int idEmpresa) {
        try {
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tblpartidas WHERE numerodocumento = ? AND idempresa = ?");
            statement.setString(1, numeroDoc);
            statement.setInt(2, idEmpresa);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return true;
            }

            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void ActualizarPartida(PartidaModel partida) {
        try{
            Connection connection = ConexionDB.connection();
            PreparedStatement statementPartida = connection.prepareStatement("UPDATE tblpartidas SET " +
                    "fecha = ?, concepto = ?, tipodocumento = ?, numerodocumento = ?, idusuario = ? WHERE idpartida = ?");
            statementPartida.setDate(1, partida.getFecha());
            statementPartida.setString(2, partida.getConcepto());
            statementPartida.setString(3, partida.getTipoDocumento());
            statementPartida.setString(4, partida.getNumeroDocumento());
            statementPartida.setInt(5, partida.getIdUsuario());
            statementPartida.setInt(6, partida.getIdPartida());
            statementPartida.executeUpdate();

            PreparedStatement statementDetalle = connection.prepareStatement("DELETE FROM tbldetallepartida WHERE idpartida = ?");
            statementDetalle.setInt(1, partida.getIdPartida());
            statementDetalle.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void EliminarPartida(int idPartida, int idEmpresa) {
        try{
            Connection connection = ConexionDB.connection();
            PreparedStatement statementFile = connection.prepareStatement("SELECT * FROM tblpartidas WHERE idpartida = ? AND idempresa = ?");
            statementFile.setInt(1, idPartida);
            statementFile.setInt(2, idEmpresa);
            ResultSet rs = statementFile.executeQuery();

            if (rs.next()) {
                EmpresaModel empresaModel = new EmpresaModel();
                String documentoRuta = "src/main/resources/com/proyecto_sistemas_contables/documentos_partidas/" + empresaModel.idBuscarEmpresa(idEmpresa) +"/" + rs.getString("numerodocumento") + ".pdf";
                File actualFile = new File(documentoRuta);
                actualFile.delete();
            }


            PreparedStatement statementDetalle = connection.prepareStatement("DELETE FROM tbldetallepartida WHERE idpartida = ?");
            statementDetalle.setInt(1, idPartida);
            statementDetalle.executeUpdate();

            PreparedStatement statementPartida = connection.prepareStatement("DELETE FROM tblpartidas WHERE idpartida = ?");
            statementPartida.setInt(1, idPartida);
            statementPartida.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
