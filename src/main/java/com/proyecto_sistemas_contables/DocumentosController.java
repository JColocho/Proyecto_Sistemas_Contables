package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.EmpresaModel;
import com.proyecto_sistemas_contables.models.PartidaModel;
import com.proyecto_sistemas_contables.models.ReporteModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DocumentosController {

    @FXML
    private Button btnDescargarPartida;

    @FXML
    private Button btnDescargarReporte;

    @FXML
    private Button btnVerDocumento;

    @FXML
    private Button btnVerReporte;

    @FXML
    private ComboBox<String> cbClasificacionPartidas;

    @FXML
    private ComboBox<String> cbTipoReporte;

    @FXML
    private TableColumn<ReporteModel, LocalDate> clDesde;

    @FXML
    private TableColumn<ReporteModel, String> clDocumento;

    @FXML
    private TableColumn<PartidaModel, LocalDate> clFecha;

    @FXML
    private TableColumn<ReporteModel, LocalDate> clHasta;

    @FXML
    private TableColumn<PartidaModel, Integer> clNumeroAsiento;

    @FXML
    private TableColumn<PartidaModel, String> clNumeroDoc;

    @FXML
    private TableColumn<PartidaModel, String> clTipoDocumento;

    @FXML
    private TableView<PartidaModel> tvDocumentosPartidas;

    @FXML
    private TableView<ReporteModel> tvReportes;

    @FXML
    private DatePicker dpFechaPartida;
    @FXML
    private Button btnLimpiarFecha;

    public static int idEmpresaSesion;
    private String rutaDocumentoPartida;

    public void initialize() throws SQLException {
        EmpresaModel empresaModel = new EmpresaModel();
        rutaDocumentoPartida = "src/main/resources/com/proyecto_sistemas_contables/documentos_partidas/" + empresaModel.idBuscarEmpresa(idEmpresaSesion);

        clDocumento.setCellValueFactory(new PropertyValueFactory<>("tipoReporte"));
        clDesde.setCellValueFactory(new PropertyValueFactory<>("fechaDesde"));
        clHasta.setCellValueFactory(new PropertyValueFactory<>("fechaHasta"));

        clNumeroAsiento.setCellValueFactory(new PropertyValueFactory<>("asiento"));
        clFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        clTipoDocumento.setCellValueFactory(new PropertyValueFactory<>("tipoDocumento"));
        clNumeroDoc.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));


        cbClasificacionPartidas.getItems().addAll(
                "Todos",
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

        cbTipoReporte.getItems().addAll(
                "Todos",
                "Libro Diario",
                "Libro Mayor",
                "Estado de Resultados",
                "Balance General",
                "Estado de Cambios en el Patrimonio",
                "Estado de Flujo de Efectivo"
        );

        cbClasificacionPartidas.getSelectionModel().selectFirst();
        cbTipoReporte.getSelectionModel().selectFirst();

        cbTipoReporte.setOnAction(event -> {
            if (cbTipoReporte.getSelectionModel().getSelectedItem().equals("Todos")) {
                ReporteModel reporte = new ReporteModel();
                tvReportes.setItems(reporte.obtenerReportes(idEmpresaSesion));
            }
            else {
                ReporteModel reporte = new ReporteModel();
                tvReportes.setItems(reporte.obtenerReportesPorTipo(idEmpresaSesion, cbTipoReporte.getSelectionModel().getSelectedItem()));
            }
        });
        cbClasificacionPartidas.setOnAction(event -> {
            if (cbClasificacionPartidas.getSelectionModel().getSelectedItem().equals("Todos")) {
                PartidaModel partidaModel = new PartidaModel();
                tvDocumentosPartidas.setItems(partidaModel.obtenerPartidaDocumento(idEmpresaSesion));
            }
            else {
                PartidaModel partidaModel = new PartidaModel();
                tvDocumentosPartidas.setItems(partidaModel.obtenerPartidaDocumentoPorTipo(idEmpresaSesion, cbClasificacionPartidas.getSelectionModel().getSelectedItem()));
            }
        });
        cargarTabla();

        btnVerReporte.setOnAction(event -> {
            if (tvReportes.getSelectionModel().getSelectedItem() != null) {
                try {
                    Desktop.getDesktop().open(new File(tvReportes.getSelectionModel().getSelectedItem().getRutaReporte()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Seleccione un Reporte para poder verlo");
                alert.showAndWait();
            }
        });
        btnDescargarReporte.setOnAction(event -> {
            if (tvReportes.getSelectionModel().getSelectedItem() != null) {
                descargarReporte();
            }
            else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Seleccione un Reporte para poder descargarlo.");
                alert.showAndWait();
            }
        });

        btnLimpiarFecha.setOnAction(event -> {
           dpFechaPartida.setValue(null);
            if (cbClasificacionPartidas.getSelectionModel().getSelectedItem().equals("Todos")) {
                PartidaModel partidaModel = new PartidaModel();
                tvDocumentosPartidas.setItems(partidaModel.obtenerPartidaDocumento(idEmpresaSesion));
            }
            else {
                PartidaModel partidaModel = new PartidaModel();
                tvDocumentosPartidas.setItems(partidaModel.obtenerPartidaDocumentoPorTipo(idEmpresaSesion, cbClasificacionPartidas.getSelectionModel().getSelectedItem()));
            }
        });

        dpFechaPartida.setOnAction(event -> {
            if (dpFechaPartida.getValue() != null) {
                PartidaModel partidaModel = new PartidaModel();
                tvDocumentosPartidas.setItems(partidaModel.obtenerPartidaPorFecha(idEmpresaSesion, java.sql.Date.valueOf(dpFechaPartida.getValue())));
            }
        });

        btnVerDocumento.setOnAction(event -> {
            if (tvDocumentosPartidas.getSelectionModel().getSelectedItem() != null) {
                try {
                    Desktop.getDesktop().open(new File(
                             rutaDocumentoPartida + "/" +tvDocumentosPartidas.getSelectionModel().getSelectedItem().getNumeroDocumento() + ".pdf"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Seleccione un Documento para poder verlo");
                alert.showAndWait();
            }
        });

        btnDescargarPartida.setOnAction(event -> {
            if (tvDocumentosPartidas.getSelectionModel().getSelectedItem() != null) {
                descargarDocumentoPartida();
            }
        });
    }

    //Carga el contenido de los tableView
    private void cargarTabla(){
        ReporteModel reporte = new ReporteModel();
        tvReportes.setItems(reporte.obtenerReportes(idEmpresaSesion));

        PartidaModel partida = new PartidaModel();
        tvDocumentosPartidas.setItems(partida.obtenerPartidaDocumento(idEmpresaSesion));
    }
    //Descarga una copia del reporte
    private void descargarReporte() {
        ReporteModel reporteSeleccionado = tvReportes.getSelectionModel().getSelectedItem();

        if (reporteSeleccionado == null) {
            mostrarAlerta("Advertencia", "Por favor, seleccione un reporte para guardar.", Alert.AlertType.WARNING);
            return;
        }

        try {
            File archivoOriginal = new File(reporteSeleccionado.getRutaReporte());

            if (!archivoOriginal.exists()) {
                mostrarAlerta("Error", "El archivo del reporte no existe en la ruta especificada.", Alert.AlertType.ERROR);
                return;
            }

            // Configurar FileChooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte");

            // Generar nombre sugerido para el archivo
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String nombreSugerido = reporteSeleccionado.getTipoReporte().replace(" ", "_") +
                    "_" + reporteSeleccionado.getFechaDesde().format(formatter) +
                    "_a_" + reporteSeleccionado.getFechaHasta().format(formatter) +
                    ".pdf";

            fileChooser.setInitialFileName(nombreSugerido);

            // Filtro para archivos PDF
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);

            // Establecer directorio inicial (Documentos del usuario)
            String userHome = System.getProperty("user.home");
            File documentsDir = new File(userHome, "Documents");
            if (documentsDir.exists()) {
                fileChooser.setInitialDirectory(documentsDir);
            }

            // Mostrar diálogo para guardar
            Stage stage = (Stage) tvReportes.getScene().getWindow();
            File archivoDestino = fileChooser.showSaveDialog(stage);

            if (archivoDestino != null) {
                // Copiar archivo
                Path origen = archivoOriginal.toPath();
                Path destino = archivoDestino.toPath();

                Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);

                mostrarAlerta("Éxito", "El reporte se guardó correctamente.", Alert.AlertType.INFORMATION);
            }

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo guardar el archivo: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    //Mostrar alertas
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    //Descarga una copia del documento de partida
    private void descargarDocumentoPartida() {
        PartidaModel partidaSeleccionada = tvDocumentosPartidas.getSelectionModel().getSelectedItem();

        if (partidaSeleccionada == null) {
            mostrarAlerta("Advertencia", "Por favor, seleccione un documento para guardar.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Construir ruta del archivo original
            String rutaArchivoOriginal = rutaDocumentoPartida + "/" +
                    partidaSeleccionada.getNumeroDocumento() + ".pdf";
            File archivoOriginal = new File(rutaArchivoOriginal);

            if (!archivoOriginal.exists()) {
                mostrarAlerta("Error", "El archivo del documento no existe en la ruta especificada.", Alert.AlertType.ERROR);
                return;
            }

            // Configurar FileChooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Documento");

            // Generar nombre sugerido para el archivo
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Convertir java.sql.Date a LocalDate si es necesario
            LocalDate fecha;
            if (partidaSeleccionada.getFecha() instanceof java.sql.Date) {
                fecha = ((java.sql.Date) partidaSeleccionada.getFecha()).toLocalDate();
            } else {
                fecha = partidaSeleccionada.getFecha().toLocalDate();
            }

            String nombreSugerido = partidaSeleccionada.getTipoDocumento().replace(" ", "_") +
                    "_" + partidaSeleccionada.getNumeroDocumento() +
                    "_Asiento_" + partidaSeleccionada.getAsiento() +
                    "_" + fecha.format(formatter) +
                    ".pdf";

            fileChooser.setInitialFileName(nombreSugerido);

            // Filtro para archivos PDF
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);

            // Establecer directorio inicial (Documentos del usuario)
            String userHome = System.getProperty("user.home");
            File documentsDir = new File(userHome, "Documents");
            if (documentsDir.exists()) {
                fileChooser.setInitialDirectory(documentsDir);
            }

            // Mostrar diálogo para guardar
            Stage stage = (Stage) tvDocumentosPartidas.getScene().getWindow();
            File archivoDestino = fileChooser.showSaveDialog(stage);

            if (archivoDestino != null) {
                // Copiar archivo
                Path origen = archivoOriginal.toPath();
                Path destino = archivoDestino.toPath();

                Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);

                mostrarAlerta("Éxito", "El documento se guardó correctamente.", Alert.AlertType.INFORMATION);
            }

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo guardar el archivo: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

}

