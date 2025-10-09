package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SeleccionarEmpresaController {

    @FXML
    private FlowPane fpEmpresas; // Contenedor dinámico de empresas

    @FXML
    private AnchorPane formulario_empresa;

    @FXML
    private TextField txt_nombre_empresa, txt_nit, txt_nrc, txt_direccion, txt_telefono, txt_correo;

    @FXML
    private ComboBox<String> cb_actividad_eco;

    @FXML
    private Button btn_agregar, btn_cancelar;

    @FXML
    private void initialize() {
        // Inicializar formulario oculto
        formulario_empresa.setVisible(false);

        // Inicializar ComboBox de actividad económica
        cb_actividad_eco.getItems().addAll(
                "Comercio", "Servicios", "Manufactura", "Construcción", "Turismo"
        );

        // Cargar empresas existentes de la base de datos
        cargarEmpresas();
    }

    // Mostrar formulario al hacer click en "Agregar empresa"
    @FXML
    void abrirFormularioEmpresa(MouseEvent event) {
        formulario_empresa.setVisible(true);
    }

    // Cancelar formulario
    @FXML
    private void cancelar() {
        formulario_empresa.setVisible(false);
    }

    // Agregar empresa
    @FXML
    private void agregarEmpresa() {
        String nombre = txt_nombre_empresa.getText();
        String nit = txt_nit.getText();
        String nrc = txt_nrc.getText();
        String direccion = txt_direccion.getText();
        String telefono = txt_telefono.getText();
        String correo = txt_correo.getText();
        String actividad_economica = cb_actividad_eco.getSelectionModel().getSelectedItem();

        if (nombre.isEmpty() || nit.isEmpty() || correo.isEmpty()) {
            mostrarAlerta("Error", "Debe completar los campos obligatorios.", Alert.AlertType.ERROR);
            return;
        }

        try (Connection conn = ConexionDB.connection()) {
            // Insertar correo y obtener id
            String sqlCorreo = "INSERT INTO tblcorreos (correo) VALUES (?) RETURNING idcorreo";
            PreparedStatement psCorreo = conn.prepareStatement(sqlCorreo);
            psCorreo.setString(1, correo);
            ResultSet rsCorreo = psCorreo.executeQuery();
            int idCorreo = 0;
            if (rsCorreo.next()) {
                idCorreo = rsCorreo.getInt("idcorreo");
            }

            // Insertar empresa con actividad económica
            String sqlEmpresa = """
                    INSERT INTO tblempresas (nombre, nit, nrc, direccion, telefono, idcorreo, actividad_economica)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    RETURNING idempresa
                    """;
            PreparedStatement psEmpresa = conn.prepareStatement(sqlEmpresa);
            psEmpresa.setString(1, nombre);
            psEmpresa.setString(2, nit);
            psEmpresa.setString(3, nrc);
            psEmpresa.setString(4, direccion);
            psEmpresa.setString(5, telefono);
            psEmpresa.setInt(6, idCorreo);
            psEmpresa.setString(7, actividad_economica);

            ResultSet rsEmpresa = psEmpresa.executeQuery();
            int idEmpresa = 0;
            if (rsEmpresa.next()) {
                idEmpresa = rsEmpresa.getInt("idempresa");
            }

            // Agregar empresa al FlowPane dinámicamente (solo el nombre)
            agregarEmpresaAlGrid(nombre, idEmpresa);

            // Limpiar formulario y ocultarlo
            txt_nombre_empresa.clear();
            txt_nit.clear();
            txt_nrc.clear();
            txt_direccion.clear();
            txt_telefono.clear();
            txt_correo.clear();
            cb_actividad_eco.getSelectionModel().clearSelection();
            formulario_empresa.setVisible(false);

            mostrarAlerta("Éxito", "Empresa registrada correctamente.", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo registrar la empresa.", Alert.AlertType.ERROR);
        }
    }

    // Agregar empresa al FlowPane (solo nombre visible)
    private void agregarEmpresaAlGrid(String nombre, int idEmpresa) {
        AnchorPane empresaPane = new AnchorPane();
        empresaPane.setPrefSize(120, 75);
        empresaPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ccc; -fx-border-radius: 5px; -fx-background-radius: 5px;");

        Label lblNombre = new Label(nombre);
        lblNombre.setLayoutX(10);
        lblNombre.setLayoutY(10);

        Button btnEliminar = new Button("X");
        btnEliminar.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        btnEliminar.setLayoutX(80);
        btnEliminar.setLayoutY(10);
        btnEliminar.setVisible(false);

        // Acción eliminar
        btnEliminar.setOnAction(e -> {
            eliminarEmpresa(idEmpresa);
            fpEmpresas.getChildren().remove(empresaPane);
        });

        empresaPane.setOnMouseEntered(e -> btnEliminar.setVisible(true));
        empresaPane.setOnMouseExited(e -> btnEliminar.setVisible(false));

        empresaPane.getChildren().addAll(lblNombre, btnEliminar);
        fpEmpresas.getChildren().add(empresaPane);
    }

    // Cargar empresas desde la base de datos
    private void cargarEmpresas() {
        try (Connection conn = ConexionDB.connection()) {
            String sql = "SELECT e.idempresa, e.nombre FROM tblempresas e";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("idempresa");
                String nombre = rs.getString("nombre");
                agregarEmpresaAlGrid(nombre, id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Eliminar empresa de la base de datos
    private void eliminarEmpresa(int idEmpresa) {
        try (Connection conn = ConexionDB.connection()) {
            String sql = "DELETE FROM tblempresas WHERE idempresa=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idEmpresa);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo eliminar la empresa.", Alert.AlertType.ERROR);
        }
    }

    // Mostrar alertas
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}