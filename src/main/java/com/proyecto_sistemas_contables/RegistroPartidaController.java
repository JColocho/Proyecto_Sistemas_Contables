package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.CatalogoCuentaModel;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

public class RegistroPartidaController {
    @FXML
    private Button btnAgregarDoc;

    @FXML
    private Button btnEliminarPartida;

    @FXML
    private Button btnGuardarDetalle;

    @FXML
    private Button btnLimpiar;

    @FXML
    private Button btnRegistrarPartida;

    @FXML
    private ComboBox<String> cmbCuentas;

    @FXML
    private ComboBox<String> cmbTipoDoc;

    @FXML
    private TableColumn<?, ?> colAbono;

    @FXML
    private TableColumn<?, ?> colCargo;

    @FXML
    private TableColumn<?, ?> colCuenta;

    @FXML
    private TableColumn<?, ?> colEliminar;

    @FXML
    private DatePicker datePartida;

    @FXML
    private RadioButton rdbAbono;

    @FXML
    private RadioButton rdbCargo;

    @FXML
    private TableView<?> tblRegistroDetalle;

    @FXML
    private Text textDiferencia;

    @FXML
    private Text textTotalAbonos;

    @FXML
    private Text textTotalCargos;

    @FXML
    private TextArea txtConcepto;

    @FXML
    private TextField txtMonto;

    @FXML
    private TextField txtNumeroDoc;

    public void initialize() {
        ToggleGroup movimiento = new ToggleGroup();
        rdbCargo.setToggleGroup(movimiento);
        rdbAbono.setToggleGroup(movimiento);

        cargarCuentas();
        cargarTipoDoc();

        cmbCuentas.setOnAction(event -> {
            CatalogoCuentaModel cuentaModel = new CatalogoCuentaModel();
            System.out.println(cmbCuentas.getSelectionModel().getSelectedItem());
            System.out.println(cuentaModel.obtenerIdCuenta(cmbCuentas.getSelectionModel().getSelectedItem(), 1));
        });

    }
    private void cargarCuentas() {
        CatalogoCuentaModel cuentaModel = new CatalogoCuentaModel();
        ObservableList<String> cuentas = cuentaModel.obtenerNombreCuentas(1);

        cmbCuentas.setItems(cuentas);

        // --- Autocompletado dinámico ---
        cmbCuentas.getEditor().addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            String texto = cmbCuentas.getEditor().getText().toLowerCase();

            // Si el texto está vacío, restauramos todos los elementos
            if (texto.isEmpty()) {
                cmbCuentas.setItems(cuentas);
                return;
            }

            // Filtramos las coincidencias
            ObservableList<String> filtradas = cuentas.filtered(item ->
                    item.toLowerCase().contains(texto)
            );

            cmbCuentas.setItems(filtradas);
            cmbCuentas.show(); // Mostrar sugerencias
        });

        // --- Permitir borrar y volver a escribir ---
        cmbCuentas.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                cmbCuentas.setItems(cuentas);
            }
        });

    }
    private void cargarTipoDoc() {
        CatalogoCuentaModel cuentaModel = new CatalogoCuentaModel();

        cmbCuentas.setItems(cuentaModel.obtenerNombreCuentas(1));
    }
}
