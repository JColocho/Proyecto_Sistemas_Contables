package com.proyecto_sistemas_contables;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class NavbarController {
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
    public static int idUsuarioSesion;

    @FXML
    private void initialize() {
        // Cargar el dashboard por defecto al iniciar
        loadView("dashboard-view.fxml");
        ReporteController.idUsuarioEnSesion = idUsuarioSesion;
        System.out.println(idUsuarioSesion);
    }

    @FXML
    private void cambiarVista(javafx.event.ActionEvent event) {
        Object source = event.getSource();

        if (source == btnDashboard) {
            loadView("dashboard-view.fxml");
        } else if (source == btnUsuarios) {
            loadView("usuarios-view.fxml");
        }else if (source == btnAuditoria) {
            loadView("auditoria-view.fxml");
        }else if (source == btnCatalogo) {
            loadView("catalogo-cuentas-view.fxml");
        }else if (source == btnPartidas) {
            loadView("partidas-view.fxml");
        }else if (source == btnReporte) {
            loadView("reporte-view.fxml");
        }else if (source == btnDocumentos) {
            loadView("documentos-view.fxml");
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
}

