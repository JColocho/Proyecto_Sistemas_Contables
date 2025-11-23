package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.AccesoModel;
import com.proyecto_sistemas_contables.models.EmpresaModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
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
    private BorderPane mainPane;

    @FXML
    private Button btnEmpresas;

    @FXML
    private Button btnCuentasT;

    @FXML
    private Button btnCerrarSesion;

    public static int idUsuarioSesion;
    public static int idEmpresaSesion;

    @FXML
    private void initialize() {
        // Guardar la instancia del controlador
        instance = this;
        Platform.runLater(() -> {
            try {
                if (mainPane.getScene() != null && mainPane.getScene().getWindow() != null) {
                    Stage stage = (Stage) mainPane.getScene().getWindow();
                    stage.setMaximized(false);
                    stage.setMaximized(true);
                }
            } catch (Exception e) {
                System.out.println("No se pudo maximizar la ventana: " + e.getMessage());
            }
        });

        // Cargar el dashboard por defecto al iniciar
        loadView("dashboard-view.fxml");

        // Inicializar IDs de sesión para otros controladores
        ReporteController.idUsuarioEnSesion = idUsuarioSesion;
        ReporteController.idEmpresaSesion = idEmpresaSesion;
        PartidasController.idUsuarioSesion = idUsuarioSesion;
        PartidasController.idEmpresaSesion = idEmpresaSesion;
        CatalogoCuentasController.idEmpresaSesion = idEmpresaSesion;
        EmpresaController.idUsuarioSesion = idUsuarioSesion;

        // CONFIGURAR PERMISOS SEGÚN EL ROL
        configurarPermisosPorRol();
    }

    /**
     * Cerrar sesión y volver al login
     */
    @FXML
    private void cerrarSesion() {
        try {
            // Limpiar datos de sesión
            LoginController.rolUsuarioSesion = null;
            EmpresaController.idUsuarioSesion = 0;
            idUsuarioSesion = 0;
            idEmpresaSesion = 0;

            // Obtener el Stage actual y cerrarlo
            Stage stageActual = (Stage) btnCerrarSesion.getScene().getWindow();
            stageActual.close();

            // Crear un nuevo Stage para el login
            Stage loginStage = new Stage();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("login-view.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root);
            loginStage.setScene(scene);
            loginStage.setTitle("Sistema Contable - Login");
            loginStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo cerrar sesión: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Configura la visibilidad y habilitación de botones según el rol del usuario
     */
    private void configurarPermisosPorRol() {
        int nivelAcceso = AccesoModel.obtenerNivelAccesoUsuario(idUsuarioSesion);

        // Por defecto, ocultar todos los botones restringidos
        btnUsuarios.setVisible(false);
        btnDocumentos.setVisible(false);
        btnAuditoria.setVisible(false);

        switch (nivelAcceso) {
            case AccesoModel.ROL_ADMINISTRADOR:
                // El administrador ve TODO
                btnUsuarios.setVisible(true);
                btnDocumentos.setVisible(true);
                btnAuditoria.setVisible(true);
                btnEmpresas.setVisible(true);
                btnCatalogo.setVisible(true);
                btnPartidas.setVisible(true);
                btnReporte.setVisible(true);
                btnDashboard.setVisible(true);
                break;

            case AccesoModel.ROL_CONTADOR:
                // El contador NO ve: Usuarios, Documentos, Auditoría
                btnUsuarios.setVisible(false);
                btnDocumentos.setVisible(false);
                btnAuditoria.setVisible(false);
                // Sí ve: Empresas, Catálogo, Partidas, Reportes, Dashboard
                btnEmpresas.setVisible(true);
                btnCatalogo.setVisible(true);
                btnPartidas.setVisible(true);
                btnReporte.setVisible(true);
                btnDashboard.setVisible(true);
                break;

            case AccesoModel.ROL_AUDITOR:
                // El auditor solo ve: Partidas (solo lectura), Reportes, Auditoría, Empresas
                btnUsuarios.setVisible(false);
                btnDocumentos.setVisible(false);
                btnCatalogo.setVisible(false);
                // Sí ve: Auditoría, Reportes, Dashboard, Empresas (solo lectura)
                btnEmpresas.setVisible(true);
                btnAuditoria.setVisible(true);
                btnPartidas.setVisible(true); // Solo lectura (se controla en el controlador)
                btnReporte.setVisible(true);
                btnDashboard.setVisible(true);
                break;

            default:
                // Sin permisos - solo dashboard
                btnDashboard.setVisible(true);
                break;
        }
    }

    @FXML
    private void cambiarVista(javafx.event.ActionEvent event) {
        Object source = event.getSource();

        if (source == btnDashboard) {
            loadView("dashboard-view.fxml");
        } else if (source == btnUsuarios) {
            // Verificar permiso antes de cargar
            if (AccesoModel.puedeGestionarUsuarios(idUsuarioSesion)) {
                loadView("usuarios-view.fxml");
            }
        } else if (source == btnAuditoria) {
            // Verificar permiso antes de cargar
            if (AccesoModel.puedeGestionarAuditorias(idUsuarioSesion)) {
                loadView("auditoria-view.fxml");
            }
        } else if (source == btnPartidas) {
            loadView("partidas-view.fxml");
        } else if (source == btnReporte) {
            loadView("reporte-view.fxml");
        } else if (source == btnDocumentos) {
            // Verificar permiso antes de cargar
            if (AccesoModel.puedeGestionarDocumentos(idUsuarioSesion)) {
                loadView("documentos-view.fxml");
            }
        } else if (source == btnEmpresas) {
            irAVistaEmpresas();
        } else if (source == btnCatalogo) {
            // Verificar permiso antes de cargar
            if (AccesoModel.puedeGestionarCatalogo(idUsuarioSesion)) {
                loadView("catalogo-cuentas-view.fxml");
            }
        }
    }

    private void loadView(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlName));
            AnchorPane view = loader.load();

            // INICIALIZAR CONTROLADORES SEGÚN LA VISTA
            if (fxmlName.equals("auditoria-view.fxml")) {
                AuditoriaController controller = loader.getController();
                controller.inicializarDatos(idUsuarioSesion, idEmpresaSesion);
            }

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
            EmpresaController.idUsuarioSesion = idUsuarioSesion;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("empresa-view.fxml"));
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