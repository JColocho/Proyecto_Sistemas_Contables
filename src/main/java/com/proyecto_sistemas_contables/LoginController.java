package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.UsuarioModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

public class LoginController {
    @FXML
    private Button btnIniciarSesion;

    @FXML
    private Hyperlink linkCrearCuenta;

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
                    Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                    alerta.setContentText("Has iniciado sesión");
                    alerta.show();
                    NavbarController.idUsuarioSesion = usuario.idUsuarioSesion(txtUsuario.getText());
                    Main.setRoot("empresa-view");
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

        //Redirigimos al formulario para registrarse
        linkCrearCuenta.setOnAction(e -> {
            Main.setRoot("register-view");
        });
    }
}
