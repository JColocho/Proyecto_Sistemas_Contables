package com.proyecto_sistemas_contables;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;

public class RegisterController {
    @FXML
    private Button btnCrearUsuario;

    @FXML
    private ComboBox<?> cmbNivelAcceso;

    @FXML
    private Hyperlink linkIniciarSesion;

    @FXML
    private TextField txtApellido;

    @FXML
    private TextField txtClave;

    @FXML
    private TextField txtConfirmClave;

    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtNombreUsuario;
}
