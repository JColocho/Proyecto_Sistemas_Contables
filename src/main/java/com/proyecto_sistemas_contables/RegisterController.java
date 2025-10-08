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
            //Validamos que todos los campos esten llenos y no esten vacios
            if (!txtApellido.getText().isEmpty() && !txtNombre.getText().isEmpty() && !txtNombreUsuario.getText().isEmpty() &&
                    !txtClave.getText().isEmpty() && !txtConfirmClave.getText().isEmpty() && !txtCorreo.getText().isEmpty()) {

                //Validamos que la clave y la clave de confirmación sean identicas
                if (txtClave.getText().equals(txtConfirmClave.getText())) {
                    //Validamos que haya seleccionado el nivel de acceso
                    if(cmbNivelAcceso.getSelectionModel().getSelectedItem() != null) {
                        //Validamos que el campo de clave de acceso no este vacía
                        if (!txtClaveAcceso.getText().isEmpty()){

                            AccesoModel acceso = new AccesoModel();
                            //Validamos que la clave de acceso ingresada sea la correcta para
                            //el acceso que está solicitando
                            if(acceso.darAcceso(
                                    cmbNivelAcceso.getSelectionModel().getSelectedItem().getIdAcceso(),
                                    txtClaveAcceso.getText())){

                                CorreoModel correoModel = new CorreoModel();
                                //Validamos que el correo ingresado no este registrado
                                if(!correoModel.correoExistente(txtCorreo.getText().toLowerCase())) {
                                    //Creamos el nuevo correo ingresado
                                    correoModel.crearCorreo(txtCorreo.getText().toLowerCase());

                                    //Creamos el nuevo usuario
                                    UsuarioModel usuarioModel = new UsuarioModel();
                                    usuarioModel.crearUsuario(txtNombreUsuario.getText(), txtNombre.getText(), txtApellido.getText(),
                                            txtClave.getText(), correoModel.buscarIdCorreo(txtCorreo.getText().toLowerCase()),
                                            cmbNivelAcceso.getSelectionModel().getSelectedItem().getIdAcceso());

                                    //Mandamos un mensaje de confirmación de creación de nuevo usuario
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                    alert.setTitle("Confirmacion");
                                    alert.setContentText("Se ha registrado correctamente.");
                                    alert.show();
                                    //Redirigimos al login
                                    Main.setRoot("login-view");
                                }
                                else{
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Error");
                                    alert.setContentText("El correo ingresado ya se encuentra registrado.");
                                    alert.show();
                                }
                            }
                            else{
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error");
                                alert.setContentText("La clave para el acceso solicitado es incorrecta: la clave de acceso debe ser " +
                                        "correspondiente al nivel de acceso solicitado.");
                                alert.show();
                            }

                        }
                        else{
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setContentText("No se ha ingresado la clave para el acceso solicitado: recuerda que la clave debe ser " +
                                    "para el acceso que se esta solicitando.");
                            alert.show();
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
                    alert.setContentText("La clave ingresada no coincide con la confirmación de clave, por favor revisar.");
                    alert.show();
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
