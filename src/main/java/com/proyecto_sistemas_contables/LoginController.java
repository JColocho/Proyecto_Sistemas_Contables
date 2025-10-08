package com.proyecto_sistemas_contables;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class LoginController {
    @FXML
    private Button btnIniciarSesion;

    @FXML
    private Hyperlink linkCrearCuenta;

    @FXML
    private Text textMensaje;

    @FXML
    private TextField txtClave;

    @FXML
    private TextField txtUsuario;

    public void initialize() {
        btnIniciarSesion.setOnAction(e -> {

        });
        linkCrearCuenta.setOnAction(e -> {

        })
    }
}
