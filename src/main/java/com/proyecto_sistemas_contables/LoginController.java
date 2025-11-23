package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.UsuarioModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    public static String rolUsuarioSesion;

    @FXML
    private Button btnIniciarSesion;

    @FXML
    private PasswordField txtClavePWD;

    @FXML
    private TextField txtClave;

    @FXML
    private TextField txtUsuario;

    @FXML
    private CheckBox chkMostrarClave;

    public void initialize() {
        //Reescribimos en un textField lo que contiene el passwordField y viceversa
        //Dependiendo si el usuario desea ver la contraseña que está ingresando
        txtClavePWD.setOnKeyTyped(event -> {
            txtClave.setText(txtClavePWD.getText());
        });
        txtClave.setOnKeyTyped(event -> {
            txtClavePWD.setText(txtClave.getText());
        });

        chkMostrarClave.setOnAction(e -> {
            if (chkMostrarClave.isSelected()) {
                txtClavePWD.setVisible(false);
                txtClave.setVisible(true);
            }
            else {
                txtClavePWD.setVisible(true);
                txtClave.setVisible(false);
            }
        });

        btnIniciarSesion.setOnAction(e -> {
            //Validamos que los campos no estén vacíos
            if(!txtUsuario.getText().isEmpty() && !txtClave.getText().isEmpty()) {

                UsuarioModel usuario = new UsuarioModel();
                //Validamos que las credenciales ingresadas sean correctas
                if (usuario.inicioSesion(txtUsuario.getText(), txtClave.getText())) {
                    EmpresaController.idUsuarioSesion = usuario.idUsuarioSesion(txtUsuario.getText());

                    // Obtener el rol del usuario
                    EmpresaController.rolUsuarioSesion = usuario.obtenerRolUsuario(txtUsuario.getText());

                    try {
                        // Cerrar la ventana del login
                        Stage loginStage = (Stage) btnIniciarSesion.getScene().getWindow();
                        loginStage.close();

                        // Abrir nueva ventana con empresa-view
                        Stage empresaStage = new Stage();
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("empresa-view.fxml")
                        );
                        Parent root = loader.load();
                        Scene scene = new Scene(root);
                        empresaStage.setScene(scene);
                        empresaStage.setTitle("Sistema Contable - Seleccionar Empresa");
                        empresaStage.show();

                        // AHORA SÍ MOSTRAR EL MENSAJE
                        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                        alerta.setContentText("Has iniciado sesión.");
                        alerta.show();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Alert alertaError = new Alert(Alert.AlertType.ERROR);
                        alertaError.setContentText("Error al abrir ventana de empresas: " + ex.getMessage());
                        alertaError.show();
                    }
                }
                else {
                    Alert alerta = new Alert(Alert.AlertType.ERROR);
                    alerta.setContentText("Contraseña o usuario incorrectos");
                    alerta.show();
                }
            }
            else{
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setContentText("Campos vacios");
                alerta.show();
            }
        });
    }
}