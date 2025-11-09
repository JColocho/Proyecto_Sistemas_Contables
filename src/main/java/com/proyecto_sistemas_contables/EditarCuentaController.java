package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.CatalogoCuentaModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

public class EditarCuentaController {

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnEditarCuenta;

    @FXML
    private ComboBox<String> cmbTipoCuenta;

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtCuenta;

    public static CatalogoCuentaModel datosCuentaActual;
    public static int idEmpresaSesion;

    public void initialize() {
        //Cargar los tipos de cuentas para la cuenta
        cmbTipoCuenta.getItems().addAll(
                "ACTIVO CORRIENTE",
                "ACTIVO NO CORRIENTE",
                "PASIVO CORRIENTE",
                "PASIVO NO CORRIENTE",
                "CAPITAL",
                "INGRESOS O VENTAS",
                "GASTOS",
                "COSTOS"
        );

        //Mostrar los datos actuales de la cuenta
        txtCodigo.setText(datosCuentaActual.getCodigoCuenta());
        txtCuenta.setText(datosCuentaActual.getCuenta());
        cmbTipoCuenta.setValue(datosCuentaActual.getTipoCuenta());

        btnEditarCuenta.setOnAction(event -> {
            //Capturar los datos ingresados del usuario
            String codigo = txtCodigo.getText().trim().replace(" ", "").toUpperCase();
            String cuenta = txtCuenta.getText().trim().replace("  ", " ").toUpperCase();
            String tipoCuenta = cmbTipoCuenta.getSelectionModel().getSelectedItem().toString();

            //Validar si hay campos vacíos o datos ya existentes en la base de datos
            CatalogoCuentaModel catalogoCuenta = new CatalogoCuentaModel();
            if(!codigo.isEmpty()){
                if(!cuenta.isEmpty()){
                    if(!tipoCuenta.isEmpty()){
                        if(!catalogoCuenta.codigoExiste(codigo, idEmpresaSesion) || codigo.equals(datosCuentaActual.getCodigoCuenta())) {
                            if (!catalogoCuenta.cuentaExiste(cuenta, idEmpresaSesion) || cuenta.equals(datosCuentaActual.getCuenta())) {

                                //Confirmación de actualización de los datos de la cuenta
                                Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION);
                                alertConfirm.setTitle("Confirmacion");
                                alertConfirm.setHeaderText(null);
                                alertConfirm.setContentText("¿Está seguro de actualizar esta cuenta?");
                                Optional<ButtonType> result = alertConfirm.showAndWait();

                                if (result.get() == ButtonType.OK) {
                                    //Actualizar los datos de la cuenta
                                    CatalogoCuentaModel catalogoCuentaModel = new CatalogoCuentaModel();
                                    catalogoCuentaModel.setCuenta(cuenta);
                                    catalogoCuentaModel.setCodigoCuenta(codigo);
                                    catalogoCuentaModel.setTipoCuenta(tipoCuenta);

                                    catalogoCuentaModel.editarCuenta(datosCuentaActual.getIdCuenta(), catalogoCuentaModel);

                                    //Mensaje de confirmación
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Cuenta Editada");
                                    alert.setHeaderText(null);
                                    alert.setContentText("La cuenta se ha editado exitosamente.");
                                    alert.show();

                                    Stage stage = (Stage) btnEditarCuenta.getScene().getWindow();
                                    stage.close();
                                }
                            }
                            else {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error");
                                alert.setHeaderText(null);
                                alert.setContentText("El nombre de la cuenta ingresada ya existe ingrese otro nombre cuenta.");
                                alert.show();
                            }
                        }
                        else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText("El codigo ingresado ya existe ingrese otro codigo.");
                            alert.show();
                        }
                    }
                    else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("No se ha seleccionado el tipo de cuenta.");
                        alert.show();
                    }
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("El campo de cuenta no puede ser vacio.");
                    alert.show();
                }

            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("El campo de codigo no puede ser vacio.");
                alert.show();
            }
        });

        btnCancelar.setOnAction(event -> {
            Stage stage = (Stage) btnCancelar.getScene().getWindow();
            stage.close();
        });
    }

}
