package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.CatalogoCuentaModel;
import com.proyecto_sistemas_contables.models.DetallePartidaModel;
import com.proyecto_sistemas_contables.models.PartidaModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

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
    private TableColumn<CatalogoCuentaModel, Double> colAbono;

    @FXML
    private TableColumn<CatalogoCuentaModel, Double> colCargo;

    @FXML
    private TableColumn<CatalogoCuentaModel, String> colCuenta;

    @FXML
    private TableColumn<CatalogoCuentaModel, Button> colEliminar;

    @FXML
    private DatePicker datePartida;

    @FXML
    private RadioButton rdbAbono;

    @FXML
    private RadioButton rdbCargo;

    @FXML
    private TableView<CatalogoCuentaModel> tblRegistroDetalle;

    @FXML
    private Text textDiferencia;

    @FXML
    private Text textTotalAbonos;

    @FXML
    private Text textTotalCargos;

    @FXML
    private Text txtDocSubido;

    @FXML
    private TextArea txtConcepto;

    @FXML
    private TextField txtMonto;

    @FXML
    private TextField txtNumeroDoc;

    private ToggleGroup movimiento;

    private ObservableList<CatalogoCuentaModel> registroDetalle;
    public static int idUsuarioSesion;

    // Guardará temporalmente el archivo PDF seleccionado
    private File archivoSeleccionado;

    // Ruta donde se guardarán los documentos (carpeta ya creada)
    private final String RUTA_DESTINO = "src/main/resources/com/proyecto_sistemas_contables/documentos_partidas";

    public void initialize() {

        ObservableList<String> tiposDocumento = FXCollections.observableArrayList(
                "Factura de consumidor final",
                "Factura de crédito fiscal",
                "Recibo",
                "Comprobante de egreso",
                "Comprobante de ingreso",
                "Comprobante de diario",
                "Nota de crédito",
                "Nota de débito",
                "Cheque",
                "Depósito bancario",
                "Transferencia bancaria",
                "Liquidación de viáticos / gastos",
                "Otros documentos"
        );

        cmbTipoDoc.setItems(tiposDocumento);


        registroDetalle = FXCollections.observableArrayList();

        movimiento = new ToggleGroup();
        this.rdbCargo.setToggleGroup(movimiento);
        this.rdbAbono.setToggleGroup(movimiento);

        cargarCuentas();
        cargarTipoDoc();

        colCuenta.setCellValueFactory(new PropertyValueFactory<>("cuenta"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colAbono.setCellValueFactory(new PropertyValueFactory<>("abono"));

        this.colEliminar.setCellFactory(tc -> new TableCell<>(){
            @Override
            protected void updateItem(Button button, boolean b) {
                super.updateItem(button, b);
                Button btnEliminar = new Button("Eliminar");
                if (b){
                    setGraphic(null);
                }
                else {
                    btnEliminar.setOnAction(e -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Eliminar");
                        alert.setContentText("¿Esta seguro de eliminar el registro?");
                        Optional<ButtonType> respuesta = alert.showAndWait();

                        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
                            // Obtener el elemento actual (la fila seleccionada)
                            registroDetalle.remove(getTableView().getItems().get(getIndex()));
                            cargarTabla();
                        }
                    });

                    setGraphic(btnEliminar);
                }
            }
        });

        btnGuardarDetalle.setOnAction(e -> {
            CatalogoCuentaModel cuentaModel = new CatalogoCuentaModel();

            if(cmbCuentas.getSelectionModel().getSelectedItem() != null){
                if(!txtMonto.getText().isEmpty()){
                    if(rdbCargo.isSelected()){
                        cuentaModel.setIdCuenta(
                                cuentaModel.obtenerIdCuenta(cmbCuentas.getSelectionModel().getSelectedItem(), 1));
                        cuentaModel.setCuenta(cmbCuentas.getSelectionModel().getSelectedItem());
                        cuentaModel.setCargo(Double.parseDouble(txtMonto.getText()));
                        cuentaModel.setAbono(0.00);
                        registroDetalle.add(cuentaModel);
                        cargarTabla();
                        limpiarRegistroDetalle();
                    } else if (rdbAbono.isSelected()) {
                        cuentaModel.setIdCuenta(
                                cuentaModel.obtenerIdCuenta(cmbCuentas.getSelectionModel().getSelectedItem(), 1));
                        cuentaModel.setCuenta(cmbCuentas.getSelectionModel().getSelectedItem());
                        cuentaModel.setAbono(Double.parseDouble(txtMonto.getText()));
                        cuentaModel.setCargo(0.00);
                        registroDetalle.add(cuentaModel);
                        cargarTabla();
                        limpiarRegistroDetalle();
                    }
                }
            }
        });

        btnLimpiar.setOnAction(e -> {
            limpiarRegistroDetalle();
        });

        btnRegistrarPartida.setOnAction(e -> {
           if(datePartida.getValue() != null){
               if(!txtConcepto.getText().isEmpty()){
                   if (cmbTipoDoc.getSelectionModel().getSelectedItem() != null){
                       if (!txtNumeroDoc.getText().isEmpty()){
                           if(archivoSeleccionado.exists()){
                               if (guardarCopiaPDF(txtNumeroDoc.getText())){
                                   if (!tblRegistroDetalle.getItems().isEmpty()){

                                       //Capturamos los datos para subir los datos de la partida
                                       PartidaModel partidaModel = new PartidaModel();
                                       partidaModel.setConcepto(txtConcepto.getText().trim().replace("   ", " ").replace("  ", " ").toUpperCase());
                                       partidaModel.setFecha(java.sql.Date.valueOf(datePartida.getValue()));
                                       partidaModel.setIdUsuario(idUsuarioSesion);
                                       partidaModel.setIdEmpresa(1);
                                       partidaModel.setTipoDocumento(cmbTipoDoc.getSelectionModel().getSelectedItem());
                                       partidaModel.setNumeroDocumento(txtNumeroDoc.getText());

                                       //Insertar la partida a la DB y obtener el id de la partida
                                       partidaModel.setIdPartida(partidaModel.agregarPartida(partidaModel));

                                       //Registramos los detalles de la partida
                                       for(CatalogoCuentaModel cuentaModel : registroDetalle){
                                           DetallePartidaModel detallePartidaModel = new DetallePartidaModel();
                                           detallePartidaModel.agregarDetalle(
                                                   partidaModel.getIdPartida(),
                                                   cuentaModel.obtenerIdCuenta(cuentaModel.getCuenta(),1),
                                                   cuentaModel.getCargo(),
                                                   cuentaModel.getAbono());
                                       }

                                       limpiarRegistro();

                                       Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                       alert.setTitle("Registro Partida");
                                       alert.setContentText("Partida registrada con exito.");
                                       alert.show();
                                   }
                               }
                           }
                       }
                   }
               }
           }
        });

        btnEliminarPartida.setOnAction(e -> {
            limpiarRegistro();
        });

    }

    public void cargarTabla(){
        tblRegistroDetalle.setItems(registroDetalle);
    }
    public void limpiarRegistroDetalle() {
        txtMonto.setText("");
        cmbCuentas.getSelectionModel().select(null);
        rdbCargo.setSelected(false);
        rdbAbono.setSelected(false);
    }
    public void limpiarRegistro() {
        txtMonto.setText("");
        txtConcepto.setText("");
        txtNumeroDoc.setText("");
        txtDocSubido.setText("");
        datePartida.setValue(null);
        cmbCuentas.getSelectionModel().select(null);
        cmbTipoDoc.getSelectionModel().select(null);
        rdbCargo.setSelected(false);
        rdbAbono.setSelected(false);
        registroDetalle.clear();
        cargarTabla();
    }
    private void cargarCuentas() {
        CatalogoCuentaModel cuentaModel = new CatalogoCuentaModel();
        ObservableList<String> cuentas = cuentaModel.obtenerNombreCuentas(1);

        this.cmbCuentas.setItems(cuentas);

        //Autocompletado dinámico
        this.cmbCuentas.getEditor().addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            String texto = this.cmbCuentas.getEditor().getText().toLowerCase();

            // Si el texto está vacío, restauramos todos los elementos
            if (texto.isEmpty()) {
                this.cmbCuentas.setItems(cuentas);
                return;
            }

            // Filtramos las coincidencias
            ObservableList<String> filtradas = cuentas.filtered(item ->
                    item.toLowerCase().contains(texto)
            );

            this.cmbCuentas.setItems(filtradas);

            // Mostrar sugerencias
            this.cmbCuentas.show();
        });

        //Permitir borrar y volver a escribir
        this.cmbCuentas.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                this.cmbCuentas.setItems(cuentas);
            }
        });

        btnAgregarDoc.setOnAction(e -> {
            //Abrir el FileChooser para seleccionar PDF
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar documento PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf")
            );
            // Mostrar diálogo
            File archivoSeleccionado = fileChooser.showOpenDialog(btnAgregarDoc.getScene().getWindow());

            if (archivoSeleccionado != null) {
                //Guardar temporalmente el archivo en una variable de clase
                this.archivoSeleccionado = archivoSeleccionado;

                //Mostrar el nombre del archivo seleccionado
                txtDocSubido.setText("📄 Documento seleccionado: " + archivoSeleccionado.getName());

                System.out.println("Ruta del archivo seleccionado: " + archivoSeleccionado.getAbsolutePath());
            } else {
                txtDocSubido.setText("❌ No se seleccionó ningún documento.");
            }
        });
    }

    private void cargarTipoDoc() {
        CatalogoCuentaModel cuentaModel = new CatalogoCuentaModel();

        this.cmbCuentas.setItems(cuentaModel.obtenerNombreCuentas(1));
    }

    //Crea un copia del documento subido en lo almacena en documentos_partidas
    public boolean guardarCopiaPDF(String nombreDestino) {
        if (archivoSeleccionado == null) {
            System.out.println("No hay archivo seleccionado para guardar.");
            return false;
        }

        try {
            File carpetaDestino = new File(RUTA_DESTINO);
            if (!carpetaDestino.exists()) {
                System.out.println("La carpeta de destino no existe: " + RUTA_DESTINO);
                return false;
            }

            String nombreFinal = nombreDestino + ".pdf";
            File archivoDestino = new File(carpetaDestino, nombreFinal);

            // Copiar el archivo, reemplazando si ya existe
            Files.copy(
                    archivoSeleccionado.toPath(),
                    archivoDestino.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            System.out.println("Archivo guardado correctamente en: " + archivoDestino.getAbsolutePath());
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al guardar el archivo: " + e.getMessage());
            return false;
        }
    }
}
