package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.CatalogoCuentaModel;
import com.proyecto_sistemas_contables.util.DialogoUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.util.Optional;

public class CatalogoCuentasController {

    @FXML
    private TableColumn<CatalogoCuentaModel, String> clCodigo;

    @FXML
    private TableColumn<CatalogoCuentaModel, String> clCuenta;

    @FXML
    private TableColumn<CatalogoCuentaModel, Double> clSaldo;

    @FXML
    private TableColumn<CatalogoCuentaModel, String> clTipoCuenta;

    @FXML
    private TableColumn<CatalogoCuentaModel, String> clTipoSaldo;

    @FXML
    private TableColumn<CatalogoCuentaModel, Void> clAccion;

    @FXML
    private ComboBox<CatalogoCuentaModel> cmbTipoCuenta;

    @FXML
    private AnchorPane mainPane;

    @FXML
    private TableView<CatalogoCuentaModel> tbCuentas;

    @FXML
    private TextField txtBuscarCuenta;

    @FXML
    private Button btnAgregarCuenta;

    public static int idEmpresaSesion;

    public void initialize() {
        //Asignado el tamaño que tomará cada columna
        clCodigo.prefWidthProperty().bind(tbCuentas.widthProperty().multiply(0.15));
        clCuenta.prefWidthProperty().bind(tbCuentas.widthProperty().multiply(0.30));
        clTipoCuenta.prefWidthProperty().bind(tbCuentas.widthProperty().multiply(0.15));
        clTipoSaldo.prefWidthProperty().bind(tbCuentas.widthProperty().multiply(0.15));
        clSaldo.prefWidthProperty().bind(tbCuentas.widthProperty().multiply(0.15));
        clAccion.prefWidthProperty().bind(tbCuentas.widthProperty().multiply(0.10));

        //Cargando la tabla con los datos de las cuentas
        cargarCuentas();

        //Configuración de las columnas para llamar los datos
        clCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoCuenta"));
        clCuenta.setCellValueFactory(new PropertyValueFactory<>("cuenta"));
        clTipoCuenta.setCellValueFactory(new PropertyValueFactory<>("tipoCuenta"));
        clTipoSaldo.setCellValueFactory(new PropertyValueFactory<>("tipoSaldo"));
        clSaldo.setCellValueFactory(new PropertyValueFactory<>("saldo"));
        clAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button();
            private final Button btnEliminar = new Button();
            private final HBox pane = new HBox(5);

            {
                // Crear iconos
                ImageView iconEditar = new ImageView(new Image(getClass().getResourceAsStream("/com/proyecto_sistemas_contables/static/img/write.png")));
                iconEditar.setFitWidth(16);
                iconEditar.setFitHeight(16);

                ImageView iconEliminar = new ImageView(new Image(getClass().getResourceAsStream("/com/proyecto_sistemas_contables/static/img/bin.png")));
                iconEliminar.setFitWidth(16);
                iconEliminar.setFitHeight(16);

                // Asignar iconos a los botones
                btnEditar.setGraphic(iconEditar);
                btnEliminar.setGraphic(iconEliminar);

                btnEditar.setStyle("-fx-background-color: rgb(210, 240, 240); -fx-text-fill: white; -fx-cursor: hand;");
                btnEliminar.setStyle("-fx-background-color: rgb(243, 66, 53); -fx-text-fill: white; -fx-cursor: hand;");

                btnEditar.setOnAction(event -> {
                });

                btnEliminar.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Eliminar Cuenta");
                    alert.setHeaderText("¿Está seguro de eliminar esta cuenta?");
                    alert.setContentText("Esta acción no se puede revertir.");

                    Optional<ButtonType> resultado = alert.showAndWait();

                    if (resultado.get() == ButtonType.OK) {
                        CatalogoCuentaModel catalogoCuentaModel = getTableView().getItems().get(getIndex());
                        catalogoCuentaModel.eliminarCuenta(catalogoCuentaModel.getIdCuenta(), idEmpresaSesion);
                        cargarCuentas();
                    }

                });

                pane.setAlignment(Pos.CENTER);
                pane.getChildren().addAll(btnEditar, btnEliminar);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        formatearColumnaSaldo();

        btnAgregarCuenta.setOnAction(event -> {
            AgregarCuentaController.idEmpresaSesion = idEmpresaSesion;
            Stage stage = (Stage) tbCuentas.getScene().getWindow();
            DialogoUtil.showDialog("agregar-cuenta-view", "Agregar partida", stage);
        });


    }

    //método para cargar la tabla con todos los datos de las cuentas
    public void cargarCuentas() {
        tbCuentas.getItems().clear();
        CatalogoCuentaModel cuentaModel = new CatalogoCuentaModel();
        tbCuentas.setItems(cuentaModel.obtenerCatalogoCuentas(idEmpresaSesion));
    }
    private void formatearColumnaSaldo() {
        clSaldo.setCellFactory(tc -> new TableCell<CatalogoCuentaModel, Double>() {
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

}
