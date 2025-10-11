package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.util.DialogoUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CatalogoController {

    @FXML
    private Button btnAgregarCuenta;

    @FXML
    private TableColumn<?, ?> clCodigo;

    @FXML
    private TableColumn<?, ?> clCuenta;

    @FXML
    private TableColumn<?, ?> clEditar;

    @FXML
    private TableColumn<?, ?> clEliminar;

    @FXML
    private TableColumn<?, ?> clTipoCuenta;

    @FXML
    private TableColumn<?, ?> clTipoSaldo;

    @FXML
    private ComboBox<?> cmbTipo;

    @FXML
    private TableView<?> tbCuentas;

    @FXML
    private TextField txtBuscarCuenta;

    @FXML
    public void initialize() {
        // Ajustar los anchos en porcentaje
        clCodigo.prefWidthProperty().bind(tbCuentas.widthProperty().multiply(0.12));
        clCuenta.prefWidthProperty().bind(tbCuentas .widthProperty().multiply(0.25));
        clTipoCuenta.prefWidthProperty().bind(tbCuentas .widthProperty().multiply(0.20));
        clTipoSaldo.prefWidthProperty().bind(tbCuentas.widthProperty().multiply(0.18));
        clEditar.prefWidthProperty().bind(tbCuentas.widthProperty().multiply(0.125));
        clEliminar.prefWidthProperty().bind(tbCuentas.widthProperty().multiply(0.125));

    }

    @FXML
    private void agregarCuenta(ActionEvent event) {
        Stage stage = (Stage) tbCuentas.getScene().getWindow();
        DialogoUtil.showDialog("agregar-cuenta", "Agregar Cuenta", stage);
    }

}
