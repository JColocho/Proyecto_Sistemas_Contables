package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class AuditoriaController implements Initializable {

    // ========== COMPONENTES DE LA VISTA ==========
    @FXML private Button btnActualizar;

    // Filtros
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarFiltros;

    // Tabla de partidas
    @FXML private TableView<AuditoriaPartidaModel.PartidaAuditoriaView> tblPartidas;
    @FXML private TableColumn<AuditoriaPartidaModel.PartidaAuditoriaView, Integer> colIdPartida;
    @FXML private TableColumn<AuditoriaPartidaModel.PartidaAuditoriaView, Date> colFecha;
    @FXML private TableColumn<AuditoriaPartidaModel.PartidaAuditoriaView, String> colConcepto;
    @FXML private TableColumn<AuditoriaPartidaModel.PartidaAuditoriaView, String> colContador;
    @FXML private TableColumn<AuditoriaPartidaModel.PartidaAuditoriaView, String> colEstado;
    @FXML private TableColumn<AuditoriaPartidaModel.PartidaAuditoriaView, String> colTipoHallazgo;

    // Detalles de la partida seleccionada
    @FXML private Label lblIdPartida;
    @FXML private Label lblFechaPartida;
    @FXML private Label lblConceptoPartida;
    @FXML private Label lblTipoDocumento;
    @FXML private Label lblNumeroDocumento;

    // Tabla de detalles (cargos y abonos)
    @FXML private TableView<DetallePartidaModel> tblDetalles;
    @FXML private TableColumn<DetallePartidaModel, String> colCuenta;
    @FXML private TableColumn<DetallePartidaModel, Double> colCargo;
    @FXML private TableColumn<DetallePartidaModel, Double> colAbono;

    // Auditoría actual (si existe)
    @FXML private VBox panelAuditoriaExistente;
    @FXML private Label lblEstadoActual;
    @FXML private Label lblTipoHallazgoActual;
    @FXML private Label lblFechaAuditoria;
    @FXML private Label lblAuditor;
    @FXML private TextArea txtObservacionActual;
    @FXML private Button btnEditarAuditoria;

    // Formulario de nueva auditoría
    @FXML private VBox panelNuevaAuditoria;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ComboBox<String> cmbTipoHallazgo;
    @FXML private TextArea txtObservacion;
    @FXML private Button btnGuardarAuditoria;
    @FXML private Button btnCancelar;

    // Estadísticas
    @FXML private Label lblTotalPartidas;
    @FXML private Label lblPendientes;
    @FXML private Label lblAprobadas;
    @FXML private Label lblConObservaciones;
    @FXML private Label lblRechazadas;

    // ========== VARIABLES DE INSTANCIA ==========

    private int idUsuarioActual;
    private int idEmpresaActual;
    private AuditoriaPartidaModel.PartidaAuditoriaView partidaSeleccionada;
    private AuditoriaPartidaModel auditoriaActual;
    private boolean modoEdicion = false;

    // ========== INICIALIZACIÓN ==========

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarTablas();
        configurarComboBoxes();
        configurarEventos();
        ocultarPaneles();
    }

    /**
     * Configura las columnas de las tablas
     */
    private void configurarTablas() {
        // Tabla de partidas con auditoría
        colIdPartida.setCellValueFactory(new PropertyValueFactory<>("idPartida"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colConcepto.setCellValueFactory(new PropertyValueFactory<>("concepto"));
        colContador.setCellValueFactory(new PropertyValueFactory<>("contador"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoAuditoria"));
        colTipoHallazgo.setCellValueFactory(new PropertyValueFactory<>("tipoHallazgo"));

        // Aplicar estilos según el estado
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Sin auditar":
                            setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-border-color: #856404;");
                            break;
                        case "Pendiente":
                            setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                            break;
                        case "Aprobada":
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                            break;
                        case "Con observaciones":
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                            break;
                        case "Rechazada":
                            setStyle("-fx-background-color: #f5c6cb; -fx-text-fill: #721c24;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Tabla de detalles de partida
        colCuenta.setCellValueFactory(new PropertyValueFactory<>("cuenta"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colAbono.setCellValueFactory(new PropertyValueFactory<>("abono"));

        // Formatear montos
        colCargo.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) {
                    setText("");
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        colAbono.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) {
                    setText("");
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
    }

    /**
     * Configura los ComboBox con las opciones disponibles
     */
    private void configurarComboBoxes() {
        // Estados de auditoría - Definición clara
        cmbEstado.getItems().clear();
        cmbEstado.getItems().addAll(
                "Pendiente",      // No se ha revisado
                "Aprobada",       // Revisada y aprobada sin observaciones
                "Con observaciones", // Revisada pero con observaciones menores
                "Rechazada"       // Revisada y rechazada por errores graves
        );
        cmbEstado.setValue("Pendiente");

        // Tipos de hallazgo
        cmbTipoHallazgo.getItems().clear();
        cmbTipoHallazgo.getItems().addAll(
                "Conforme",
                "Recomendación",
                "Error Formal",
                "Error Material"
        );
        cmbTipoHallazgo.setValue("Conforme");
    }

    /**
     * Configura los eventos de los componentes
     */
    private void configurarEventos() {
        // Evento al seleccionar una partida
        tblPartidas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cargarDetallesPartida(newSelection);
            }
        });


        // Botón buscar
        if (btnBuscar != null) {
            btnBuscar.setOnAction(event -> buscarPartidas());
        }

        // Botón limpiar filtros
        if (btnLimpiarFiltros != null) {
            btnLimpiarFiltros.setOnAction(event -> limpiarFiltros());
        }

        // Botón guardar auditoría
        if (btnGuardarAuditoria != null) {
            btnGuardarAuditoria.setOnAction(event -> guardarAuditoria());
        }

        // Botón editar auditoría
        if (btnEditarAuditoria != null) {
            btnEditarAuditoria.setOnAction(event -> habilitarEdicionAuditoria());
        }

        // Botón cancelar
        if (btnCancelar != null) {
            btnCancelar.setOnAction(event -> cancelarEdicion());
        }

        if (btnActualizar != null) {
            btnActualizar.setOnAction(event -> recargarDatos());
        }
    }

    /**
     * Oculta los paneles de auditoría al inicio
     */
    private void ocultarPaneles() {
        if (panelAuditoriaExistente != null) {
            panelAuditoriaExistente.setVisible(false);
            panelAuditoriaExistente.setManaged(false);
        }
        if (panelNuevaAuditoria != null) {
            panelNuevaAuditoria.setVisible(false);
            panelNuevaAuditoria.setManaged(false);
        }
    }

    // ========== MÉTODOS PÚBLICOS ==========

    /**
     * Inicializa el controlador con los datos del usuario y empresa
     */
    public void inicializarDatos(int idUsuario, int idEmpresa) {
        this.idUsuarioActual = idUsuario;
        this.idEmpresaActual = idEmpresa;
        cargarPartidas();
        actualizarEstadisticas();
        configurarPermisos();
    }

    /**
     * Configura los permisos según el rol del usuario
     */
    private void configurarPermisos() {
        boolean puedeEditar = AccesoModel.puedeGestionarAuditorias(idUsuarioActual);

        // Si es solo lectura (no puede gestionar auditorías)
        if (!puedeEditar) {
            // Deshabilitar formularios de auditoría
            if (btnGuardarAuditoria != null) btnGuardarAuditoria.setDisable(true);
            if (btnEditarAuditoria != null) btnEditarAuditoria.setDisable(true);
            if (cmbEstado != null) cmbEstado.setDisable(true);
            if (cmbTipoHallazgo != null) cmbTipoHallazgo.setDisable(true);
            if (txtObservacion != null) txtObservacion.setEditable(false);
        }
    }

    // ========== MÉTODOS DE CARGA DE DATOS ==========

    /**
     * Carga las partidas con su estado de auditoría
     */
    /**
     * Carga las partidas con su estado de auditoría
     */
    private void cargarPartidas() {
        try {
            Date fechaInicio = null;
            Date fechaFin = null;

            if (dpFechaInicio != null && dpFechaInicio.getValue() != null) {
                fechaInicio = Date.valueOf(dpFechaInicio.getValue());
            }

            if (dpFechaFin != null && dpFechaFin.getValue() != null) {
                fechaFin = Date.valueOf(dpFechaFin.getValue());
            }

            ObservableList<AuditoriaPartidaModel.PartidaAuditoriaView> partidas =
                    AuditoriaPartidaModel.obtenerPartidasConAuditoria(fechaInicio, fechaFin, idEmpresaActual);

            tblPartidas.setItems(partidas);

            // IMPORTANTE: Actualizar estadísticas después de cargar las partidas
            actualizarEstadisticas();

        } catch (Exception e) {
            System.err.println("Error al cargar partidas: " + e.getMessage());
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron cargar las partidas: " + e.getMessage());
        }
    }

    /**
     * Carga los detalles de la partida seleccionada
     */
    private void cargarDetallesPartida(AuditoriaPartidaModel.PartidaAuditoriaView partida) {
        this.partidaSeleccionada = partida;

        // Obtener información completa de la partida
        PartidaModel partidaModel = new PartidaModel();
        PartidaModel partidaCompleta = partidaModel.obtenerPartida(partida.getIdPartida());

        // Mostrar información básica
        if (lblIdPartida != null) lblIdPartida.setText(String.valueOf(partida.getIdPartida()));
        if (lblFechaPartida != null) lblFechaPartida.setText(partida.getFecha().toString());
        if (lblConceptoPartida != null) lblConceptoPartida.setText(partida.getConcepto());
        if (lblTipoDocumento != null) lblTipoDocumento.setText(partidaCompleta.getTipoDocumento());
        if (lblNumeroDocumento != null) lblNumeroDocumento.setText(partidaCompleta.getNumeroDocumento());

        // Cargar detalles (cargos y abonos)
        ObservableList<DetallePartidaModel> detalles =
                DetallePartidaModel.obtenerDetallePartida(partida.getIdPartida());
        tblDetalles.setItems(detalles);

        // Verificar si ya existe auditoría
        verificarAuditoriaExistente(partida.getIdPartida());
    }

    /**
     * Verifica si la partida ya tiene auditoría y muestra el panel correspondiente
     */
    private void verificarAuditoriaExistente(int idPartida) {
        auditoriaActual = AuditoriaPartidaModel.obtenerAuditoriaPorPartida(idPartida);

        if (auditoriaActual != null) {
            // Ya existe auditoría - mostrar panel de auditoría existente
            mostrarAuditoriaExistente(auditoriaActual);
        } else {
            // No existe auditoría - mostrar panel para crear nueva
            mostrarFormularioNuevaAuditoria();
        }
    }

    /**
     * Muestra el panel con la auditoría existente
     */
    private void mostrarAuditoriaExistente(AuditoriaPartidaModel auditoria) {
        if (panelAuditoriaExistente != null) {
            panelAuditoriaExistente.setVisible(true);
            panelAuditoriaExistente.setManaged(true);
        }

        if (panelNuevaAuditoria != null) {
            panelNuevaAuditoria.setVisible(false);
            panelNuevaAuditoria.setManaged(false);
        }

        if (lblEstadoActual != null) lblEstadoActual.setText(auditoria.getEstadoAuditoria());
        if (lblTipoHallazgoActual != null) lblTipoHallazgoActual.setText(auditoria.getTipoHallazgo() != null ? auditoria.getTipoHallazgo() : "N/A");
        if (lblFechaAuditoria != null) lblFechaAuditoria.setText(auditoria.getFechaAuditoria().toString());
        if (lblAuditor != null) lblAuditor.setText(auditoria.getNombreAuditor());
        if (txtObservacionActual != null) {
            txtObservacionActual.setText(auditoria.getObservacion() != null ? auditoria.getObservacion() : "Sin observaciones");
            txtObservacionActual.setEditable(false);
        }
    }

    /**
     * Muestra el formulario para crear nueva auditoría
     */
    private void mostrarFormularioNuevaAuditoria() {
        if (panelNuevaAuditoria != null) {
            panelNuevaAuditoria.setVisible(true);
            panelNuevaAuditoria.setManaged(true);
        }

        if (panelAuditoriaExistente != null) {
            panelAuditoriaExistente.setVisible(false);
            panelAuditoriaExistente.setManaged(false);
        }

        limpiarFormulario();
    }

    // ========== MÉTODOS DE ACCIONES ==========

    /**
     * Busca partidas según los filtros aplicados
     */
    private void buscarPartidas() {
        cargarPartidas();
        actualizarEstadisticas();
    }

    /**
     * Limpia los filtros y recarga todas las partidas
     */
    private void limpiarFiltros() {
        if (dpFechaInicio != null) dpFechaInicio.setValue(null);
        if (dpFechaFin != null) dpFechaFin.setValue(null);
        cargarPartidas();
        actualizarEstadisticas();
    }

    /**
     * Guarda o actualiza la auditoría
     */
    private void guardarAuditoria() {
        if (!validarFormulario()) {
            return;
        }

        AuditoriaPartidaModel auditoria = new AuditoriaPartidaModel();
        auditoria.setIdPartida(partidaSeleccionada.getIdPartida());
        auditoria.setIdUsuarioAuditor(idUsuarioActual);
        auditoria.setEstadoAuditoria(cmbEstado.getValue());
        auditoria.setTipoHallazgo(cmbTipoHallazgo.getValue());
        auditoria.setObservacion(txtObservacion.getText().trim());

        boolean exito;

        if (modoEdicion && auditoriaActual != null) {
            // Actualizar auditoría existente
            auditoria.setIdAuditoria(auditoriaActual.getIdAuditoria());
            exito = auditoria.actualizarAuditoria(auditoria);
        } else {
            // Crear nueva auditoría
            exito = auditoria.crearAuditoria(auditoria);
        }

        if (exito) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito",
                    modoEdicion ? "Auditoría actualizada correctamente" : "Auditoría registrada correctamente");

            // Recargar todos los datos
            recargarDatos();

            if (partidaSeleccionada != null) {
                verificarAuditoriaExistente(partidaSeleccionada.getIdPartida());
            }
            modoEdicion = false;
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar la auditoría");
        }
    }

    /**
     * Método público para recargar todos los datos desde otras ventanas
     */
    public void recargarDatos() {
        cargarPartidas();
        actualizarEstadisticas();

        // Si hay una partida seleccionada, recargar sus detalles también
        if (partidaSeleccionada != null) {
            cargarDetallesPartida(partidaSeleccionada);
        }
    }

    /**
     * Habilita la edición de una auditoría existente
     */
    private void habilitarEdicionAuditoria() {
        if (auditoriaActual == null) {
            return;
        }

        modoEdicion = true;

        // Cambiar a panel de edición
        if (panelNuevaAuditoria != null) {
            panelNuevaAuditoria.setVisible(true);
            panelNuevaAuditoria.setManaged(true);
        }

        if (panelAuditoriaExistente != null) {
            panelAuditoriaExistente.setVisible(false);
            panelAuditoriaExistente.setManaged(false);
        }

        // Cargar datos actuales en el formulario
        cmbEstado.setValue(auditoriaActual.getEstadoAuditoria());
        cmbTipoHallazgo.setValue(auditoriaActual.getTipoHallazgo());
        txtObservacion.setText(auditoriaActual.getObservacion());

        if (btnGuardarAuditoria != null) {
            btnGuardarAuditoria.setText("Actualizar Auditoría");
        }
    }

    /**
     * Cancela la edición y vuelve al modo vista
     */
    private void cancelarEdicion() {
        modoEdicion = false;
        if (btnGuardarAuditoria != null) {
            btnGuardarAuditoria.setText("Guardar Auditoría");
        }

        if (partidaSeleccionada != null) {
            verificarAuditoriaExistente(partidaSeleccionada.getIdPartida());
        }
    }

    /**
     * Actualiza las estadísticas de auditoría
     */
    private void actualizarEstadisticas() {
        try {
            // Obtener TODAS las partidas sin filtros de fecha para las estadísticas
            ObservableList<AuditoriaPartidaModel.PartidaAuditoriaView> todasLasPartidas =
                    AuditoriaPartidaModel.obtenerPartidasConAuditoria(null, null, idEmpresaActual);

            if (todasLasPartidas == null || todasLasPartidas.isEmpty()) {
                // Si no hay partidas, establecer todo en cero
                establecerEstadisticasEnCero();
                return;
            }

            int total = todasLasPartidas.size();
            int pendientes = 0;
            int aprobadas = 0;
            int conObservaciones = 0;
            int rechazadas = 0;

            for (AuditoriaPartidaModel.PartidaAuditoriaView partida : todasLasPartidas) {
                String estado = partida.getEstadoAuditoria();
                if (estado == null || estado.equals("Sin auditar") || estado.equals("Pendiente")) {
                    pendientes++;
                } else if (estado.equals("Aprobada")) {
                    aprobadas++;
                } else if (estado.equals("Con observaciones")) {
                    conObservaciones++;
                } else if (estado.equals("Rechazada")) {
                    rechazadas++;
                }
            }

            // Actualizar las labels en el hilo de UI
            actualizarLabelsEstadisticas(total, pendientes, aprobadas, conObservaciones, rechazadas);

        } catch (Exception e) {
            System.err.println("Error al actualizar estadísticas: " + e.getMessage());
            establecerEstadisticasEnCero();
        }
    }

    /**
     * Establece todas las estadísticas en cero
     */
    private void establecerEstadisticasEnCero() {
        actualizarLabelsEstadisticas(0, 0, 0, 0, 0);
    }

    /**
     * Actualiza las labels de estadísticas en el hilo de UI
     */
    private void actualizarLabelsEstadisticas(int total, int pendientes, int aprobadas, int conObservaciones, int rechazadas) {
        javafx.application.Platform.runLater(() -> {
            if (lblTotalPartidas != null) lblTotalPartidas.setText(String.valueOf(total));
            if (lblPendientes != null) lblPendientes.setText(String.valueOf(pendientes));
            if (lblAprobadas != null) lblAprobadas.setText(String.valueOf(aprobadas));
            if (lblConObservaciones != null) lblConObservaciones.setText(String.valueOf(conObservaciones));
            if (lblRechazadas != null) lblRechazadas.setText(String.valueOf(rechazadas));
        });
    }

    // ========== MÉTODOS DE VALIDACIÓN Y UTILIDADES ==========

    /**
     * Valida el formulario de auditoría
     */
    private boolean validarFormulario() {
        if (partidaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", "Debe seleccionar una partida");
            return false;
        }

        if (cmbEstado.getValue() == null || cmbEstado.getValue().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", "Debe seleccionar un estado");
            return false;
        }

        if (cmbTipoHallazgo.getValue() == null || cmbTipoHallazgo.getValue().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", "Debe seleccionar un tipo de hallazgo");
            return false;
        }

        return true;
    }

    /**
     * Limpia el formulario de auditoría
     */
    private void limpiarFormulario() {
        if (cmbEstado != null) cmbEstado.setValue("Pendiente");
        if (cmbTipoHallazgo != null) cmbTipoHallazgo.setValue("Conforme");
        if (txtObservacion != null) txtObservacion.clear();
    }

    /**
     * Muestra una alerta al usuario
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}