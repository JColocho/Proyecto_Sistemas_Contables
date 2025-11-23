package com.proyecto_sistemas_contables;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.proyecto_sistemas_contables.models.EmpresaModel;
import com.proyecto_sistemas_contables.models.ReporteModel;
import com.proyecto_sistemas_contables.models.UsuarioModel;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.awt.*;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import java.text.DecimalFormat;

import java.io.File;

public class ReporteController {

    @FXML private ComboBox<String> cmbTipoReporte;

    @FXML private ComboBox<String> cmbPeriodoRapido;

    @FXML private DatePicker dateDesde;

    @FXML private DatePicker dateHasta;

    @FXML private Button btnLimpiar;

    @FXML private Button btnExportarPDF;

    @FXML private TextField txtObservaciones;

    @FXML private TableView<Map<String, Object>> tblPreview;

    @FXML private Label lblRegistros;

    @FXML private Label lblPreviewInfo;

    @FXML private Button btnGuardar;

    /** Modelo para interactuar con la base de datos de reportes */
    private final ReporteModel reporteModel = new ReporteModel();

    /** Formateador para mostrar cantidades monetarias con signo $ y 2 decimales */
    private final DecimalFormat moneyFormat = new DecimalFormat("$#,##0.00");

    /** Formateador de fechas en formato año-mes-día (para nombres de archivos) */
    public static DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** ID del usuario que ha iniciado sesión */
    public static int idUsuarioEnSesion;

    /** ID de la empresa activa en la sesión */
    public static int idEmpresaSesion;

    @FXML
    public void initialize() {
        // Agregar opciones de tipos de reporte
        cmbTipoReporte.getItems().addAll(
                "Libro Diario",
                "Libro Mayor",
                "Estado de Resultados",
                "Balance General",
                "Estado de Cambios en el Patrimonio",
                "Estado de Flujo de Efectivo"
        );

        // Agregar opciones de periodos rápidos
        cmbPeriodoRapido.getItems().addAll("Hoy", "Este mes", "Mes anterior",
                "Trimestre", "Semestral", "Anual"
        );

        // Establecer valores por defecto
        cmbTipoReporte.setValue("Libro Diario");
        cmbPeriodoRapido.setValue("Este mes");

        // Aplicar el periodo "Este mes" por defecto
        manejarPeriodoRapido("Este mes");
        Platform.runLater(this::generarPrevisualizacion);

        cmbTipoReporte.setOnAction((event) -> {
            Platform.runLater(this::generarPrevisualizacion);
        });
        dateHasta.setOnAction((event) -> {
            if (dateHasta.getValue().isAfter(dateDesde.getValue()) || dateHasta.getValue() == dateDesde.getValue()) {
                Platform.runLater(this::generarPrevisualizacion);
            }
            else {
                dateDesde.setValue(dateHasta.getValue());
            }

        });
        dateDesde.setOnAction((event) -> {
            if (dateDesde.getValue().isBefore(dateHasta.getValue()) || dateDesde.getValue() == dateHasta.getValue()) {
                Platform.runLater(this::generarPrevisualizacion);
            }
            else {
                dateDesde.setValue(dateHasta.getValue());
            }
        });

        // Configurar event handlers
        cmbPeriodoRapido.setOnAction(e -> {
            manejarPeriodoRapido(cmbPeriodoRapido.getValue());
            Platform.runLater(this::generarPrevisualizacion);
        });
        btnExportarPDF.setOnAction(e -> Platform.runLater(this::exportarPDF));

        btnLimpiar.setOnAction(e -> {
            txtObservaciones.clear();
        });
        btnGuardar.setOnAction(e -> Platform.runLater(this::guardarPDF));
    }

    /**
     * Maneja la selección de periodos rápidos predefinidos.
     * Calcula automáticamente las fechas desde/hasta según la opción seleccionada.
     */
    private void manejarPeriodoRapido(String opcion) {
        LocalDate hoy = LocalDate.now();

        // Habilitar los DatePickers para permitir edición manual
        dateDesde.setDisable(false);
        dateHasta.setDisable(false);

        // Calcular fechas según la opción seleccionada
        switch (opcion) {
            case "Hoy" -> {
                dateDesde.setValue(hoy);
                dateHasta.setValue(hoy);
            }
            case "Este mes" -> {
                dateDesde.setValue(hoy.withDayOfMonth(1)); // Primer día del mes
                dateHasta.setValue(hoy);
            }
            case "Mes anterior" -> {
                LocalDate inicio = hoy.minusMonths(1).withDayOfMonth(1); // Primer día del mes anterior
                dateDesde.setValue(inicio);
                dateHasta.setValue(inicio.withDayOfMonth(inicio.lengthOfMonth())); // Último día del mes anterior
            }
            case "Trimestre" -> {
                dateDesde.setValue(hoy.minusMonths(3)); // 3 meses atrás
                dateHasta.setValue(hoy);
            }
            case "Semestral" -> {
                dateDesde.setValue(hoy.minusMonths(6)); // 6 meses atrás
                dateHasta.setValue(hoy);
            }
            case "Anual" -> {
                dateDesde.setValue(hoy.withDayOfYear(1)); // 1 de enero del año actual
                dateHasta.setValue(hoy);
            }
        }
    }

    /**
     * Genera la previsualización del reporte en la tabla.
     * Consulta la base de datos según el tipo de reporte y periodo seleccionados.
     */
    private void generarPrevisualizacion() {
        String tipo = cmbTipoReporte.getValue();
        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();

        // Validar fechas según el tipo de reporte
        if (tipo.equals("Balance General")) {
            // Balance General solo requiere fecha hasta
            if (hasta == null) {
                avisar("Error", "Debe seleccionar la fecha de corte para el Balance General.");
                return;
            }
        } else {
            // Los demás reportes requieren ambas fechas
            if (desde == null || hasta == null) {
                avisar("Error", "Debe seleccionar fechas válidas.");
                return;
            }
        }

        try {
            List<Map<String, Object>> data;

            // Llamar al método correspondiente según el tipo
            switch (tipo) {
                case "Libro Diario" ->
                        data = reporteModel.obtenerLibroDiario(desde, hasta, idEmpresaSesion);

                case "Libro Mayor" ->
                        data = reporteModel.obtenerLibroMayor(desde, hasta, idEmpresaSesion);

                case "Estado de Resultados" ->
                        data = reporteModel.obtenerEstadoResultados(desde, hasta, idEmpresaSesion);

                case "Balance General" ->
                        data = reporteModel.obtenerBalanceGeneral(desde, hasta, idEmpresaSesion);

                case "Estado de Cambios en el Patrimonio" ->
                        data = reporteModel.obtenerEstadoCapital(desde, hasta, idEmpresaSesion);

                case "Estado de Flujo de Efectivo" ->
                        data = reporteModel.obtenerFlujoEfectivo(desde, hasta, idEmpresaSesion);

                default -> {
                    avisar("Error", "Tipo de reporte no reconocido.");
                    return;
                }
            }

            poblarTablaDesdeMap(data);

            // Actualizar info según el tipo
            String infoReporte = tipo.equals("Balance General")
                    ? tipo + " | Al " + hasta.format(df)
                    : tipo + " | " + desde.format(df) + " - " + hasta.format(df);

            lblPreviewInfo.setText(infoReporte);

        } catch (Exception ex) {
            avisar("Error BD", ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Puebla la tabla de previsualización con datos dinámicos.
     * Crea columnas automáticamente basándose en las keys del Map.
     */
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

                // Manejo especial para columna "Concepto" en estados financieros
                if ("Concepto".equals(key) && value != null) {
                    String texto = value.toString();

                    // Aplicar estilos según prefijos
                    if (texto.startsWith(">>>")) {
                        // Encabezado de sección
                        return new ReadOnlyObjectWrapper<>(texto.replace(">>>", "").trim());
                    } else if (texto.startsWith("===")) {
                        // Total principal
                        return new ReadOnlyObjectWrapper<>(texto.replace("===", "").trim());
                    } else if (texto.startsWith("**")) {
                        // Subtotal
                        return new ReadOnlyObjectWrapper<>(texto.replace("**", "").trim());
                    }
                }

                // Columna "Asiento" sin formato
                if ("Asiento".equals(key) && value instanceof Number) {
                    return new ReadOnlyObjectWrapper<>(
                            String.valueOf(((Number) value).intValue())
                    );
                }
                // Columna "Código" sin formato
                else if ("Código".equals(key) && value != null) {
                    return new ReadOnlyObjectWrapper<>(value.toString());
                }
                // Números con formato de moneda
                else if (value instanceof Number) {
                    return new ReadOnlyObjectWrapper<>(
                            moneyFormat.format(((Number) value).doubleValue())
                    );
                }
                // Otros valores
                else {
                    return new ReadOnlyObjectWrapper<>(value);
                }
            });

            tblPreview.getColumns().add(col);
        });

        tblPreview.setItems(FXCollections.observableArrayList(filas));
        lblRegistros.setText(filas.size() + " registros");
    }

    /**
     * Exporta el reporte a un archivo PDF.
     */
    private void guardarPDF() {
        String tipo = cmbTipoReporte.getValue();
        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();

        // Validación especial para Balance General
        if (tipo.equals("Balance General")) {
            if (hasta == null) {
                avisar("Periodo inválido", "Debe seleccionar la fecha de corte.");
                return;
            }
            // Para Balance General, establecer desde = hasta si no está definido
            if (desde == null) desde = hasta;
        } else {
            if (desde == null || hasta == null) {
                avisar("Periodo inválido", "Debe seleccionar fechas válidas.");
                return;
            }
        }

        // Validar rango de fechas
        if (desde.isAfter(hasta)) {
            avisar("Periodo inválido", "La fecha 'Desde' no puede ser mayor que la fecha 'Hasta'.");
            return;
        }

        try {
            // Obtener datos necesarios
            UsuarioModel usuarioModel = new UsuarioModel();
            List<Map<String, Object>> datos = tblPreview.getItems();
            Map<String, String> infoEmpresa = reporteModel.obtenerInfoEmpresa(idEmpresaSesion);

            // Crear carpeta local para la empresa si no existe
            EmpresaModel empresaModel = new EmpresaModel();
            Path carpetaLocal = Paths.get(
                    "src/main/resources/com/proyecto_sistemas_contables/reportes_generados/"
                            + empresaModel.idBuscarEmpresa(idEmpresaSesion)
            );

            if (!Files.exists(carpetaLocal)) {
                Files.createDirectories(carpetaLocal);
            }

            // Crear el nombre del archivo PDF
            String nombreArchivo = tipo.replace(" ", "_") + "_" + desde.format(df) + "_" + hasta.format(df) + ".pdf";
            Path destinoPDF = carpetaLocal.resolve(nombreArchivo);

            // Generar directamente el PDF en la carpeta destino
            generarReportePDF(
                    tipo,
                    infoEmpresa,
                    usuarioModel.obtenerNombreUsuario(idUsuarioEnSesion),
                    desde,
                    hasta,
                    destinoPDF.toString(),
                    txtObservaciones.getText(),
                    datos
            );

            // Registrar el reporte en la base de datos
            reporteModel.registrarReporteGenerado(
                    idUsuarioEnSesion,
                    tipo,
                    desde,
                    hasta,
                    destinoPDF.toString(),
                    txtObservaciones.getText(),
                    idEmpresaSesion
            );

            // Mostrar mensaje de éxito
            avisar("Éxito", "Reporte generado exitosamente en:\n" + destinoPDF.toAbsolutePath());
            Desktop.getDesktop().open(destinoPDF.toFile());


        } catch (Exception ex) {
            avisar("Error PDF", "Ocurrió un error al generar el reporte:\n" + ex.getMessage());
            ex.printStackTrace();
        }
    }


    /**
     * Exporta el reporte a un archivo PDF.
     */
    private void exportarPDF() {
        String tipo = cmbTipoReporte.getValue();
        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();

        // Validación especial para Balance General
        if (tipo.equals("Balance General")) {
            if (hasta == null) {
                avisar("Periodo inválido", "Debe seleccionar la fecha de corte.");
                return;
            }
            // Para Balance General, establecer desde = hasta si no está definido
            if (desde == null) desde = hasta;
        } else {
            if (desde == null || hasta == null) {
                avisar("Periodo inválido", "Debe seleccionar fechas válidas.");
                return;
            }
        }

        // Validar fechas
        if (desde == null || hasta == null) {
            avisar("Periodo inválido", "Debe seleccionar fechas válidas.");
            return;
        }

        // Configurar diálogo para guardar archivo
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fc.setInitialFileName(tipo.replace(" ", "_") + "_" + desde.format(df) + "_" + hasta.format(df) + ".pdf");

        // Mostrar diálogo y obtener ubicación seleccionada
        File destino = fc.showSaveDialog(tblPreview.getScene().getWindow());
        if (destino == null) return; // Usuario canceló

        try {
            // Obtener datos necesarios
            UsuarioModel usuarioModel = new UsuarioModel();
            List<Map<String, Object>> datos = tblPreview.getItems();
            Map<String, String> infoEmpresa = reporteModel.obtenerInfoEmpresa(idEmpresaSesion);

            // Generar el PDF en la ubicación seleccionada
            generarReportePDF(tipo, infoEmpresa, usuarioModel.obtenerNombreUsuario(idUsuarioEnSesion),
                    desde, hasta, destino.getAbsolutePath(), txtObservaciones.getText(), datos);

            // Crear carpeta local para la empresa si no existe
            EmpresaModel empresaModel = new EmpresaModel();
            Path carpetaLocal = Paths.get(
                    "src/main/resources/com/proyecto_sistemas_contables/reportes_generados/"
                            + empresaModel.idBuscarEmpresa(idEmpresaSesion)
            );

            if (!Files.exists(carpetaLocal)) {
                Files.createDirectories(carpetaLocal);
            }

            // Crear copia local del PDF
            String nombreArchivo = tipo.replace(" ", "_") + "_" + desde.format(df) + "_" + hasta.format(df) + ".pdf";
            Path destinoCopia = carpetaLocal.resolve(nombreArchivo);
            Files.copy(destino.toPath(), destinoCopia, StandardCopyOption.REPLACE_EXISTING);

            // Registrar el reporte en la base de datos
            reporteModel.registrarReporteGenerado(
                    idUsuarioEnSesion, tipo, desde, hasta,
                    destinoCopia.toString(),
                    txtObservaciones.getText(),
                    idEmpresaSesion
            );

            // Mostrar mensaje de éxito
            avisar("Éxito", "Reporte guardado en:\n" + destino.getAbsolutePath());

        } catch (Exception ex) {
            avisar("Error PDF", ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Muestra un cuadro de diálogo informativo.
     */
    private void avisar(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        a.setContentText(msg);
        a.showAndWait();
    }

    /**
     * Genera un reporte PDF con formato profesional.
     */
    public static void generarReportePDF(String tipo, Map<String, String> infoEmpresa, String nombreUsuario,
                                         LocalDate desde, LocalDate hasta, String ruta,
                                         String observaciones, List<Map<String, Object>> datos) throws Exception {

        // Formateadores para el PDF
        DecimalFormat moneyFormat = new DecimalFormat("$#,##0.00");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Crear el documento PDF
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(ruta));
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            // ==================== COLORES CORPORATIVOS ====================
            DeviceRgb colorPrimario = new DeviceRgb(0, 153, 81);
            DeviceRgb colorSecundario = new DeviceRgb(52, 73, 94);

            // ==================== ENCABEZADO ====================
            // Nombre de la empresa (grande, en negrita, centrado, color primario)
            Paragraph empresa = new Paragraph(infoEmpresa.getOrDefault("nombre", "EMPRESA"))
                    .setBold()
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(colorPrimario);
            doc.add(empresa);

            // Información adicional de la empresa (NIT, dirección, teléfono)
            Paragraph infoEmp = new Paragraph(
                    "NIT: " + infoEmpresa.getOrDefault("nit", "N/A") + " | " +
                            infoEmpresa.getOrDefault("direccion", "") + "\n" +
                            "Tel: " + infoEmpresa.getOrDefault("telefono", ""))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(9)
                    .setFontColor(ColorConstants.DARK_GRAY);
            doc.add(infoEmp);

            // Línea separadora decorativa
            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("═══════════════════════════════════════════════════════════════")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(colorPrimario)
                    .setBold());
            doc.add(new Paragraph("\n"));

            // ==================== TÍTULO DEL REPORTE ====================
            Paragraph titulo = new Paragraph(tipo.toUpperCase())
                    .setBold()
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(colorSecundario);
            doc.add(titulo);

            // Información del periodo
            Paragraph periodo = new Paragraph(
                    "Periodo: " + desde.format(dateFormat) + " al " + hasta.format(dateFormat))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setItalic();
            doc.add(periodo);

            doc.add(new Paragraph("\n"));

            // ==================== TABLA DE DATOS ====================
            if (datos != null && !datos.isEmpty()) {
                // Calcular número de columnas y anchos
                int cols = datos.get(0).size();
                float[] columnWidths = calcularAnchoColumnas(tipo, cols);

                // Crear tabla con anchos calculados
                Table tabla = new Table(UnitValue.createPercentArray(columnWidths))
                        .useAllAvailableWidth()
                        .setFontSize(8);

                // Crear encabezados de columnas (con fondo azul y texto blanco)
                datos.get(0).keySet().forEach(k -> {
                    Cell headerCell = new Cell()
                            .add(new Paragraph(k).setBold().setFontColor(ColorConstants.WHITE))
                            .setBackgroundColor(colorPrimario)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setVerticalAlignment(VerticalAlignment.MIDDLE)
                            .setPadding(5);
                    tabla.addHeaderCell(headerCell);
                });

                // Generar filas de datos según el tipo de reporte
                switch (tipo) {
                    case "Libro Diario" ->
                            generarFilasLibroDiario(tabla, datos, moneyFormat);

                    case "Libro Mayor" ->
                            generarFilasLibroMayor(tabla, datos, moneyFormat, colorSecundario);

                    case "Estado de Resultados",
                         "Balance General",
                         "Estado de Cambios en el Patrimonio",
                         "Estado de Flujo de Efectivo" ->
                            generarFilasEstadosFinancieros(tabla, datos, moneyFormat, colorSecundario);
                }

                doc.add(tabla);
            }

            doc.add(new Paragraph("\n"));

            // ==================== PIE DE PÁGINA ====================
            // Observaciones (si existen)
            if (observaciones != null && !observaciones.isBlank()) {
                Paragraph obsTitle = new Paragraph("Observaciones:")
                        .setBold()
                        .setFontSize(10);
                doc.add(obsTitle);

                Paragraph obs = new Paragraph(observaciones)
                        .setFontSize(9)
                        .setItalic()
                        .setFontColor(ColorConstants.DARK_GRAY);
                doc.add(obs);
                doc.add(new Paragraph("\n"));
            }

            // Línea separadora final
            doc.add(new Paragraph("─────────────────────────────────────────────────────────────────")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.LIGHT_GRAY));

            // Información de auditoría (quién y cuándo generó el reporte)
            Paragraph footer = new Paragraph(
                    "Generado por: " + nombreUsuario + " | " +
                            "Fecha: " + LocalDateTime.now().format(dateTimeFormat))
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY);
            doc.add(footer);
        }
    }

    /**
     * Calcula los anchos óptimos de las columnas según el tipo de reporte.
     */
    private static float[] calcularAnchoColumnas(String tipo, int cols) {
        return switch (tipo) {
            case "Libro Diario" -> new float[]{10, 8, 30, 10, 25, 10, 10};
            case "Libro Mayor" -> new float[]{20, 10, 30, 12, 12, 15};
            case "Estado de Resultados",
                 "Balance General",
                 "Estado de Cambios en el Patrimonio",
                 "Estado de Flujo de Efectivo" -> new float[]{70, 30}; // Concepto y Monto
            default -> new float[]{50, 50}; // Por defecto 2 columnas iguales
        };
    }

    /**
     * Genera las filas de datos para el Libro Diario en formato PDF.
     */
    private static void generarFilasLibroDiario(Table tabla, List<Map<String, Object>> datos, DecimalFormat moneyFormat) {
        int asientoActual = -1;           // Control del asiento actual
        double totalCargo = 0;            // Acumulador de cargos del asiento
        double totalAbono = 0;            // Acumulador de abonos del asiento
        int contadorFilasAsiento = 0;     // Contador para alternar colores

        // Iterar sobre cada fila de datos
        for (int i = 0; i < datos.size(); i++) {
            Map<String, Object> fila = datos.get(i);
            Integer asientoObj = (Integer) fila.get("Asiento");

            // Validar que el asiento no sea nulo
            if (asientoObj == null) continue;
            int asiento = asientoObj;

            // Si cambió de asiento, agregar totales del asiento anterior
            if (asiento != asientoActual && asientoActual != -1) {
                agregarTotalesAsiento(tabla, totalCargo, totalAbono, moneyFormat);
                totalCargo = 0;
                totalAbono = 0;
                contadorFilasAsiento = 0;
            }

            asientoActual = asiento;
            contadorFilasAsiento++;

            // Color alternado para mejor legibilidad (gris claro / blanco)
            DeviceRgb bgColor = (contadorFilasAsiento % 2 == 0)
                    ? new DeviceRgb(245, 245, 245)  // Gris muy claro
                    : new DeviceRgb(255, 255, 255);  // Blanco

            // Procesar cada columna de la fila
            for (Map.Entry<String, Object> entry : fila.entrySet()) {
                Object val = entry.getValue();
                String key = entry.getKey();
                String texto = "";
                TextAlignment align = TextAlignment.LEFT;

                // Si es la columna "Asiento", mostrar como entero centrado
                if ("Asiento".equals(key) && val instanceof Number) {
                    texto = String.valueOf(((Number) val).intValue());
                    align = TextAlignment.CENTER;
                }
                // Si es un número (Cargo o Abono), aplicar formato de moneda
                else if (val instanceof Number) {
                    double num = ((Number) val).doubleValue();
                    texto = moneyFormat.format(num);
                    align = TextAlignment.RIGHT;

                    // Acumular totales
                    if ("Cargo".equals(key)) totalCargo += num;
                    if ("Abono".equals(key)) totalAbono += num;
                }
                // Otros valores (texto, fechas, etc.)
                else {
                    texto = val == null ? "" : val.toString();
                }

                // Crear y agregar la celda a la tabla
                Cell cell = new Cell()
                        .add(new Paragraph(texto).setFontSize(8))
                        .setTextAlignment(align)
                        .setBackgroundColor(bgColor)
                        .setPadding(3);
                tabla.addCell(cell);
            }
        }

        // Agregar totales del último asiento
        if (asientoActual != -1) {
            agregarTotalesAsiento(tabla, totalCargo, totalAbono, moneyFormat);
        }
    }

    /**
     * Agrega una fila de totales al final de cada asiento en el Libro Diario.
     */
    private static void agregarTotalesAsiento(Table tabla, double totalCargo, double totalAbono, DecimalFormat moneyFormat) {
        DeviceRgb colorTotal = new DeviceRgb(230, 230, 230);  // Gris claro para destacar

        // Celda que ocupa 5 columnas con el texto "TOTALES DEL ASIENTO"
        tabla.addCell(new Cell(1, 5)
                .add(new Paragraph("TOTALES DEL ASIENTO").setBold())
                .setTextAlignment(TextAlignment.RIGHT)
                .setBackgroundColor(colorTotal)
                .setPadding(5));

        // Celda con el total de cargos
        tabla.addCell(new Cell()
                .add(new Paragraph(moneyFormat.format(totalCargo)).setBold())
                .setTextAlignment(TextAlignment.RIGHT)
                .setBackgroundColor(colorTotal)
                .setPadding(5));

        // Celda con el total de abonos
        tabla.addCell(new Cell()
                .add(new Paragraph(moneyFormat.format(totalAbono)).setBold())
                .setTextAlignment(TextAlignment.RIGHT)
                .setBackgroundColor(colorTotal)
                .setPadding(5));

        // Fila separadora en blanco (7 celdas sin bordes)
        for (int i = 0; i < 7; i++) {
            tabla.addCell(new Cell().add(new Paragraph(" ")).setBorder(null).setPadding(2));
        }
    }

    /**
     * Genera las filas de datos para el Libro Mayor en formato PDF.
     */
    private static void generarFilasLibroMayor(Table tabla, List<Map<String, Object>> datos,
                                               DecimalFormat moneyFormat, DeviceRgb colorSecundario) {
        boolean esCabecera = false;  // Indica si la fila actual es un encabezado de cuenta

        // Iterar sobre cada fila de datos
        for (Map<String, Object> fila : datos) {
            String cuenta = (String) fila.get("Cuenta");

            // Detectar si es cabecera de cuenta (comienza con ">>>")
            if (cuenta != null && cuenta.startsWith(">>>")) {
                esCabecera = true;
            } else if (cuenta != null && !cuenta.isEmpty()) {
                esCabecera = false;
            }

            // Determinar color de fondo según el tipo de fila
            DeviceRgb bgColor;
            if (esCabecera) {
                // Azul claro para encabezados de cuenta
                bgColor = new DeviceRgb(220, 230, 240);
            } else if (cuenta == null || cuenta.isEmpty()) {
                // Blanco para separadores y movimientos
                bgColor = new DeviceRgb(255, 255, 255);
            } else {
                bgColor = new DeviceRgb(255, 255, 255);
            }

            // Procesar cada columna de la fila
            for (Map.Entry<String, Object> entry : fila.entrySet()) {
                Object val = entry.getValue();
                String key = entry.getKey();
                String texto = "";
                TextAlignment align = TextAlignment.LEFT;
                boolean negrita = esCabecera;  // Encabezados en negrita

                // Si es la columna "Código", mostrar como entero centrado
                if ("Código".equals(key) && val instanceof Number) {
                    texto = String.valueOf(((Number) val).intValue());
                    align = TextAlignment.CENTER;
                }
                // Si es un número (Cargo, Abono, Saldo), aplicar formato de moneda
                else if (val instanceof Number) {
                    double num = ((Number) val).doubleValue();
                    texto = moneyFormat.format(num);
                    align = TextAlignment.RIGHT;
                }
                // Otros valores (texto, fechas, etc.)
                else {
                    texto = val == null ? "" : val.toString();
                }

                // Crear párrafo con negrita si es cabecera
                Paragraph p = new Paragraph(texto).setFontSize(8);
                if (negrita) p.setBold();

                // Crear y agregar la celda a la tabla
                Cell cell = new Cell()
                        .add(p)
                        .setTextAlignment(align)
                        .setBackgroundColor(bgColor)
                        .setPadding(3);
                tabla.addCell(cell);
            }
        }
    }

    private static void generarFilasEstadosFinancieros(Table tabla, List<Map<String, Object>> datos,
                                                       DecimalFormat moneyFormat, DeviceRgb colorSecundario) {
        for (Map<String, Object> fila : datos) {
            String concepto = (String) fila.get("Concepto");
            Object montoObj = fila.get("Monto");

            DeviceRgb bgColor;
            boolean esNegrita = false;
            TextAlignment align = TextAlignment.LEFT;

            // Determinar formato según prefijos
            if (concepto != null) {
                if (concepto.startsWith(">>>")) {
                    // Encabezado de sección
                    bgColor = new DeviceRgb(0, 153, 81); // Verde
                    esNegrita = true;
                    concepto = concepto.replace(">>>", "").trim();

                    // Para encabezados, ocupar ambas columnas
                    Cell cellConcepto = new Cell(1, 2)
                            .add(new Paragraph(concepto).setBold().setFontColor(ColorConstants.WHITE))
                            .setBackgroundColor(bgColor)
                            .setTextAlignment(TextAlignment.LEFT)
                            .setPadding(5);
                    tabla.addCell(cellConcepto);
                    continue; // Saltar a la siguiente fila

                } else if (concepto.startsWith("===")) {
                    // Total principal
                    bgColor = new DeviceRgb(0, 122, 85); // verde
                    esNegrita = true;
                    concepto = concepto.replace("===", "").trim();

                } else if (concepto.startsWith("**")) {
                    // Subtotal
                    bgColor = new DeviceRgb(200, 200, 200); // Gris claro
                    esNegrita = true;
                    concepto = concepto.replace("**", "").trim();

                } else if (concepto.trim().isEmpty()) {
                    // Fila vacía (separador)
                    bgColor = (DeviceRgb) ColorConstants.WHITE;

                } else {
                    // Fila normal
                    bgColor = (DeviceRgb) ColorConstants.WHITE;
                }
            } else {
                bgColor = (DeviceRgb) ColorConstants.WHITE;
            }

            // Celda de concepto
            Paragraph pConcepto = new Paragraph(concepto == null ? "" : concepto).setFontSize(9);
            if (esNegrita) pConcepto.setBold();

            Cell cellConcepto = new Cell()
                    .add(pConcepto)
                    .setTextAlignment(align)
                    .setBackgroundColor(bgColor)
                    .setPadding(4);
            tabla.addCell(cellConcepto);

            // Celda de monto
            String textoMonto = "";
            if (montoObj instanceof Number) {
                textoMonto = moneyFormat.format(((Number) montoObj).doubleValue());
            }

            Paragraph pMonto = new Paragraph(textoMonto).setFontSize(9);
            if (esNegrita) pMonto.setBold();

            Cell cellMonto = new Cell()
                    .add(pMonto)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBackgroundColor(bgColor)
                    .setPadding(4);
            tabla.addCell(cellMonto);
        }
    }
}