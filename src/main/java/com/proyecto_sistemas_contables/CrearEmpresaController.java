package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CrearEmpresaController {
    @FXML
    private Button btn_agregar;

    @FXML
    private AnchorPane btn_agregar_empresa;

    @FXML
    private AnchorPane formulario_empresa;

    @FXML
    private Button btn_cancelar;

    @FXML
    private ComboBox<String> cb_actividad_eco;

    @FXML
    private TextField txt_correo;

    @FXML
    private TextField txt_direccion;

    @FXML
    private TextField txt_nit;

    @FXML
    private TextField txt_nombre_empresa;

    @FXML
    private TextField txt_nrc;

    @FXML
    private TextField txt_telefono;

    @FXML
    private void initialize() {
        //Inicializar formulario como invisible
        formulario_empresa.setVisible(false);
    }

    @FXML
    private void abrirFormularioEmpresa(){
        formulario_empresa.setVisible(true);
    }

    @FXML
    private void agregarEmpresa(){
        String nombre = txt_nombre_empresa.getText();
        String nit = txt_nit.getText();
        String nrc = txt_nrc.getText();
        String direccion = txt_direccion.getText();
        String telefono = txt_telefono.getText();
        String correo = txt_correo.getText();

        if (nombre.isEmpty() || nit.isEmpty() || correo.isEmpty()) {
            mostrarAlerta("Error", "Debe completar los campos obligatorios.", Alert.AlertType.ERROR);
            return;
        }

        try (Connection conn = ConexionDB.connection()){
            //Insertar correo y obtener id
            String sqlCorreo = "INSERT INTO tblcorreos (correos) VALUES (?) RETURNING idcorreo";
            PreparedStatement psCorreo = conn.prepareStatement(sqlCorreo);
            psCorreo.setString(1, correo);
            ResultSet rs = psCorreo.executeQuery();
            int idCorreo = 0;
            if (rs.next()){
                idCorreo = rs.getInt("idcorreo");
            }

            //Insertar empresa
            String sqlEmpresa = """
                    INSERT INTO tblempresas (nombre, nit, nrc, direccion, telefono, idcorreo)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
            PreparedStatement psEmpresa = conn.prepareStatement(sqlEmpresa);
            psEmpresa.setString(1, nombre);
            psEmpresa.setString(2, nit);
            psEmpresa.setString(3, nrc);
            psEmpresa.setString(4, direccion);
            psEmpresa.setString(5, telefono);
            psEmpresa.setInt(6, idCorreo);

            psEmpresa.executeUpdate();

            mostrarAlerta("Éxito", "Empresa registrada correctamente.", Alert.AlertType.INFORMATION);

            txt_nombre_empresa.clear();
            txt_nit.clear();
            txt_nrc.clear();
            txt_direccion.clear();
            txt_telefono.clear();
            txt_correo.clear();
            cb_actividad_eco.getSelectionModel().clearSelection();
            formulario_empresa.setVisible(false);
        } catch (Exception e){
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo registrar la empresa.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cancelar() {
        btn_agregar_empresa.setVisible(false);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo){
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
