package com.proyecto_sistemas_contables.models;
import com.proyecto_sistemas_contables.Conexion.ConexionDB;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
public class ReporteModel {
    private static final int IDEMPRESA = 1;

    public List<Map<String, Object>> obtenerLibroDiario(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = """
            SELECT p.fecha, p.concepto, c.cuenta, d.cargo, d.abono
            FROM tblpartidas p
            JOIN tbldetallepartida d ON p.idpartida = d.idpartida
            LEFT JOIN tblcatalogocuentas c ON d.idcuenta = c.idcuenta
            WHERE p.fecha BETWEEN ? AND ? AND p.idempresa = ?
            ORDER BY p.fecha, p.idpartida, d.idcuenta
        """;

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            ps.setInt(3, IDEMPRESA);

            ResultSet rs = ps.executeQuery();
            return mapearResultados(rs);
        }
    }

    public List<Map<String, Object>> obtenerLibroMayor(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = """
            SELECT c.cuenta, SUM(d.cargo) AS total_cargo, SUM(d.abono) AS total_abono
            FROM tbldetallepartida d
            JOIN tblpartidas p ON d.idpartida = p.idpartida
            JOIN tblcatalogocuentas c ON d.idcuenta = c.idcuenta
            WHERE p.fecha BETWEEN ? AND ? AND p.idempresa = ?
            GROUP BY c.idcuenta, c.cuenta
            ORDER BY c.cuenta
        """;

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            ps.setInt(3, IDEMPRESA);

            ResultSet rs = ps.executeQuery();
            return mapearResultados(rs);
        }
    }

    public void registrarReporteGenerado(int idUsuario, String tipo, LocalDate desde, LocalDate hasta, String ruta, String observaciones) {
        String sql = """
            INSERT INTO tblreportes 
            (idempresa, idusuario, tipo_reporte, fecha_desde, fecha_hasta, ruta_pdf, observaciones, fecha_generacion)
            VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
        """;

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, IDEMPRESA);
            ps.setInt(2, idUsuario);
            ps.setString(3, tipo);
            ps.setDate(4, Date.valueOf(desde));
            ps.setDate(5, Date.valueOf(hasta));
            ps.setString(6, ruta);
            ps.setString(7, observaciones);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private List<Map<String, Object>> mapearResultados(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= cols; i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }
}
