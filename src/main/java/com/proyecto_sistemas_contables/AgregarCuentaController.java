package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.CatalogoCuentaModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AgregarCuentaController {

    @FXML
    private Button btnAgregarCuenta;

    @FXML
    private Button btnCancelar;

    @FXML
    private ComboBox<String> cmbTipoCuenta;

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtCuenta;

    public static int idEmpresaSesion;

    public void initialize() {
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


        btnAgregarCuenta.setOnAction(event -> {
            String codigo = txtCodigo.getText().trim().replace(" ", "").toUpperCase();
            String cuenta = txtCuenta.getText().trim().replace("  ", " ").toUpperCase();
            String tipoCuenta = cmbTipoCuenta.getSelectionModel().getSelectedItem().toString();

            CatalogoCuentaModel catalogoCuenta = new CatalogoCuentaModel();
            if(!codigo.isEmpty()){
                if(!cuenta.isEmpty()){
                    if(!tipoCuenta.isEmpty()){
                        if(!catalogoCuenta.codigoExiste(codigo, idEmpresaSesion)) {
                            if (!catalogoCuenta.cuentaExiste(cuenta, idEmpresaSesion)) {
                                CatalogoCuentaModel catalogoCuentaModel = new CatalogoCuentaModel();
                                catalogoCuentaModel.setCuenta(cuenta);
                                catalogoCuentaModel.setCodigoCuenta(codigo);
                                catalogoCuentaModel.setTipoCuenta(tipoCuenta);
                                catalogoCuentaModel.setIdEmpresa(idEmpresaSesion);

                                catalogoCuentaModel.crearCuenta(catalogoCuentaModel);

                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Cuenta Creada");
                                alert.setHeaderText(null);
                                alert.setContentText("Cuenta creada con exito.");

                                Stage stage = (Stage) btnAgregarCuenta.getScene().getWindow();
                                stage.close();
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
