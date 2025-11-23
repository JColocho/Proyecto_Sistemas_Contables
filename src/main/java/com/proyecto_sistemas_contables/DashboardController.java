package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.EmpresaModel;
import com.proyecto_sistemas_contables.models.UsuarioModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML
    private Label lblGastosTotales;

    @FXML
    private Label lblIngresosTotales;

    @FXML
    private Label lblTextoUtilidadPerdida;

    @FXML
    private Label lblUtilidadPerdida;

    @FXML
    private Label lblNombreUsuario;

    @FXML
    private Label lblRolUsuario;

    private EmpresaModel empresaSeleccionada;

    @FXML
    private void initialize() {
        // Cargar información del usuario
        cargarInformacionUsuario();
    }

    /**
     * Método para recibir la empresa seleccionada desde otro controlador
     */
    public void setEmpresa(EmpresaModel empresa) {
        this.empresaSeleccionada = empresa;
    }

    /**
     * Cargar nombre y rol del usuario actual
     */
    private void cargarInformacionUsuario() {
        try {
            // Obtener nombre del usuario
            UsuarioModel usuarioModel = new UsuarioModel();
            String nombreUsuario = usuarioModel.obtenerNombreUsuario(EmpresaController.idUsuarioSesion);

            // Obtener rol del usuario
            String rol = LoginController.rolUsuarioSesion;

            // Mostrar en los labels
            lblNombreUsuario.setText(nombreUsuario);
            lblRolUsuario.setText(rol != null ? rol : "Sin rol");

        } catch (Exception e) {
            lblNombreUsuario.setText("Usuario desconocido");
            lblRolUsuario.setText("Sin rol");
        }
    }
}