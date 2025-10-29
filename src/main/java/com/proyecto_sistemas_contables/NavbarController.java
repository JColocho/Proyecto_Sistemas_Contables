package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.EmpresaModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class NavbarController {

    // Variable estática para mantener la instancia del controlador
    private static NavbarController instance;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnAuditoria;

    @FXML
    private Button btnCatalogo;

    @FXML
    private Button btnDocumentos;

    @FXML
    private Button btnPartidas;

    @FXML
    private Button btnReporte;

    @FXML
    private Button btnUsuarios;

    @FXML
    private AnchorPane contentPane;

    @FXML
    private Button btnEmpresas;

    public static int idUsuarioSesion;

    @FXML
    private void initialize() {
        // Guardar la instancia del controlador
        instance = this;

        // Cargar el dashboard por defecto al iniciar
        loadView("dashboard-view.fxml");
        ReporteController.idUsuarioEnSesion = idUsuarioSesion;
        PartidasController.idUsuarioSesion = idUsuarioSesion;

        btnAuditoria.setVisible(false);
        btnCatalogo.setVisible(false);
        btnDocumentos.setVisible(false);
        btnUsuarios.setVisible(false);
    }

    @FXML
    private void cambiarVista(javafx.event.ActionEvent event) {
        Object source = event.getSource();

        if (source == btnDashboard) {
            loadView("dashboard-view.fxml");
        } else if (source == btnUsuarios) {
            loadView("usuarios-view.fxml");
        } else if (source == btnAuditoria) {
            loadView("auditoria-view.fxml");
        } else if (source == btnCatalogo) {
            loadView("catalogo-cuentas-view.fxml");
        } else if (source == btnPartidas) {
            loadView("partidas-view.fxml");
        } else if (source == btnReporte) {
            loadView("reporte-view.fxml");
        } else if (source == btnDocumentos) {
            loadView("documentos-view.fxml");
        } else if (source == btnEmpresas) {
            irAVistaEmpresas(); // Cambiar completamente la ventana
        }
    }

    private void loadView(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlName));
            AnchorPane view = loader.load();
            contentPane.getChildren().setAll(view);

            // Anclar la vista a todos los bordes del contenedor
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cambia completamente la ventana a la vista de empresas (sin navbar)
     */
    private void irAVistaEmpresas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view_seleccionar_empresa.fxml"));
            Parent root = loader.load();

            // Obtener el Stage actual
            Stage stage = (Stage) contentPane.getScene().getWindow();

            // Cambiar la escena completa
            stage.setScene(new Scene(root));
            stage.setTitle("Seleccionar Empresa");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar vista de empresas: " + e.getMessage());
        }
    }

    // ============================================
    // MÉTODOS PÚBLICOS ESTÁTICOS PARA NAVEGACIÓN
    // ============================================

    /**
     * Carga una vista desde cualquier otro controlador
     * @param fxmlName Nombre del archivo FXML a cargar
     */
    public static void cargarVista(String fxmlName) {
        if (instance != null) {
            instance.loadView(fxmlName);
        } else {
            System.err.println("Error: NavbarController no está inicializado");
        }
    }

    /**
     * Carga el dashboard y le pasa una empresa específica
     * @param empresa La empresa seleccionada
     */
    public static void cargarDashboardConEmpresa(EmpresaModel empresa) {
        if (instance != null) {
            try {
                FXMLLoader loader = new FXMLLoader(NavbarController.class.getResource("dashboard-view.fxml"));
                AnchorPane view = loader.load();

                // Pasar datos al controlador del dashboard si existe el método
                DashboardController controller = loader.getController();
                if (controller != null) {
                    controller.setEmpresa(empresa);
                }

                // Cargar la vista en el contentPane
                instance.contentPane.getChildren().setAll(view);
                AnchorPane.setTopAnchor(view, 0.0);
                AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setLeftAnchor(view, 0.0);
                AnchorPane.setRightAnchor(view, 0.0);

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error al cargar dashboard con empresa: " + e.getMessage());
            }
        } else {
            System.err.println("Error: NavbarController no está inicializado");
        }
    }
}