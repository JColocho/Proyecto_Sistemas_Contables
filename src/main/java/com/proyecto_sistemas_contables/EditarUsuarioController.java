package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.AccesoModel;
import com.proyecto_sistemas_contables.models.UsuarioModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class EditarUsuarioController {

    @FXML private Button btnCancelar;
    @FXML private Button btnEditarUsuario;

    @FXML private CheckBox chkMostrarClave;
    @FXML private CheckBox chkMostrarConfirmClave;

    @FXML private ComboBox<AccesoModel> cmbNivelAcceso;

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtNombreUsuario;
    @FXML private TextField txtCorreo;

    @FXML private TextField txtClave;
    @FXML private PasswordField txtClavePWD;

    @FXML private TextField txtConfirmClave;
    @FXML private PasswordField txtConfirmClavePWD;

    // Variables estáticas para recibir datos del controlador principal
    public static int idUsuarioEditar;
    public static int idUsuarioSesion;

    private String nombreUsuarioOriginal;
    private String correoOriginal;

    public void initialize() {
        try {
            // Sincronizar passwordfield ↔ textfield
            txtClavePWD.textProperty().addListener((obs, oldVal, newVal) -> txtClave.setText(newVal));
            txtClave.textProperty().addListener((obs, oldVal, newVal) -> txtClavePWD.setText(newVal));

            txtConfirmClavePWD.textProperty().addListener((obs, oldVal, newVal) -> txtConfirmClave.setText(newVal));
            txtConfirmClave.textProperty().addListener((obs, oldVal, newVal) -> txtConfirmClavePWD.setText(newVal));

            // Mostrar/ocultar clave
            chkMostrarClave.setOnAction(e -> {
                boolean mostrar = chkMostrarClave.isSelected();
                txtClave.setVisible(mostrar);
                txtClavePWD.setVisible(!mostrar);
                if (mostrar) {
                    txtClave.setText(txtClavePWD.getText());
                } else {
                    txtClavePWD.setText(txtClave.getText());
                }
            });

            // Mostrar/ocultar confirmación
            chkMostrarConfirmClave.setOnAction(e -> {
                boolean mostrar = chkMostrarConfirmClave.isSelected();
                txtConfirmClave.setVisible(mostrar);
                txtConfirmClavePWD.setVisible(!mostrar);
                if (mostrar) {
                    txtConfirmClave.setText(txtConfirmClavePWD.getText());
                } else {
                    txtConfirmClavePWD.setText(txtConfirmClave.getText());
                }
            });

            // Cargar niveles de acceso
            AccesoModel accesoModel = new AccesoModel();
            cmbNivelAcceso.setItems(accesoModel.listaAccesos());

            // Cargar datos del usuario a editar
            cargarDatosUsuario();

            // Acción del botón Editar
            btnEditarUsuario.setOnAction(e -> actualizarUsuario());

            // Acción del botón Cancelar
            btnCancelar.setOnAction(e -> {
                Stage stage = (Stage) btnCancelar.getScene().getWindow();
                stage.close();
            });

        } catch (Exception e) {
            System.out.println("Error en initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarDatosUsuario() throws SQLException {
        UsuarioModel usuario = UsuarioModel.obtenerUsuarioPorId(idUsuarioEditar);

        if (usuario != null) {
            txtNombre.setText(usuario.getNombre());
            txtApellido.setText(usuario.getApellido());
            txtNombreUsuario.setText(usuario.getNombreUsuario());
            txtCorreo.setText(usuario.getCorreo());

            // Guardar valores originales para validación
            nombreUsuarioOriginal = usuario.getNombreUsuario();
            correoOriginal = usuario.getCorreo();

            // Seleccionar nivel de acceso
            for (AccesoModel acceso : cmbNivelAcceso.getItems()) {
                if (acceso.getIdAcceso() == usuario.getIdAcceso()) {
                    cmbNivelAcceso.getSelectionModel().select(acceso);
                    break;
                }
            }
        }
    }

    private void actualizarUsuario() {
        // Validaciones
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre es obligatorio.");
            return;
        }

        if (txtApellido.getText().trim().isEmpty()) {
            mostrarError("El apellido es obligatorio.");
            return;
        }

        if (txtNombreUsuario.getText().trim().isEmpty()) {
            mostrarError("El nombre de usuario es obligatorio.");
            return;
        }

        if (txtCorreo.getText().trim().isEmpty()) {
            mostrarError("El correo es obligatorio.");
            return;
        }

        if (!validarCorreo(txtCorreo.getText().trim())) {
            mostrarError("El formato del correo no es válido.");
            return;
        }

        if (cmbNivelAcceso.getSelectionModel().getSelectedItem() == null) {
            mostrarError("Debe seleccionar un nivel de acceso.");
            return;
        }

        // Validar si el nombre de usuario ya existe (si fue modificado)
        if (!txtNombreUsuario.getText().trim().equals(nombreUsuarioOriginal)) {
            if (UsuarioModel.nombreUsuarioExiste(txtNombreUsuario.getText().trim())) {
                mostrarError("El nombre de usuario ya está en uso.");
                return;
            }
        }

        // Validar si el correo ya existe (si fue modificado)
        if (!txtCorreo.getText().trim().equalsIgnoreCase(correoOriginal)) {
            if (UsuarioModel.correoExiste(txtCorreo.getText().trim())) {
                mostrarError("El correo ya está registrado.");
                return;
            }
        }

        // Validar contraseña solo si se ingresó una nueva
        String claveActual = chkMostrarClave.isSelected() ? txtClave.getText() : txtClavePWD.getText();
        String confirmClaveActual = chkMostrarConfirmClave.isSelected() ? txtConfirmClave.getText() : txtConfirmClavePWD.getText();

        boolean cambiarClave = !claveActual.trim().isEmpty();

        if (cambiarClave) {
//            if (claveActual.length() < 6) {
//                mostrarError("La contraseña debe tener al menos 6 caracteres.");
//                return;
//            }

            if (!claveActual.equals(confirmClaveActual)) {
                mostrarError("Las contraseñas no coinciden.");
                return;
            }
        }

        // Crear objeto UsuarioModel con los datos actualizados
        UsuarioModel usuario = new UsuarioModel();
        usuario.setIdUsuario(idUsuarioEditar);
        usuario.setNombre(txtNombre.getText().trim());
        usuario.setApellido(txtApellido.getText().trim());
        usuario.setNombreUsuario(txtNombreUsuario.getText().trim());
        usuario.setCorreo(txtCorreo.getText().trim());
        usuario.setIdAcceso(cmbNivelAcceso.getSelectionModel().getSelectedItem().getIdAcceso());

        // Obtener el idCorreo actual del usuario
        try {
            UsuarioModel usuarioActual = UsuarioModel.obtenerUsuarioPorId(idUsuarioEditar);
            if (usuarioActual != null) {
                usuario.setIdCorreo(usuarioActual.getIdCorreo());
            }
        } catch (Exception e) {
            mostrarError("Error al obtener datos del usuario.");
            return;
        }

        // Solo establecer la clave si se va a cambiar
        if (cambiarClave) {
            usuario.setClave(claveActual);
        }

        // Actualizar en la base de datos
        if (UsuarioModel.actualizarUsuario(usuario, cambiarClave)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Actualización exitosa");
            alert.setHeaderText(null);
            alert.setContentText("El usuario ha sido actualizado correctamente.");
            alert.showAndWait();

            Stage stage = (Stage) btnEditarUsuario.getScene().getWindow();
            stage.close();
        } else {
            mostrarError("Error al actualizar el usuario. Intente nuevamente.");
        }
    }

    private boolean validarCorreo(String correo) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return correo.matches(regex);
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.show();
    }
}