package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import com.proyecto_sistemas_contables.models.CatalogoCuentaModel;
import com.proyecto_sistemas_contables.models.EmpresaModel;
import com.proyecto_sistemas_contables.models.PartidaModel;
import com.proyecto_sistemas_contables.util.DialogoUtil;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;

import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class DashboardController {

    @FXML private Label lblIngresosTotales;
    @FXML private Label lblGastosTotales;
    @FXML private Label lblUtilidadPerdida;
    @FXML private Label lblTextoUtilidadPerdida;

    @FXML private TableView<PartidaModel> tblTransacciones;
    @FXML private TableColumn<PartidaModel, Date> clFecha;
    @FXML private TableColumn<PartidaModel, String> clConcepto;
    @FXML private TableColumn<PartidaModel, String> clUsuario;
    @FXML private TableColumn<PartidaModel, Void> clVerDetalles;
    @FXML private TableColumn<PartidaModel, Void> clDocumento;

    @FXML private Label lblTotalActivos;
    @FXML private Label lblTotalPasivos;
    @FXML private Label lblTotalCapital;

    @FXML private TabPane tabPaneBalance;

    @FXML private TableView<CatalogoCuentaModel> tblActivos;
    @FXML private TableColumn<CatalogoCuentaModel, String> clActivoCuenta;
    @FXML private TableColumn<CatalogoCuentaModel, String> clActivoTipo;
    @FXML private TableColumn<CatalogoCuentaModel, String> clActivoSaldo;

    @FXML private TableView<CatalogoCuentaModel> tblPasivos;
    @FXML private TableColumn<CatalogoCuentaModel, String> clPasivoCuenta;
    @FXML private TableColumn<CatalogoCuentaModel, String> clPasivoTipo;
    @FXML private TableColumn<CatalogoCuentaModel, String> clPasivoSaldo;

    @FXML private TableView<CatalogoCuentaModel> tblCapital;
    @FXML private TableColumn<CatalogoCuentaModel, String> clCapitalCuenta;
    @FXML private TableColumn<CatalogoCuentaModel, String> clCapitalTipo;
    @FXML private TableColumn<CatalogoCuentaModel, String> clCapitalSaldo;

    private EmpresaModel empresaSeleccionada;
    private NumberFormat formatoMoneda;
    public static int idEmpresaSesion;

    public void initialize() {
        // Configurar formato de moneda
        formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "US"));

        // Asignar el ID de la empresa de la sesión
        idEmpresaSesion = NavbarController.idEmpresaSesion;

        configurarTablasBalance();      // configura los campos de Activo, Pasivos y Capital
        configurarTablaPartidas();      // configura los campos para las ultimas partidas realizadas
        cargarEmpresaDesdeSesion();     // Cargar empresa desde la sesión y datos
    }

    private void configurarTablaPartidas(){
        try {
            // Ajustar los anchos en porcentaje
            clFecha.prefWidthProperty().bind(tblTransacciones.widthProperty().multiply(0.15));
            clConcepto.prefWidthProperty().bind(tblTransacciones.widthProperty().multiply(0.40));
            clUsuario.prefWidthProperty().bind(tblTransacciones.widthProperty().multiply(0.12));
            clVerDetalles.prefWidthProperty().bind(tblTransacciones.widthProperty().multiply(0.165));
            clDocumento.prefWidthProperty().bind(tblTransacciones.widthProperty().multiply(0.165));

            // Configurar las columnas
            clFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
            clConcepto.setCellValueFactory(new PropertyValueFactory<>("concepto"));
            clUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));

            // Configurar columna de Ver Detalles
            clVerDetalles.setCellFactory(param -> new TableCell<>() {
                private final Button btnVer = new Button();
                private final HBox pane = new HBox();

                {
                    ImageView iconVer = new ImageView(new Image(getClass().getResourceAsStream("/com/proyecto_sistemas_contables/static/img/buscar.png")));
                    iconVer.setFitWidth(16);
                    iconVer.setFitHeight(16);

                    btnVer.setGraphic(iconVer);
                    btnVer.setStyle("-fx-background-color: rgb(210, 240, 240); -fx-text-fill: white; -fx-cursor: hand;");

                    btnVer.setOnAction(event -> {
                        PartidaModel partida = getTableView().getItems().get(getIndex());
                        DetallePartidaController.idPartida = partida.getIdPartida();
                        DetallePartidaController.idEmpresaSesion = idEmpresaSesion;
                        Stage stage = (Stage) tblTransacciones.getScene().getWindow();
                        DialogoUtil.showDialog("detalle-partida-view", "Detalle", stage);
                    });

                    pane.setAlignment(Pos.CENTER);
                    pane.getChildren().add(btnVer);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : pane);
                }
            });

            // Configurar columna de Ver Documento
            clDocumento.setCellFactory(param -> new TableCell<>() {
                private final Button btnDocumento = new Button();
                private final HBox pane = new HBox();

                {
                    ImageView iconDoc = new ImageView(new Image(getClass().getResourceAsStream("/com/proyecto_sistemas_contables/static/img/documentos.png")));
                    iconDoc.setFitWidth(16);
                    iconDoc.setFitHeight(16);

                    btnDocumento.setGraphic(iconDoc);
                    btnDocumento.setStyle("-fx-background-color: rgb(52, 152, 219); -fx-text-fill: white; -fx-cursor: hand;");

                    btnDocumento.setOnAction(event -> {
                        PartidaModel partida = getTableView().getItems().get(getIndex());
                        abrirDocumento(partida.getIdPartida());
                    });

                    pane.setAlignment(Pos.CENTER);
                    pane.getChildren().add(btnDocumento);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : pane);
                }
            });

            // Cargar las últimas 10 partidas
            cargarUltimasPartidas();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void cargarUltimasPartidas() {
        ObservableList<PartidaModel> partidas = PartidaModel.obtenerUltimasPartidas(10, idEmpresaSesion);
        tblTransacciones.setItems(partidas);
    }

    private void abrirDocumento(int idPartida) {
        try {
            PartidaModel partidaModel = new PartidaModel();
            PartidaModel partida = partidaModel.obtenerPartida(idPartida);

            EmpresaModel empresaModel = new EmpresaModel();
            String documentoRuta = "src/main/resources/com/proyecto_sistemas_contables/documentos_partidas/"
                    + empresaModel.idBuscarEmpresa(idEmpresaSesion) + "/"
                    + partida.getNumeroDocumento() + ".pdf";

            File archivo = new File(documentoRuta);

            if (archivo.exists()) {
                Desktop.getDesktop().open(archivo);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Documento no encontrado");
                alert.setHeaderText("No se encontró el documento");
                alert.setContentText("El archivo PDF no existe en la ruta especificada.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error al abrir el documento");
            alert.setContentText("No se pudo abrir el documento: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    /**
     * Configurar las columnas de las tablas del balance
     */
    private void configurarTablasBalance() {
        // Configurar tabla de ACTIVOS
        clActivoCuenta.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCuenta()));
        clActivoTipo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTipoSaldo()));
        clActivoSaldo.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatoMoneda.format(cellData.getValue().getSaldo())));

        // Configurar tabla de PASIVOS
        clPasivoCuenta.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCuenta()));
        clPasivoTipo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTipoSaldo()));
        clPasivoSaldo.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatoMoneda.format(cellData.getValue().getSaldo())));

        // Configurar tabla de CAPITAL
        clCapitalCuenta.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCuenta()));
        clCapitalTipo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTipoSaldo()));
        clCapitalSaldo.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatoMoneda.format(cellData.getValue().getSaldo())));
    }

    /**
     * Cargar datos del Balance General (Activos, Pasivos, Capital)
     */
    private void cargarBalanceGeneral() {
        try {
            CatalogoCuentaModel catalogoModel = new CatalogoCuentaModel();

            // CARGAR ACTIVOS (corrientes y no corrientes)
            ObservableList<CatalogoCuentaModel> activos =
                    catalogoModel.obtenerCuentasPorTipoSimilitud("ACTIVO", empresaSeleccionada.getId());
            tblActivos.setItems(activos);

            double totalActivos = activos.stream()
                    .mapToDouble(CatalogoCuentaModel::getSaldo)
                    .sum();
            lblTotalActivos.setText(formatoMoneda.format(totalActivos));

            // CARGAR PASIVOS (corrientes y no corrientes)
            ObservableList<CatalogoCuentaModel> pasivos =
                    catalogoModel.obtenerCuentasPorTipoSimilitud("PASIVO", empresaSeleccionada.getId());
            tblPasivos.setItems(pasivos);

            double totalPasivos = pasivos.stream()
                    .mapToDouble(CatalogoCuentaModel::getSaldo)
                    .sum();
            lblTotalPasivos.setText(formatoMoneda.format(totalPasivos));

            // CARGAR CAPITAL
            ObservableList<CatalogoCuentaModel> capital =
                    catalogoModel.obtenerCatalogoCuentasPorTipo("CAPITAL", empresaSeleccionada.getId());
            tblCapital.setItems(capital);

            double totalCapital = capital.stream()
                    .mapToDouble(CatalogoCuentaModel::getSaldo)
                    .sum();
            lblTotalCapital.setText(formatoMoneda.format(totalCapital));

        } catch (Exception e) {
            System.err.println("Error al cargar balance general: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cargar empresa desde la sesión del navbar
     */
    private void cargarEmpresaDesdeSesion() {
        if (NavbarController.idEmpresaSesion > 0) {
            try {
                // Buscar la empresa en la base de datos
                Connection conn = ConexionDB.connection();
                String sql = """
                    SELECT e.idempresa, e.nombre, e.nit, e.nrc, e.direccion, 
                           e.telefono, e.idcorreo, c.correo
                    FROM tblempresas e
                    LEFT JOIN tblcorreos c ON e.idcorreo = c.idcorreo
                    WHERE e.idempresa = ?
                    """;
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, NavbarController.idEmpresaSesion);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    empresaSeleccionada = new EmpresaModel(
                            rs.getInt("idempresa"),
                            rs.getString("nombre"),
                            rs.getString("nit"),
                            rs.getString("nrc"),
                            rs.getString("direccion"),
                            rs.getString("telefono"),
                            rs.getInt("idcorreo"),
                            rs.getString("correo")
                    );

                    // Cargar datos del dashboard
                    cargarDatosDashboard();
                }

            } catch (Exception e) {
                System.err.println("Error al cargar empresa desde sesión: " + e.getMessage());
                e.printStackTrace();
                mostrarDatosVacios();
            }
        } else {
            System.err.println("No hay empresa en sesión");
            mostrarDatosVacios();
        }
    }

    /**
     * Método para recibir la empresa seleccionada desde otro controlador
     */
    public void setEmpresa(EmpresaModel empresa) {
        this.empresaSeleccionada = empresa;
        cargarDatosDashboard();
    }

    /**
     * Método público para recargar datos
     */
    public void cargarDatos() {
        cargarEmpresaDesdeSesion();
    }

    /**
     * Método principal para cargar todos los datos del dashboard
     */
    private void cargarDatosDashboard() {
        if (empresaSeleccionada == null) {
            System.err.println("No hay empresa seleccionada");
            mostrarDatosVacios();
            return;
        }

        try {
            // Calcular ingresos y gastos
            double ingresosTotales = calcularIngresos();
            double gastosTotales = calcularGastos();
            double utilidadPerdida = ingresosTotales - gastosTotales;

            // Actualizar labels con formato de moneda
            lblIngresosTotales.setText(formatoMoneda.format(ingresosTotales));
            lblGastosTotales.setText(formatoMoneda.format(gastosTotales));
            lblUtilidadPerdida.setText(formatoMoneda.format(Math.abs(utilidadPerdida)));
            cargarBalanceGeneral();

            // Determinar si es utilidad o pérdida
            if (utilidadPerdida >= 0) {
                lblTextoUtilidadPerdida.setText("Utilidad Neta");
                lblUtilidadPerdida.setStyle("-fx-text-fill: #009951;");
            } else {
                lblTextoUtilidadPerdida.setText("Pérdida Neta");
                lblUtilidadPerdida.setStyle("-fx-text-fill: #C00F0C;");
            }

        } catch (Exception e) {
            System.err.println("Error al cargar datos del dashboard: " + e.getMessage());
            e.printStackTrace();
            mostrarDatosVacios();
        }
    }

    /**
     * Mostrar valores en cero cuando no hay datos
     */
    private void mostrarDatosVacios() {
        lblIngresosTotales.setText("$0.00");
        lblGastosTotales.setText("$0.00");
        lblUtilidadPerdida.setText("$0.00");
        lblTextoUtilidadPerdida.setText("Utilidad o pérdida");

        lblTotalActivos.setText("$0.00");
        lblTotalPasivos.setText("$0.00");
        lblTotalCapital.setText("$0.00");
    }

    /**
     * Calcular total de ingresos
     */
    private double calcularIngresos() {
        double totalIngresos = 0.0;

        try {
            CatalogoCuentaModel catalogoModel = new CatalogoCuentaModel();
            ObservableList<CatalogoCuentaModel> cuentasIngreso =
                    catalogoModel.obtenerCatalogoCuentasPorTipo("INGRESOS O VENTAS", empresaSeleccionada.getId());

            for (CatalogoCuentaModel cuenta : cuentasIngreso) {
                totalIngresos += cuenta.getSaldo();
            }

        } catch (Exception e) {
            System.err.println("Error al calcular ingresos: " + e.getMessage());
            e.printStackTrace();
        }

        return totalIngresos;
    }

    /**
     * Calcular total de gastos
     */
    private double calcularGastos() {
        double totalGastos = 0.0;

        try {
            CatalogoCuentaModel catalogoModel = new CatalogoCuentaModel();
            ObservableList<CatalogoCuentaModel> cuentasGasto =
                    catalogoModel.obtenerCatalogoCuentasPorTipo("GASTOS", empresaSeleccionada.getId());


            for (CatalogoCuentaModel cuenta : cuentasGasto) {
                totalGastos += cuenta.getSaldo();
            }

        } catch (Exception e) {
            System.err.println("Error al calcular gastos: " + e.getMessage());
            e.printStackTrace();
        }

        return totalGastos;
    }

    /**
     * Método para actualizar dashboard manualmente
     */
    public void actualizarDashboard() {
        cargarDatosDashboard();
    }


}