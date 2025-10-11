package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.AccesoModel;
import com.proyecto_sistemas_contables.models.CorreoModel;
import com.proyecto_sistemas_contables.models.UsuarioModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {
    @FXML
    private Button btnCrearUsuario;

    @FXML
    private ComboBox<AccesoModel> cmbNivelAcceso;

    @FXML
    private Hyperlink linkIniciarSesion;

    @FXML
    private TextField txtApellido;

    @FXML
    private TextField txtClave;

    @FXML
    private TextField txtClaveAcceso;

    @FXML
    private PasswordField txtClaveAccesoPWD;

    @FXML
    private PasswordField txtClavePWD;

    @FXML
    private TextField txtConfirmClave;

    @FXML
    private PasswordField txtConfirmClavePWD;

    @FXML
    private CheckBox chkMostrarClave;

    @FXML
    private CheckBox chkMostrarClaveAcceso;

    @FXML
    private CheckBox chkMostrarConfirmClave;

    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtNombreUsuario;

    public void initialize() {

        //Reescribimos en un textField lo que contiene el passwordField y viceversa
        //Dependiendo si el usuario desea ver la contraseña que está ingresando
        txtClavePWD.setOnKeyTyped(e ->{
            txtClave.setText(txtClavePWD.getText());
        });
        txtConfirmClavePWD.setOnKeyTyped(e ->{
            txtConfirmClave.setText(txtConfirmClavePWD.getText());
        });
        txtClaveAccesoPWD.setOnKeyTyped(e ->{
           txtClaveAcceso.setText(txtClaveAccesoPWD.getText());
        });

        chkMostrarClave.setOnAction(e ->{
            if (chkMostrarClave.isSelected()) {
                txtClave.setVisible(true);
                txtClavePWD.setVisible(false);
            }
            else{
                txtClave.setVisible(false);
                txtClavePWD.setVisible(true);
            }
        });
        chkMostrarConfirmClave.setOnAction(e ->{
            if (chkMostrarConfirmClave.isSelected()) {
                txtConfirmClave.setVisible(true);
                txtConfirmClavePWD.setVisible(false);
            }
            else{
                txtConfirmClave.setVisible(false);
                txtConfirmClavePWD.setVisible(true);
            }
        });
        chkMostrarClaveAcceso.setOnAction(e ->{
            if (chkMostrarClaveAcceso.isSelected()) {
                txtClaveAcceso.setVisible(true);
                txtClaveAccesoPWD.setVisible(false);
            }
            else{
                txtClaveAcceso.setVisible(false);
                txtClaveAccesoPWD.setVisible(true);
            }
        });

        //Cargamos todos los niveles de accesos
        AccesoModel accesoModel = new AccesoModel();
        cmbNivelAcceso.setItems(accesoModel.listaAccesos());

        //Redirigimos al formulario para que se loguee
        linkIniciarSesion.setOnAction(e -> {
           Main.setRoot("login-view");
        });

        btnCrearUsuario.setOnAction(e -> {

            //Declaramos las variables y capturamos los datos ingresados
            String nombre = txtNombre.getText().trim().replace("   ", " ").replace("  ", " ").toUpperCase();
            String apellido = txtApellido.getText().trim().replace("   ", " ").replace("  ", " ").toUpperCase();
            String nombreUsuario = txtNombreUsuario.getText().trim();
            String clave = txtClave.getText();
            String confirmClave = txtConfirmClave.getText();
            String claveAcceso = txtClaveAcceso.getText();
            String correo = txtCorreo.getText().trim().toLowerCase().replace("   ", " ").replace("  ", " ");

            //Validamos que todos los campos esten llenos y no esten vacios
            if (!apellido.isEmpty() && !nombre.isEmpty() && !nombreUsuario.isEmpty() &&
                    !clave.isEmpty() && !confirmClave.isEmpty() && !correo.isEmpty()) {

                //Validamos que la clave y la clave de confirmación sean identicas
                if (clave.equals(confirmClave)) {
                    CorreoModel correoModel = new CorreoModel();
                    //Validamos que el correo ingresado no este registrado
                    if(!correoModel.correoExistente(correo)) {
                        UsuarioModel usuarioModel = new UsuarioModel();
                        if(!usuarioModel.usuarioExistente(nombreUsuario)){
                            //Validamos que haya seleccionado el nivel de acceso
                            if(cmbNivelAcceso.getSelectionModel().getSelectedItem() != null) {
                                //Validamos que el campo de clave de acceso no este vacía
                                if (!claveAcceso.isEmpty()){

                                    AccesoModel acceso = new AccesoModel();
                                    //Validamos que la clave de acceso ingresada sea la correcta para
                                    //el acceso que está solicitando
                                    if(acceso.darAcceso(
                                            cmbNivelAcceso.getSelectionModel().getSelectedItem().getIdAcceso(),
                                            claveAcceso)){

                                        //Creamos el nuevo correo ingresado
                                        correoModel.crearCorreo(correo);

                                        //Creamos el nuevo usuario
                                        usuarioModel.crearUsuario(nombreUsuario, nombre,
                                                apellido, clave,
                                                correoModel.buscarIdCorreo(correo),
                                                cmbNivelAcceso.getSelectionModel().getSelectedItem().getIdAcceso());

                                        //Mandamos un mensaje de confirmación de creación de nuevo usuario
                                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                        alert.setTitle("Confirmacion");
                                        alert.setContentText("Se ha registrado correctamente.");
                                        alert.show();
                                        //Redirigimos al login
                                        Main.setRoot("login-view");


                                    }
                                    else{
                                        Alert alert = new Alert(Alert.AlertType.ERROR);
                                        alert.setTitle("Error");
                                        alert.setContentText("La clave para el acceso solicitado es incorrecta: la clave de acceso debe ser " +
                                                "correspondiente al nivel de acceso solicitado.");
                                        alert.show();
                                        txtClaveAccesoPWD.requestFocus();
                                        txtClaveAcceso.requestFocus();
                                    }

                                }
                                else{
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Error");
                                    alert.setContentText("No se ha ingresado la clave para el acceso solicitado: recuerda que la clave debe ser " +
                                            "para el acceso que se esta solicitando.");
                                    alert.show();
                                    txtClaveAccesoPWD.requestFocus();
                                    txtClaveAcceso.requestFocus();
                                }

                            }
                            else {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error");
                                alert.setContentText("No se ha seleccionado el acceso para su usuario");
                                alert.show();
                            }
                        }
                        else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setContentText("El nombre de usuario ya existe, ingrese otro nombre de usuario.");
                            alert.show();
                            txtNombreUsuario.requestFocus();
                        }
                    }
                    else{
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setContentText("El correo ingresado ya se encuentra registrado.");
                        alert.show();
                        txtCorreo.requestFocus();
                    }

                }
                else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("La clave ingresada no coincide con la confirmación de clave, por favor revisar.");
                    alert.show();
                    txtConfirmClavePWD.requestFocus();
                    txtConfirmClave.requestFocus();
                }
            }
            else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Los campos del formulario están vacíos, completar los campos del formulario.");
                alert.show();
            }
        });
    }
}
