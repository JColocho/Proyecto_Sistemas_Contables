package com.proyecto_sistemas_contables.models;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class AuditoriaPartidaModel {
    private int idAuditoria;
    private int idPartida;
    private int idUsuarioAuditor;
    private Timestamp fechaAuditoria;
    private String estadoAuditoria;
    private String tipoHallazgo;
    private String observacion;

    // Campos adicionales para mostrar en vistas (JOIN)
    private Date fechaPartida;
    private String conceptoPartida;
    private String nombreAuditor;

    // Constructores
    public AuditoriaPartidaModel() {
    }

    public AuditoriaPartidaModel(int idPartida, int idUsuarioAuditor, String estadoAuditoria,
                                 String tipoHallazgo, String observacion) {
        this.idPartida = idPartida;
        this.idUsuarioAuditor = idUsuarioAuditor;
        this.estadoAuditoria = estadoAuditoria;
        this.tipoHallazgo = tipoHallazgo;
        this.observacion = observacion;
    }

    // Getters y Setters
    public int getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(int idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public int getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(int idPartida) {
        this.idPartida = idPartida;
    }

    public int getIdUsuarioAuditor() {
        return idUsuarioAuditor;
    }

    public void setIdUsuarioAuditor(int idUsuarioAuditor) {
        this.idUsuarioAuditor = idUsuarioAuditor;
    }

    public Timestamp getFechaAuditoria() {
        return fechaAuditoria;
    }

    public void setFechaAuditoria(Timestamp fechaAuditoria) {
        this.fechaAuditoria = fechaAuditoria;
    }

    public String getEstadoAuditoria() {
        return estadoAuditoria;
    }

    public void setEstadoAuditoria(String estadoAuditoria) {
        this.estadoAuditoria = estadoAuditoria;
    }

    public String getTipoHallazgo() {
        return tipoHallazgo;
    }

    public void setTipoHallazgo(String tipoHallazgo) {
        this.tipoHallazgo = tipoHallazgo;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Date getFechaPartida() {
        return fechaPartida;
    }

    public void setFechaPartida(Date fechaPartida) {
        this.fechaPartida = fechaPartida;
    }

    public String getConceptoPartida() {
        return conceptoPartida;
    }

    public void setConceptoPartida(String conceptoPartida) {
        this.conceptoPartida = conceptoPartida;
    }

    public String getNombreAuditor() {
        return nombreAuditor;
    }

    public void setNombreAuditor(String nombreAuditor) {
        this.nombreAuditor = nombreAuditor;
    }

    // ========== MÉTODOS DE BASE DE DATOS ==========

    /**
     * Crea una nueva auditoría para una partida
     * @param auditoria Objeto con los datos de la auditoría
     * @return true si se creó exitosamente, false en caso contrario
     */
    public boolean crearAuditoria(AuditoriaPartidaModel auditoria) {
        String sql = "INSERT INTO tblauditoriapartida (idpartida, idusuarioauditor, estadoauditoria, tipohallazgo, observacion) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.connection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, auditoria.getIdPartida());
            stmt.setInt(2, auditoria.getIdUsuarioAuditor());
            stmt.setString(3, auditoria.getEstadoAuditoria());
            stmt.setString(4, auditoria.getTipoHallazgo());
            stmt.setString(5, auditoria.getObservacion());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al crear auditoría: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza una auditoría existente
     * @param auditoria Objeto con los datos actualizados
     * @return true si se actualizó exitosamente, false en caso contrario
     */
    public boolean actualizarAuditoria(AuditoriaPartidaModel auditoria) {
        String sql = "UPDATE tblauditoriapartida SET estadoauditoria = ?, tipohallazgo = ?, observacion = ?, " +
                "fechaauditoria = CURRENT_TIMESTAMP WHERE idauditoria = ?";

        try (Connection conn = ConexionDB.connection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, auditoria.getEstadoAuditoria());
            stmt.setString(2, auditoria.getTipoHallazgo());
            stmt.setString(3, auditoria.getObservacion());
            stmt.setInt(4, auditoria.getIdAuditoria());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar auditoría: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la auditoría de una partida específica
     * @param idPartida ID de la partida
     * @return Objeto AuditoriaPartidaModel o null si no existe
     */
    public static AuditoriaPartidaModel obtenerAuditoriaPorPartida(int idPartida) {
        String sql = "SELECT a.*, u.nombreusuario, p.fecha, p.concepto " +
                "FROM tblauditoriapartida a " +
                "INNER JOIN tblusuarios u ON a.idusuarioauditor = u.idusuario " +
                "INNER JOIN tblpartidas p ON a.idpartida = p.idpartida " +
                "WHERE a.idpartida = ?";

        try (Connection conn = ConexionDB.connection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPartida);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                AuditoriaPartidaModel auditoria = new AuditoriaPartidaModel();
                auditoria.setIdAuditoria(rs.getInt("idauditoria"));
                auditoria.setIdPartida(rs.getInt("idpartida"));
                auditoria.setIdUsuarioAuditor(rs.getInt("idusuarioauditor"));
                auditoria.setFechaAuditoria(rs.getTimestamp("fechaauditoria"));
                auditoria.setEstadoAuditoria(rs.getString("estadoauditoria"));
                auditoria.setTipoHallazgo(rs.getString("tipohallazgo"));
                auditoria.setObservacion(rs.getString("observacion"));
                auditoria.setNombreAuditor(rs.getString("nombreusuario"));
                auditoria.setFechaPartida(rs.getDate("fecha"));
                auditoria.setConceptoPartida(rs.getString("concepto"));
                return auditoria;
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener auditoría por partida: " + e.getMessage());
        }

        return null;
    }

    /**
     * Obtiene todas las partidas con su estado de auditoría
     * @param fechaInicio Fecha inicial del rango (puede ser null)
     * @param fechaFin Fecha final del rango (puede ser null)
     * @param idEmpresa ID de la empresa
     * @return Lista observable de partidas con información de auditoría
     */
    public static ObservableList<PartidaAuditoriaView> obtenerPartidasConAuditoria(Date fechaInicio, Date fechaFin, int idEmpresa) {
        ObservableList<PartidaAuditoriaView> lista = FXCollections.observableArrayList();
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT p.idpartida, p.fecha, p.concepto, u.nombreusuario as contador, ");
        sql.append("COALESCE(a.estadoauditoria, 'Sin auditar') as estadoauditoria, ");
        sql.append("a.tipohallazgo, a.fechaauditoria, u2.nombreusuario as auditor ");
        sql.append("FROM tblpartidas p ");
        sql.append("INNER JOIN tblusuarios u ON p.idusuario = u.idusuario ");
        sql.append("LEFT JOIN tblauditoriapartida a ON p.idpartida = a.idpartida ");
        sql.append("LEFT JOIN tblusuarios u2 ON a.idusuarioauditor = u2.idusuario ");
        sql.append("WHERE p.idempresa = ? ");

        if (fechaInicio != null && fechaFin != null) {
            sql.append("AND p.fecha BETWEEN ? AND ? ");
        }

        sql.append("ORDER BY p.fecha DESC");

        try (Connection conn = ConexionDB.connection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setInt(1, idEmpresa);

            if (fechaInicio != null && fechaFin != null) {
                stmt.setDate(2, new java.sql.Date(fechaInicio.getTime()));
                stmt.setDate(3, new java.sql.Date(fechaFin.getTime()));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PartidaAuditoriaView item = new PartidaAuditoriaView();
                item.setIdPartida(rs.getInt("idpartida"));
                item.setFecha(rs.getDate("fecha"));
                item.setConcepto(rs.getString("concepto"));
                item.setContador(rs.getString("contador"));
                item.setEstadoAuditoria(rs.getString("estadoauditoria"));
                item.setTipoHallazgo(rs.getString("tipohallazgo"));
                item.setFechaAuditoria(rs.getTimestamp("fechaauditoria"));
                item.setAuditor(rs.getString("auditor"));
                lista.add(item);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener partidas con auditoría: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Verifica si una partida ya ha sido auditada
     * @param idPartida ID de la partida
     * @return true si ya existe una auditoría, false en caso contrario
     */
    public static boolean partidaYaAuditada(int idPartida) {
        String sql = "SELECT COUNT(*) FROM tblauditoriapartida WHERE idpartida = ?";

        try (Connection conn = ConexionDB.connection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPartida);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar si partida fue auditada: " + e.getMessage());
        }

        return false;
    }

    /**
     * Obtiene estadísticas de auditoría para un período
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @param idEmpresa ID de la empresa
     * @return Array con [total, pendientes, aprobadas, con observaciones, rechazadas]
     */
    public static int[] obtenerEstadisticasAuditoria(Date fechaInicio, Date fechaFin, int idEmpresa) {
        int[] stats = new int[5]; // [total, pendientes, aprobadas, con observaciones, rechazadas]

        String sql = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN a.estadoauditoria IS NULL THEN 1 ELSE 0 END) as pendientes, " +
                "SUM(CASE WHEN a.estadoauditoria = 'Aprobada' THEN 1 ELSE 0 END) as aprobadas, " +
                "SUM(CASE WHEN a.estadoauditoria = 'Con observaciones' THEN 1 ELSE 0 END) as conobservaciones, " +
                "SUM(CASE WHEN a.estadoauditoria = 'Rechazada' THEN 1 ELSE 0 END) as rechazadas " +
                "FROM tblpartidas p " +
                "LEFT JOIN tblauditoriapartida a ON p.idpartida = a.idpartida " +
                "WHERE p.idempresa = ? AND p.fecha BETWEEN ? AND ?";

        try (Connection conn = ConexionDB.connection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpresa);
            stmt.setDate(2, new java.sql.Date(fechaInicio.getTime()));
            stmt.setDate(3, new java.sql.Date(fechaFin.getTime()));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                stats[0] = rs.getInt("total");
                stats[1] = rs.getInt("pendientes");
                stats[2] = rs.getInt("aprobadas");
                stats[3] = rs.getInt("conobservaciones");
                stats[4] = rs.getInt("rechazadas");
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Elimina una auditoría
     * @param idAuditoria ID de la auditoría a eliminar
     * @return true si se eliminó exitosamente, false en caso contrario
     */
    public boolean eliminarAuditoria(int idAuditoria) {
        String sql = "DELETE FROM tblauditoriapartida WHERE idauditoria = ?";

        try (Connection conn = ConexionDB.connection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idAuditoria);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar auditoría: " + e.getMessage());
            return false;
        }
    }

    // ========== CLASE INTERNA PARA VISTA DE PARTIDAS CON AUDITORÍA ==========

    /**
     * Clase auxiliar para mostrar partidas con su información de auditoría en tablas
     */
    public static class PartidaAuditoriaView {
        private int idPartida;
        private Date fecha;
        private String concepto;
        private String contador;
        private String estadoAuditoria;
        private String tipoHallazgo;
        private Timestamp fechaAuditoria;
        private String auditor;

        // Getters y Setters
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

        public String getConcepto() {
            return concepto;
        }

        public void setConcepto(String concepto) {
            this.concepto = concepto;
        }

        public String getContador() {
            return contador;
        }

        public void setContador(String contador) {
            this.contador = contador;
        }

        public String getEstadoAuditoria() {
            return estadoAuditoria;
        }

        public void setEstadoAuditoria(String estadoAuditoria) {
            this.estadoAuditoria = estadoAuditoria;
        }

        public String getTipoHallazgo() {
            return tipoHallazgo;
        }

        public void setTipoHallazgo(String tipoHallazgo) {
            this.tipoHallazgo = tipoHallazgo;
        }

        public Timestamp getFechaAuditoria() {
            return fechaAuditoria;
        }

        public void setFechaAuditoria(Timestamp fechaAuditoria) {
            this.fechaAuditoria = fechaAuditoria;
        }

        public String getAuditor() {
            return auditor;
        }

        public void setAuditor(String auditor) {
            this.auditor = auditor;
        }
    }
}