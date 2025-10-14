package com.proyecto_sistemas_contables;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.proyecto_sistemas_contables.models.ReporteModel;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;

import java.io.File;
public class ReporteController {
    @FXML
    private ComboBox<String> cmbTipoReporte;
    @FXML
    private ComboBox<String> cmbPeriodoRapido;
    @FXML
    private DatePicker dateDesde;
    @FXML
    private DatePicker dateHasta;
    @FXML
    private Button btnLimpiar;
    @FXML
    private Button btnVistaPrevia;
    @FXML
    private Button btnExportarPDF;
    @FXML
    private TextField txtObservaciones;
    @FXML
    private TableView<Map<String, Object>> tblPreview;
    @FXML
    private Label lblRegistros;
    @FXML
    private Label lblPreviewInfo;
    @FXML
    private final ReporteModel reporteModel = new ReporteModel();
    public static DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static int idUsuarioEnSesion;

    @FXML
    public void initialize() {
        System.out.println("idEnviado: " + idUsuarioEnSesion);
        cmbTipoReporte.getItems().addAll("Libro Diario", "Libro Mayor");
        cmbPeriodoRapido.getItems().addAll("Personalizado", "Hoy", "Este mes", "Mes anterior", "Trimestre", "Semestral", "Anual");
        cmbTipoReporte.setValue("Libro Diario");
        cmbPeriodoRapido.setValue("Este mes");

        manejarPeriodoRapido("Este mes");

        cmbPeriodoRapido.setOnAction(e -> manejarPeriodoRapido(cmbPeriodoRapido.getValue()));
        btnLimpiar.setOnAction(e -> limpiarFiltros());
        btnVistaPrevia.setOnAction(e -> Platform.runLater(this::generarPrevisualizacion));
        btnExportarPDF.setOnAction(e -> Platform.runLater(this::exportarPDF));
    }

    private void manejarPeriodoRapido(String opcion) {
        LocalDate hoy = LocalDate.now();
        dateDesde.setDisable(false);
        dateHasta.setDisable(false);

        switch (opcion) {
            case "Hoy" -> { dateDesde.setValue(hoy); dateHasta.setValue(hoy); }
            case "Este mes" -> { dateDesde.setValue(hoy.withDayOfMonth(1)); dateHasta.setValue(hoy); }
            case "Mes anterior" -> {
                LocalDate inicio = hoy.minusMonths(1).withDayOfMonth(1);
                dateDesde.setValue(inicio);
                dateHasta.setValue(inicio.withDayOfMonth(inicio.lengthOfMonth()));
            }
            case "Trimestre" -> { dateDesde.setValue(hoy.minusMonths(3)); dateHasta.setValue(hoy); }
            case "Semestral" -> { dateDesde.setValue(hoy.minusMonths(6)); dateHasta.setValue(hoy); }
            case "Anual" -> { dateDesde.setValue(hoy.withDayOfYear(1)); dateHasta.setValue(hoy); }
            default -> { dateDesde.setValue(null); dateHasta.setValue(null); }
        }
    }

    private void limpiarFiltros() {
        cmbTipoReporte.setValue("Libro Diario");
        cmbPeriodoRapido.setValue("Personalizado");
        dateDesde.setValue(null);
        dateHasta.setValue(null);
        txtObservaciones.clear();
        tblPreview.getColumns().clear();
        tblPreview.getItems().clear();
        lblRegistros.setText("0 registros");
        lblPreviewInfo.setText("");
    }

    private void generarPrevisualizacion() {
        String tipo = cmbTipoReporte.getValue();
        LocalDate desde = dateDesde.getValue(), hasta = dateHasta.getValue();

        if (desde == null || hasta == null) {
            avisar("Error", "Debe seleccionar fechas válidas.");
            return;
        }

        try {
            List<Map<String, Object>> data = tipo.equals("Libro Diario")
                    ? reporteModel.obtenerLibroDiario(desde, hasta)
                    : reporteModel.obtenerLibroMayor(desde, hasta);

            poblarTablaDesdeMap(data);
            lblPreviewInfo.setText(tipo + " | " + desde.format(df) + " - " + hasta.format(df));

        } catch (Exception ex) {
            avisar("Error BD", ex.getMessage());
        }
    }

    private void poblarTablaDesdeMap(List<Map<String, Object>> filas) {
        tblPreview.getItems().clear();
        tblPreview.getColumns().clear();

        if (filas.isEmpty()) {
            tblPreview.setPlaceholder(new Label("Sin datos"));
            lblRegistros.setText("0 registros");
            return;
        }

        Map<String, Object> primera = filas.get(0);
        primera.keySet().forEach(key -> {
            TableColumn<Map<String, Object>, Object> col = new TableColumn<>(key);
            col.setCellValueFactory(param -> {
                Object value = param.getValue().get(key);
                if (value instanceof Number) {
                    return new ReadOnlyObjectWrapper<>(
                            String.format("%.2f", ((Number) value).doubleValue())
                    );
                } else {
                    return new ReadOnlyObjectWrapper<>(value);
                }
            });
            tblPreview.getColumns().add(col);
        });

        tblPreview.setItems(FXCollections.observableArrayList(filas));
        lblRegistros.setText(filas.size() + " registros");
    }

    private void exportarPDF() {
        String tipo = cmbTipoReporte.getValue();
        LocalDate desde = dateDesde.getValue(), hasta = dateHasta.getValue();

        if (desde == null || hasta == null) {
            avisar("Periodo inválido", "Debe seleccionar fechas válidas.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fc.setInitialFileName(tipo + "_" + desde.format(df) + "_" + hasta.format(df) + ".pdf");
        File destino = fc.showSaveDialog(tblPreview.getScene().getWindow());
        if (destino == null) return;

        try {
            List<Map<String, Object>> datos = tblPreview.getItems();
            generarReportePDF(tipo, 1, idUsuarioEnSesion, desde, hasta,
                    destino.getAbsolutePath(), txtObservaciones.getText(), datos);

            //Crear o verificar carpeta local
            Path carpetaLocal = Paths.get("src/main/resources/com/proyecto_sistemas_contables/reportes_generados");
            if (!Files.exists(carpetaLocal)) {
                Files.createDirectories(carpetaLocal);
            }

            //Crear copia local
            String nombreArchivo = tipo + "_" + desde.format(df) + "_" + hasta.format(df) + ".pdf";
            Path destinoCopia = carpetaLocal.resolve(nombreArchivo);

            Files.copy(destino.toPath(), destinoCopia, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Copia del PDF guardada en: " + destinoCopia.toAbsolutePath());

            // Registrar en BD usando la ruta de la copia local
            reporteModel.registrarReporteGenerado(
                    idUsuarioEnSesion,
                    tipo,
                    desde,
                    hasta,
                    destinoCopia.toAbsolutePath().toString(),
                    txtObservaciones.getText()
            );

            avisar("Éxito", "Reporte guardado en:\n" + destino.getAbsolutePath());

        } catch (Exception ex) {
            avisar("Error PDF", ex.getMessage());
        }
    }

    private void avisar(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        a.setContentText(msg);
        a.showAndWait();
    }
    public static void generarReportePDF(String tipo, int idEmpresa, int idUsuario,
                                         LocalDate desde, LocalDate hasta,
                                         String ruta, String observaciones,
                                         List<Map<String, Object>> datos) throws Exception {

        try (PdfWriter writer = new PdfWriter(new FileOutputStream(ruta));
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            Paragraph header = new Paragraph("Reporte: " + tipo)
                    .setBold()
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(header);

            Paragraph sub = new Paragraph("Empresa ID: " + idEmpresa + " | Usuario ID: " + idUsuario
                    + "\nPeriodo: " + desde.format(df) + " - " + hasta.format(df))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10);
            doc.add(sub);
            doc.add(new Paragraph("\n"));

            if (datos != null && !datos.isEmpty()) {
                int cols = datos.get(0).size();
                Table tabla = new Table(UnitValue.createPercentArray(cols)).useAllAvailableWidth();

                // Cabeceras
                datos.get(0).keySet().forEach(k ->
                        tabla.addHeaderCell(new Cell().add(new Paragraph(k).setBold())));

                // Filas
                for (Map<String, Object> fila : datos) {
                    for (Object val : fila.values()) {
                        String texto;
                        if (val instanceof Number) {
                            texto = String.format("%.2f", ((Number) val).doubleValue());
                        } else {
                            texto = val == null ? "" : val.toString();
                        }
                        tabla.addCell(new Cell().add(new Paragraph(texto)));
                    }
                }
                doc.add(tabla);
            }

            if (observaciones != null && !observaciones.isBlank()) {
                doc.add(new Paragraph("\nObservaciones:").setBold());
                doc.add(new Paragraph(observaciones));
            }
            else {
                doc.close();
            }
        }
    }
}
