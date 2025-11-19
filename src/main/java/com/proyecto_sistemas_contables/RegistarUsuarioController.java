package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.AccesoModel;
import com.proyecto_sistemas_contables.models.CorreoModel;
import com.proyecto_sistemas_contables.models.UsuarioModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegistarUsuarioController {
    @FXML private Button btnCrearUsuario;
    @FXML private Button btnCancelar;

    @FXML private ComboBox<AccesoModel> cmbNivelAcceso;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtNombreUsuario;
    @FXML private TextField txtClave;
    @FXML private PasswordField txtClavePWD;
    @FXML private TextField txtConfirmClave;
    @FXML private PasswordField txtConfirmClavePWD;

    @FXML private CheckBox chkMostrarClave;
    @FXML private CheckBox chkMostrarConfirmClave;

    public void initialize() {

        // sincronizar passwordfield ↔ textfield
        txtClavePWD.setOnKeyTyped(e -> txtClave.setText(txtClavePWD.getText()));
        txtConfirmClavePWD.setOnKeyTyped(e -> txtConfirmClave.setText(txtConfirmClavePWD.getText()));

        // mostrar/ocultar clave
        chkMostrarClave.setOnAction(e -> {
            boolean mostrar = chkMostrarClave.isSelected();
            txtClave.setVisible(mostrar);
            txtClavePWD.setVisible(!mostrar);
        });

        // mostrar/ocultar confirmación
        chkMostrarConfirmClave.setOnAction(e -> {
            boolean mostrar = chkMostrarConfirmClave.isSelected();
            txtConfirmClave.setVisible(mostrar);
            txtConfirmClavePWD.setVisible(!mostrar);
        });

        // cargar niveles de acceso
        AccesoModel accesoModel = new AccesoModel();
        cmbNivelAcceso.setItems(accesoModel.listaAccesos());

        btnCrearUsuario.setOnAction(e -> {
            // capturar datos
            String nombre = txtNombre.getText().trim().replace("   ", " ").replace("  ", " ").toUpperCase();
            String apellido = txtApellido.getText().trim().replace("   ", " ").replace("  ", " ").toUpperCase();
            String nombreUsuario = txtNombreUsuario.getText().trim();
            String clave = txtClave.getText();
            String confirmClave = txtConfirmClave.getText();
            String correo = txtCorreo.getText().trim().toLowerCase().replace("   ", " ").replace("  ", " ");

            // validar campos vacíos
            if (!apellido.isEmpty() && !nombre.isEmpty() && !nombreUsuario.isEmpty() &&
                    !clave.isEmpty() && !confirmClave.isEmpty() && !correo.isEmpty()) {

                // validar coincidencia de clave
                if (clave.equals(confirmClave)) {
                    CorreoModel correoModel = new CorreoModel();
                    if (!correoModel.correoExistente(correo)) {
                        UsuarioModel usuarioModel = new UsuarioModel();
                        if (!usuarioModel.usuarioExistente(nombreUsuario)) {
                            if (cmbNivelAcceso.getSelectionModel().getSelectedItem() != null) {
                                // crear correo
                                correoModel.crearCorreo(correo);

                                // crear usuario
                                usuarioModel.crearUsuario(
                                        nombreUsuario,
                                        nombre,
                                        apellido,
                                        clave,
                                        correoModel.buscarIdCorreo(correo),
                                        cmbNivelAcceso.getSelectionModel().getSelectedItem().getIdAcceso()
                                );

                                // mensaje
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Confirmación");
                                alert.setContentText("Se ha registrado correctamente.");
                                alert.show();
                            } else {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error");
                                alert.setContentText("Debe seleccionar un nivel de acceso.");
                                alert.show();
                            }
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setContentText("El nombre de usuario ya existe.");
                            alert.show();
                            txtNombreUsuario.requestFocus();
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setContentText("El correo ingresado ya está registrado.");
                        alert.show();
                        txtCorreo.requestFocus();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("La contraseña y la confirmación no coinciden.");
                    alert.show();
                    txtConfirmClavePWD.requestFocus();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Debe completar todos los campos.");
                alert.show();
            }
        });

        //salir del dialogo
        btnCancelar.setOnAction(e -> {
            Stage stage = (Stage) btnCancelar.getScene().getWindow();
            stage.close();
        });
    }
}
