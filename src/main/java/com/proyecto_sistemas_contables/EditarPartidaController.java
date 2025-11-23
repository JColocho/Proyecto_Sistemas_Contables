package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.CatalogoCuentaModel;
import com.proyecto_sistemas_contables.models.DetallePartidaModel;
import com.proyecto_sistemas_contables.models.EmpresaModel;
import com.proyecto_sistemas_contables.models.PartidaModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Optional;

public class EditarPartidaController {
    @FXML
    private Button btnAgregarDoc;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnGuardarDetalle;

    @FXML
    private Button btnLimpiar;

    @FXML
    private Button btnActualizarPartida;

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
    private TableColumn<CatalogoCuentaModel, Void> colEliminar;

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

    @FXML
    private Hyperlink linkVerDoc;

    private ToggleGroup movimiento;

    private ObservableList<CatalogoCuentaModel> registroDetalle;
    public static int idUsuarioSesion;
    public static int idEmpresaSesion;

    // Guardará temporalmente el archivo PDF seleccionado
    private File archivoSeleccionado;

    // Ruta donde se guardarán los documentos (carpeta ya creada)
    private String RUTA_DESTINO;
    public static int idPartida;
    private String documentoRuta;
    private String numeroDocActual;

    public void initialize() throws SQLException {
        //Definimos la ruta de los documentos
        EmpresaModel empresaModel = new EmpresaModel();
        RUTA_DESTINO = "src/main/resources/com/proyecto_sistemas_contables/documentos_partidas/" + empresaModel.idBuscarEmpresa(idEmpresaSesion);

        cargarDetallePartida();
        cargarTabla();

        File documento = new File(documentoRuta);
        linkVerDoc.setOnAction(e -> {
            try{
                Desktop.getDesktop().open(documento);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        //Cargando lista de tipo de documento subido
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
                txtDocSubido.setText("Documento seleccionado: " + archivoSeleccionado.getName());
                linkVerDoc.setVisible(true);
                System.out.println("Ruta del archivo seleccionado: " + archivoSeleccionado.getAbsolutePath());
            } else {
                txtDocSubido.setText("No se seleccionó ningún documento.");
                linkVerDoc.setVisible(false);
            }
        });

        movimiento = new ToggleGroup();
        this.rdbCargo.setToggleGroup(movimiento);
        this.rdbAbono.setToggleGroup(movimiento);

        cargarCuentas();

        colCuenta.setCellValueFactory(new PropertyValueFactory<>("cuenta"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colAbono.setCellValueFactory(new PropertyValueFactory<>("abono"));
        colEliminar.setCellFactory(param -> new TableCell<>() {
            private final Button btnEliminar = new Button();
            private final HBox pane = new HBox(5);

            {
                ImageView iconEliminar = new ImageView(new Image(getClass().getResourceAsStream("/com/proyecto_sistemas_contables/static/img/bin.png")));
                iconEliminar.setFitWidth(16);
                iconEliminar.setFitHeight(16);


                btnEliminar.setGraphic(iconEliminar);

                btnEliminar.setStyle("-fx-background-color: rgb(243, 66, 53); -fx-text-fill: white; -fx-cursor: hand;");

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

                pane.setAlignment(Pos.CENTER);
                pane.getChildren().addAll(btnEliminar);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        formatearColumnaCargoAbono();

        btnGuardarDetalle.setOnAction(e -> {
            CatalogoCuentaModel cuentaModel = new CatalogoCuentaModel();

            if(cmbCuentas.getSelectionModel().getSelectedItem() != null){
                if(!txtMonto.getText().isEmpty()){
                    if (validarMonto()){
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
                        else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setContentText("No se ha seleccionado el tipo de movimiento.");
                            alert.show();
                        }
                    }
                    else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setContentText("El valor del monto es invalido.");
                        alert.show();
                    }
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("No se ha ingresado el monto para la partida");
                    alert.show();
                }
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("No se ha seleccionado la cuenta afectada.");
                alert.show();
            }
        });

        btnLimpiar.setOnAction(e -> {
            limpiarRegistroDetalle();
        });

        btnActualizarPartida.setOnAction(e -> {
            if(datePartida.getValue() != null){
                if(!txtConcepto.getText().isEmpty()){
                    if (cmbTipoDoc.getSelectionModel().getSelectedItem() != null){
                        if (!txtNumeroDoc.getText().isEmpty()){
                            PartidaModel validarDoc = new PartidaModel();
                            if (!validarDoc.numeroDocExistente(txtNumeroDoc.getText(), idEmpresaSesion) || txtNumeroDoc.getText().equals(numeroDocActual)){
                                if(archivoSeleccionado.exists()){
                                    if (guardarCopiaPDF(txtNumeroDoc.getText())){
                                        if (textDiferencia.getText().equals("$0.00")){
                                            if (!tblRegistroDetalle.getItems().isEmpty()){

                                                //Capturamos los datos para subir los datos de la partida
                                                PartidaModel partidaModel = new PartidaModel();
                                                partidaModel.setIdPartida(idPartida);
                                                partidaModel.setConcepto(txtConcepto.getText().trim().replace("   ", " ").replace("  ", " ").toUpperCase());
                                                partidaModel.setFecha(java.sql.Date.valueOf(datePartida.getValue()));
                                                partidaModel.setIdUsuario(idUsuarioSesion);
                                                partidaModel.setTipoDocumento(cmbTipoDoc.getSelectionModel().getSelectedItem());
                                                partidaModel.setNumeroDocumento(txtNumeroDoc.getText());

                                                //Actualizamos la partida en la DB
                                                partidaModel.ActualizarPartida(partidaModel);

                                                //Registramos los detalles de la partida
                                                for(CatalogoCuentaModel cuentaModel : registroDetalle){
                                                    DetallePartidaModel detallePartidaModel = new DetallePartidaModel();
                                                    detallePartidaModel.agregarDetalle(
                                                            idPartida,
                                                            cuentaModel.obtenerIdCuenta(cuentaModel.getCuenta(),idEmpresaSesion),
                                                            cuentaModel.getCargo(),
                                                            cuentaModel.getAbono());
                                                }

                                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                                alert.setTitle("Actualización de partida");
                                                alert.setContentText("La actualización de la partida fue exitoso.");
                                                alert.show();

                                                Stage stage = (Stage) btnActualizarPartida.getScene().getWindow();
                                                stage.close();

                                            }
                                            else {
                                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                                alert.setTitle("Error");
                                                alert.setContentText("No hay detalles de las cuentas.");
                                                alert.show();
                                            }
                                        }
                                        else {
                                            Alert alert = new Alert(Alert.AlertType.ERROR);
                                            alert.setTitle("Error");
                                            alert.setContentText("Existe una diferencia de: " + textDiferencia.getText() + ", la partida no debe tener diferencias.");
                                            alert.show();
                                        }
                                    }
                                }
                                else {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Error");
                                    alert.setContentText("No se ha subido el documento de la partida.");
                                    alert.show();
                                }
                            }else {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error");
                                alert.setContentText("No numero de documento ya existe, ingrese otro numero de documento.");
                                alert.show();
                            }
                        }
                        else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setContentText("No se ha ingresado el numero de documento.");
                            alert.show();
                        }
                    }
                    else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setContentText("No se ha seleccionado el tipo de documento a subir");
                        alert.show();
                    }

                }
                else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("No se ha ingresado el concepto de la partida.");
                    alert.show();
                }
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("No se ha ingresado la fecha de la partida generada.");
                alert.show();
            }
        });

        btnCancelar.setOnAction(e -> {
            Stage stage = (Stage) btnCancelar.getScene().getWindow();
            stage.close();
        });

    }

    //Metodo para validar si es un valor decimal y si es una cantidad válida
    public boolean validarMonto(){
        try{
            double monto = Double.parseDouble(txtMonto.getText());

            if (monto >= 0){
                return true;
            }
            else {
                return false;
            }
        }
        catch(Exception e){
            return false;
        }
    }
    //Metodo para cargar los datos de la partida en el TableView
    public void cargarTabla(){
        double cargo = 0.00;
        double abono = 0.00;
        double diferencia = 0.00;
        tblRegistroDetalle.setItems(registroDetalle);
        for (CatalogoCuentaModel catalogoCuentaModel : registroDetalle) {
            cargo += catalogoCuentaModel.getCargo();
            abono += catalogoCuentaModel.getAbono();
        }

        diferencia = Math.abs(cargo - abono);

        textTotalCargos.setText("$" +String.format("%.2f", cargo));
        textTotalAbonos.setText("$" +String.format("%.2f", abono));
        textDiferencia.setText("$" + String.format("%.2f", diferencia));
    }
    //Metodo para limpiar todos los campos del detalle de partida
    public void limpiarRegistroDetalle() {
        txtMonto.setText("");
        cmbCuentas.getSelectionModel().select(null);
        rdbCargo.setSelected(false);
        rdbAbono.setSelected(false);
    }
    //Método para cargar el cátalogo de cuentas
    private void cargarCuentas() {
        CatalogoCuentaModel cuentaModel = new CatalogoCuentaModel();
        ObservableList<String> cuentas = cuentaModel.obtenerNombreCuentas(idEmpresaSesion);

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
    }

    //Método para cargar todos los datos de la partida
    public void cargarDetallePartida() throws SQLException {
        PartidaModel partidaModel = new PartidaModel();
        partidaModel = partidaModel.obtenerPartida(idPartida);
        txtConcepto.setText(partidaModel.getConcepto());
        cmbTipoDoc.getSelectionModel().select(partidaModel.getTipoDocumento());
        txtNumeroDoc.setText(partidaModel.getNumeroDocumento());
        numeroDocActual =  partidaModel.getNumeroDocumento();
        datePartida.setValue(partidaModel.getFecha().toLocalDate());

        //Cargamos la tabla con los datos del detalle de partida
        DetallePartidaModel detallePartidaModel = new DetallePartidaModel();
        ObservableList<CatalogoCuentaModel> listaCuentas = FXCollections.observableArrayList();

        //Obtenemos los datos de la cuenta
        for (DetallePartidaModel detalle : detallePartidaModel.obtenerDetallePartida(idPartida)){
            CatalogoCuentaModel cuentaModel = new CatalogoCuentaModel();
            cuentaModel.setIdCuenta(detalle.getIdCuenta());
            cuentaModel.setCuenta(detalle.getCuenta());
            cuentaModel.setCargo(detalle.getCargo());
            cuentaModel.setAbono(detalle.getAbono());
            listaCuentas.add(cuentaModel);
        }

        //Rellenamos los registros de la cuenta
        registroDetalle = listaCuentas;

        EmpresaModel empresaModel = new EmpresaModel();
        //Obtenemos la ruta del documento fuente que se subió
        documentoRuta = "src/main/resources/com/proyecto_sistemas_contables/documentos_partidas/" + empresaModel.idBuscarEmpresa(idEmpresaSesion) +"/" + partidaModel.getNumeroDocumento() + ".pdf";
        txtDocSubido.setText("Documento seleccionado: " + partidaModel.getNumeroDocumento() + ".pdf");

        archivoSeleccionado = new File(documentoRuta);

        //Cálculo de total de cargos, abonos y diferencia
        double cargo = 0.00;
        double abono = 0.00;
        double diferencia = 0.00;
        for (DetallePartidaModel dp : detallePartidaModel.obtenerDetallePartida(idPartida)) {
            cargo += dp.getCargo();
            abono += dp.getAbono();
        }

        diferencia = Math.abs(cargo - abono);

        textTotalCargos.setText(String.format("%.2f", cargo));
        textTotalAbonos.setText(String.format("%.2f", abono));
        textDiferencia.setText(String.format("%.2f", diferencia));
    }

    //Crea una copia del documento subido a documentos_partidas
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
    private void formatearColumnaCargoAbono() {
        colCargo.setCellFactory(tc -> new TableCell<CatalogoCuentaModel, Double>() {
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

        colAbono.setCellFactory(tc -> new TableCell<CatalogoCuentaModel, Double>() {
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
