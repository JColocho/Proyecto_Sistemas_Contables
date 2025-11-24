package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.CatalogoCuentaModel;
import com.proyecto_sistemas_contables.models.DetallePartidaModel;
import eu.hansolo.tilesfx.tools.DoubleExponentialSmoothingForLinearSeries;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.DecimalFormat;

public class CuentasTController {

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnImprimir;

    @FXML
    private TableColumn<DetallePartidaModel, Double> clActivosDebe;

    @FXML
    private TableColumn<DetallePartidaModel, Double> clActivosHaber;

    @FXML
    private TableColumn<DetallePartidaModel, Double> clCapitalDebe;

    @FXML
    private TableColumn<DetallePartidaModel, Double> clCapitalHaber;

    @FXML
    private TableColumn<DetallePartidaModel, Double> clCostosDebe;

    @FXML
    private TableColumn<DetallePartidaModel, Double> clCostosHaber;

    @FXML
    private TableColumn<DetallePartidaModel, Double> clGastosDebe;

    @FXML
    private TableColumn<DetallePartidaModel, Double> clGastosHaber;

    @FXML
    private TableColumn<DetallePartidaModel, Double> clIngresosDebe;

    @FXML
    private TableColumn<DetallePartidaModel, Double> clIngresosHaber;

    @FXML
    private TableColumn<DetallePartidaModel, Double> clPasivosDebe;

    @FXML
    private TableColumn<DetallePartidaModel, Double> clPasivosHaber;

    @FXML
    private TableColumn<DetallePartidaModel, Double> clRetirosDebe;

    @FXML
    private TableColumn<DetallePartidaModel, Double> clRetirosHaber;

    @FXML
    private ComboBox<CatalogoCuentaModel> cmbCuentasActivos;

    @FXML
    private ComboBox<CatalogoCuentaModel> cmbCuentasCapital;

    @FXML
    private ComboBox<CatalogoCuentaModel> cmbCuentasCostos;

    @FXML
    private ComboBox<CatalogoCuentaModel> cmbCuentasGastos;

    @FXML
    private ComboBox<CatalogoCuentaModel> cmbCuentasIngresos;

    @FXML
    private ComboBox<CatalogoCuentaModel> cmbCuentasPasivos;

    @FXML
    private ComboBox<CatalogoCuentaModel> cmbCuentasRetiros;

    @FXML
    private Label lblSaldoActivos;

    @FXML
    private Label lblSaldoCapital;

    @FXML
    private Label lblSaldoCostos;

    @FXML
    private Label lblSaldoGastos;

    @FXML
    private Label lblSaldoIngresos;

    @FXML
    private Label lblSaldoPasivos;

    @FXML
    private Label lblSaldoRetiros;

    @FXML
    private Label lblTotalDebeActivos;

    @FXML
    private Label lblTotalDebeCapital;

    @FXML
    private Label lblTotalDebeCostos;

    @FXML
    private Label lblTotalDebeGastos;

    @FXML
    private Label lblTotalDebeIngresos;

    @FXML
    private Label lblTotalDebePasivos;

    @FXML
    private Label lblTotalDebeRetiros;

    @FXML
    private Label lblTotalHaberActivos;

    @FXML
    private Label lblTotalHaberCapital;

    @FXML
    private Label lblTotalHaberCostos;

    @FXML
    private Label lblTotalHaberGastos;

    @FXML
    private Label lblTotalHaberIngresos;

    @FXML
    private Label lblTotalHaberPasivos;

    @FXML
    private Label lblTotalHaberRetiros;

    @FXML
    private TableView<DetallePartidaModel> tbActivos;

    @FXML
    private TableView<DetallePartidaModel> tbCapital;

    @FXML
    private TableView<DetallePartidaModel> tbCostos;

    @FXML
    private TableView<DetallePartidaModel> tbGastos;

    @FXML
    private TableView<DetallePartidaModel> tbIngresos;

    @FXML
    private TableView<DetallePartidaModel> tbPasivos;

    @FXML
    private TableView<DetallePartidaModel> tbRetiros;

    public static int idEmpresaSesion;

    public void initialize() {
        CatalogoCuentaModel catalogoCuentaModel = new CatalogoCuentaModel();
        cmbCuentasActivos.setItems(catalogoCuentaModel.obtenerCatalogoCuentasPorTipo("ACTIVO", idEmpresaSesion));
        cmbCuentasPasivos.setItems(catalogoCuentaModel.obtenerCatalogoCuentasPorTipo("PASIVO", idEmpresaSesion));
        cmbCuentasCapital.setItems(catalogoCuentaModel.obtenerCatalogoCuentasPorTipo("CAPITAL", idEmpresaSesion));
        cmbCuentasGastos.setItems(catalogoCuentaModel.obtenerCatalogoCuentasPorTipo("GASTOS", idEmpresaSesion));
        cmbCuentasCostos.setItems(catalogoCuentaModel.obtenerCatalogoCuentasPorTipo("COSTOS", idEmpresaSesion));
        cmbCuentasIngresos.setItems(catalogoCuentaModel.obtenerCatalogoCuentasPorTipo("INGRESOS", idEmpresaSesion));
        cmbCuentasRetiros.setItems(catalogoCuentaModel.obtenerCatalogoCuentasPorTipo("RETIROS", idEmpresaSesion));

        clActivosDebe.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        clActivosHaber.setCellValueFactory(new PropertyValueFactory<>("abono"));
        clPasivosDebe.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        clPasivosHaber.setCellValueFactory(new PropertyValueFactory<>("abono"));
        clCapitalDebe.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        clCapitalHaber.setCellValueFactory(new PropertyValueFactory<>("abono"));
        clGastosDebe.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        clGastosHaber.setCellValueFactory(new PropertyValueFactory<>("abono"));
        clCostosDebe.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        clCostosHaber.setCellValueFactory(new PropertyValueFactory<>("abono"));
        clIngresosDebe.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        clIngresosHaber.setCellValueFactory(new PropertyValueFactory<>("abono"));
        clRetirosDebe.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        clRetirosHaber.setCellValueFactory(new PropertyValueFactory<>("abono"));

        //Dar formato a las columnas
        formatearColumnaCargoAbono(clActivosDebe, clActivosHaber);
        formatearColumnaCargoAbono(clCapitalDebe, clCapitalHaber);
        formatearColumnaCargoAbono(clPasivosDebe, clPasivosHaber);
        formatearColumnaCargoAbono(clGastosDebe, clGastosHaber);
        formatearColumnaCargoAbono(clCostosDebe, clCostosHaber);
        formatearColumnaCargoAbono(clIngresosDebe, clIngresosHaber);
        formatearColumnaCargoAbono(clRetirosDebe, clRetirosHaber);

        cmbCuentasActivos.setOnAction(event -> {
            DetallePartidaModel detallePartidaModel = new DetallePartidaModel();
            tbActivos.setItems(detallePartidaModel.obtenerDetallePorCuenta(
                    cmbCuentasActivos.getSelectionModel().getSelectedItem().getIdCuenta(),
                    idEmpresaSesion
            ));
            calcularSaldos(tbActivos,lblTotalDebeActivos,lblTotalHaberActivos,lblSaldoActivos);
        });
        cmbCuentasPasivos.setOnAction(event -> {
            DetallePartidaModel detallePartidaModel = new DetallePartidaModel();
            tbPasivos.setItems(detallePartidaModel.obtenerDetallePorCuenta(
                    cmbCuentasPasivos.getSelectionModel().getSelectedItem().getIdCuenta(),
                    idEmpresaSesion
            ));
            calcularSaldos(tbPasivos,lblTotalDebePasivos,lblTotalHaberPasivos,lblSaldoPasivos);
        });
        cmbCuentasCapital.setOnAction(event -> {
            DetallePartidaModel detallePartidaModel = new DetallePartidaModel();
            tbCapital.setItems(detallePartidaModel.obtenerDetallePorCuenta(
                    cmbCuentasCapital.getSelectionModel().getSelectedItem().getIdCuenta(),
                    idEmpresaSesion
            ));
            calcularSaldos(tbCapital, lblTotalDebeCapital,lblTotalHaberCapital,lblSaldoCapital);
        });
        cmbCuentasGastos.setOnAction(event -> {
            DetallePartidaModel detallePartidaModel = new DetallePartidaModel();
            tbGastos.setItems(detallePartidaModel.obtenerDetallePorCuenta(
                    cmbCuentasGastos.getSelectionModel().getSelectedItem().getIdCuenta(),
                    idEmpresaSesion
            ));
            calcularSaldos(tbGastos, lblTotalDebeGastos,lblTotalHaberGastos,lblSaldoGastos);
        });
        cmbCuentasCostos.setOnAction(event -> {
            DetallePartidaModel detallePartidaModel = new DetallePartidaModel();
            tbCostos.setItems(detallePartidaModel.obtenerDetallePorCuenta(
                    cmbCuentasCostos.getSelectionModel().getSelectedItem().getIdCuenta(),
                    idEmpresaSesion
            ));
            calcularSaldos(tbCostos,lblTotalDebeCostos,lblTotalHaberCostos,lblSaldoCostos);
        });
        cmbCuentasIngresos.setOnAction(event -> {
            DetallePartidaModel detallePartidaModel = new DetallePartidaModel();
            tbIngresos.setItems(detallePartidaModel.obtenerDetallePorCuenta(
                    cmbCuentasIngresos.getSelectionModel().getSelectedItem().getIdCuenta(),
                    idEmpresaSesion
            ));
            calcularSaldos(tbIngresos, lblTotalDebeIngresos,lblTotalHaberIngresos,lblSaldoIngresos);
        });
        cmbCuentasRetiros.setOnAction(event -> {
            DetallePartidaModel detallePartidaModel = new DetallePartidaModel();
            tbRetiros.setItems(detallePartidaModel.obtenerDetallePorCuenta(
                    cmbCuentasRetiros.getSelectionModel().getSelectedItem().getIdCuenta(),
                    idEmpresaSesion
            ));
            calcularSaldos(tbRetiros, lblTotalDebeRetiros,lblTotalHaberRetiros,lblSaldoRetiros);
        });
    }

    private void formatearColumnaCargoAbono(TableColumn<DetallePartidaModel, Double> clDebe, TableColumn<DetallePartidaModel, Double> clHaber) {
        clDebe.setCellFactory(tc -> new TableCell<DetallePartidaModel, Double>() {
            private final DecimalFormat formato = new DecimalFormat("$#,##0.00");

            @Override
            protected void updateItem(Double saldo, boolean empty) {
                super.updateItem(saldo, empty);
                setAlignment(Pos.CENTER_RIGHT);

                if (empty || saldo == null) {
                    setText(null);
                } else {
                    setText(formato.format(saldo));
                }
            }
        });
        clHaber.setCellFactory(tc -> new TableCell<DetallePartidaModel, Double>() {
            private final DecimalFormat formato = new DecimalFormat("$#,##0.00");

            @Override
            protected void updateItem(Double saldo, boolean empty) {
                super.updateItem(saldo, empty);
                setAlignment(Pos.CENTER_RIGHT);

                if (empty || saldo == null) {
                    setText(null);
                } else {
                    setText(formato.format(saldo));
                }
            }
        });
    }

    private void calcularSaldos(TableView<DetallePartidaModel> tbCuenta, Label lblDebe, Label lblHabe, Label lblSaldo) {
        double debe = 0;
        double haber = 0;
        double total = 0;
        for (DetallePartidaModel saldos: tbCuenta.getItems()){
            debe += saldos.getCargo();
            haber += saldos.getAbono();
        }
        lblDebe.setText("$"+  String.format("%.2f", debe));
        lblHabe.setText("$"+  String.format("%.2f", haber));
        total = debe - haber;

        if (total < 0){
            lblSaldo.setText("$"+  String.format("%.2f", Math.abs(total)) + " ACREEDOR");
        }
        else if (total > 0){
            lblSaldo.setText("$"+  String.format("%.2f", Math.abs(total)) + " DEUDOR");
        }
        else {
            lblSaldo.setText("$"+  String.format("%.2f", Math.abs(total)));
        }
    }
}

