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

    public AccesoModel(int idAcceso, String nivelAcceso, String claveAcceso) {
        this.idAcceso = idAcceso;
        this.nivelAcceso = nivelAcceso;
        this.claveAcceso = claveAcceso;
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

    // ========== MÉTODOS ORIGINALES (EXISTENTES) ==========

    /**
     * Obtiene la lista de todos los niveles de acceso para mostrar en ComboBox
     * @return Lista observable de AccesoModel
     */
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

    /**
     * Valida si la clave de acceso es correcta para un nivel de acceso
     * @param idAcceso ID del nivel de acceso
     * @param claveIngresada Clave ingresada por el usuario
     * @return true si la clave es correcta, false en caso contrario
     */
    public boolean darAcceso(int idAcceso, String claveIngresada) {
        try {
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT claveacceso FROM tblaccesos WHERE idacceso = ?"
            );
            statement.setInt(1, idAcceso);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String claveCorrecta = rs.getString("claveacceso");
                return claveIngresada.equals(claveCorrecta);
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error al validar acceso: " + e.getMessage());
        }

        return false;
    }

    // ========== MÉTODOS NUEVOS PARA SISTEMA DE PERMISOS ==========

    /**
     * Obtiene el nivel de acceso (rol) de un usuario
     * @param idUsuario ID del usuario
     * @return ID del nivel de acceso (1=Admin, 2=Contador, 3=Auditor) o -1 si no existe
     */
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

    /**
     * Obtiene el nombre del rol de un usuario
     * @param idUsuario ID del usuario
     * @return Nombre del rol ("Administrador", "Contador", "Auditor") o null
     */
    public static String obtenerNombreRol(int idUsuario) {
        String sql = "SELECT a.nivelacceso FROM tblusuarios u " +
                "INNER JOIN tblaccesos a ON u.idacceso = a.idacceso " +
                "WHERE u.idusuario = ?";

        try (Connection conn = ConexionDB.connection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("nivelacceso");
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener nombre de rol: " + e.getMessage());
        }

        return null;
    }

    // ========== MÉTODOS DE VALIDACIÓN DE PERMISOS ==========

    /**
     * Verifica si un usuario es Administrador
     */
    public static boolean esAdministrador(int idUsuario) {
        return obtenerNivelAccesoUsuario(idUsuario) == ROL_ADMINISTRADOR;
    }

    /**
     * Verifica si un usuario es Contador
     */
    public static boolean esContador(int idUsuario) {
        return obtenerNivelAccesoUsuario(idUsuario) == ROL_CONTADOR;
    }

    /**
     * Verifica si un usuario es Auditor
     */
    public static boolean esAuditor(int idUsuario) {
        return obtenerNivelAccesoUsuario(idUsuario) == ROL_AUDITOR;
    }

    // ========== PERMISOS ESPECÍFICOS POR FUNCIONALIDAD ==========

    /**
     * Puede gestionar usuarios (crear, editar, eliminar)
     * Solo: Administrador
     */
    public static boolean puedeGestionarUsuarios(int idUsuario) {
        return esAdministrador(idUsuario);
    }

    /**
     * Puede gestionar empresas
     * Solo: Administrador
     */
    public static boolean puedeGestionarEmpresas(int idUsuario) {
        return esAdministrador(idUsuario);
    }

    /**
     * Puede gestionar catálogo de cuentas
     * Solo: Administrador y Contador
     */
    public static boolean puedeGestionarCatalogo(int idUsuario) {
        int nivel = obtenerNivelAccesoUsuario(idUsuario);
        return nivel == ROL_ADMINISTRADOR || nivel == ROL_CONTADOR;
    }

    /**
     * Puede gestionar clasificación de documentos
     * Solo: Administrador
     */
    public static boolean puedeGestionarDocumentos(int idUsuario) {
        return esAdministrador(idUsuario);
    }

    /**
     * Puede crear y editar partidas
     * Solo: Administrador y Contador
     */
    public static boolean puedeEditarPartidas(int idUsuario) {
        int nivel = obtenerNivelAccesoUsuario(idUsuario);
        return nivel == ROL_ADMINISTRADOR || nivel == ROL_CONTADOR;
    }

    /**
     * Puede ver partidas (solo lectura)
     * Todos los roles
     */
    public static boolean puedeVerPartidas(int idUsuario) {
        return obtenerNivelAccesoUsuario(idUsuario) != -1;
    }

    /**
     * Puede generar reportes
     * Todos los roles
     */
    public static boolean puedeGenerarReportes(int idUsuario) {
        return obtenerNivelAccesoUsuario(idUsuario) != -1;
    }

    /**
     * Puede gestionar auditorías (crear, editar observaciones)
     * Solo: Administrador y Auditor
     */
    public static boolean puedeGestionarAuditorias(int idUsuario) {
        int nivel = obtenerNivelAccesoUsuario(idUsuario);
        return nivel == ROL_ADMINISTRADOR || nivel == ROL_AUDITOR;
    }

    /**
     * Puede ver auditorías (solo consulta)
     * Todos los roles
     */
    public static boolean puedeVerAuditorias(int idUsuario) {
        return obtenerNivelAccesoUsuario(idUsuario) != -1;
    }

    /**
     * Puede cerrar sesión
     * Todos los roles
     */
    public static boolean puedeCerrarSesion(int idUsuario) {
        return obtenerNivelAccesoUsuario(idUsuario) != -1;
    }
}