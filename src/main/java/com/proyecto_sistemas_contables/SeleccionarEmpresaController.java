package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import com.proyecto_sistemas_contables.models.EmpresaModel;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SeleccionarEmpresaController {

    @FXML
    private TableView<EmpresaModel> tablaEmpresas;

    @FXML
    private TableColumn<EmpresaModel, Integer> colId;

    @FXML
    private TableColumn<EmpresaModel, String> colNombre;

    @FXML
    private TableColumn<EmpresaModel, String> colNit;

    @FXML
    private TableColumn<EmpresaModel, String> colNrc;

    @FXML
    private TableColumn<EmpresaModel, String> colDireccion;

    @FXML
    private TableColumn<EmpresaModel, String> colTelefono;

    @FXML
    private TableColumn<EmpresaModel, String> colCorreo;

    @FXML
    private TableColumn<EmpresaModel, String> colActividad;

    @FXML
    private TableColumn<EmpresaModel, Void> colAcciones;

    @FXML
    private AnchorPane formulario_empresa;

    @FXML
    private TextField txt_nombre_empresa, txt_nit, txt_nrc, txt_direccion, txt_telefono, txt_correo;

    @FXML
    private ComboBox<String> cb_actividad_eco;

    @FXML
    private Button btn_agregar, btn_cancelar;

    @FXML
    private TextField txt_buscar;

    @FXML
    private ComboBox<String> cb_buscar;

    private ObservableList<EmpresaModel> listaEmpresas = FXCollections.observableArrayList();
    private FilteredList<EmpresaModel> listaFiltrada;
    private EmpresaModel empresaEditando = null;

    @FXML
    private void initialize() {
        formulario_empresa.setVisible(false);

        // Inicializar ComboBox de actividad económica
        cb_actividad_eco.getItems().addAll(
                "Comercio", "Servicios", "Manufactura", "Construcción", "Turismo"
        );

        // Inicializar ComboBox de búsqueda
        cb_buscar.setItems(FXCollections.observableArrayList(
                "Todos",
                "Nombre",
                "NIT",
                "NRC",
                "Actividad Económica"
        ));
        cb_buscar.setValue("Todos"); // Valor por defecto

        // Configurar columnas y acciones
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNit.setCellValueFactory(new PropertyValueFactory<>("nit"));
        colNrc.setCellValueFactory(new PropertyValueFactory<>("nrc"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colActividad.setCellValueFactory(new PropertyValueFactory<>("actividadEconomica"));
        configurarColumnaAcciones();

        // Cargar empresas y configurar filtro
        cargarEmpresas();
        configurarFiltro();

        // Hacer responsive
        hacerFormularioResponsive();
    }

    // ------------------------
    // Configurar filtro de búsqueda
    // ------------------------
    private void configurarFiltro() {
        // Crear FilteredList
        listaFiltrada = new FilteredList<>(listaEmpresas, p -> true);
        tablaEmpresas.setItems(listaFiltrada);

        // Listener para el TextField de búsqueda
        txt_buscar.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarTabla();
        });

        // Listener para el ComboBox de filtro
        cb_buscar.valueProperty().addListener((observable, oldValue, newValue) -> {
            filtrarTabla();
        });
    }

    private void filtrarTabla() {
        String textoBusqueda = txt_buscar.getText().toLowerCase().trim();
        String criterio = cb_buscar.getValue();

        listaFiltrada.setPredicate(empresa -> {
            // Si el campo está vacío, mostrar todo
            if (textoBusqueda.isEmpty()) {
                return true;
            }

            // Filtrar según el criterio seleccionado
            switch (criterio) {
                case "Nombre":
                    return empresa.getNombre().toLowerCase().contains(textoBusqueda);

                case "NIT":
                    return empresa.getNit().toLowerCase().contains(textoBusqueda);

                case "NRC":
                    return empresa.getNrc().toLowerCase().contains(textoBusqueda);

                case "Actividad Económica":
                    return empresa.getActividadEconomica().toLowerCase().contains(textoBusqueda);

                case "Todos":
                default:
                    // Buscar en todos los campos
                    return empresa.getNombre().toLowerCase().contains(textoBusqueda) ||
                            empresa.getNit().toLowerCase().contains(textoBusqueda) ||
                            empresa.getNrc().toLowerCase().contains(textoBusqueda) ||
                            empresa.getDireccion().toLowerCase().contains(textoBusqueda) ||
                            empresa.getTelefono().toLowerCase().contains(textoBusqueda) ||
                            empresa.getCorreo().toLowerCase().contains(textoBusqueda) ||
                            empresa.getActividadEconomica().toLowerCase().contains(textoBusqueda);
            }
        });
    }

    // ------------------------
    // Responsive del formulario
    // ------------------------
    private void hacerFormularioResponsive() {
        AnchorPane parent = (AnchorPane) formulario_empresa.getParent();
        double maxWidth = formulario_empresa.getPrefWidth();
        double maxHeight = formulario_empresa.getPrefHeight();

        // Listener del ancho del padre
        parent.widthProperty().addListener((obs, oldVal, newVal) -> ajustarFormulario(maxWidth, maxHeight));
        parent.heightProperty().addListener((obs, oldVal, newVal) -> ajustarFormulario(maxWidth, maxHeight));
    }

    private void ajustarFormulario(double maxWidth, double maxHeight) {
        AnchorPane parent = (AnchorPane) formulario_empresa.getParent();
        double parentWidth = parent.getWidth();
        double parentHeight = parent.getHeight();

        // Ajustar tamaño
        double newWidth = Math.min(maxWidth, parentWidth - 40);
        double newHeight = Math.min(maxHeight, parentHeight - 40);

        formulario_empresa.setPrefWidth(newWidth);
        formulario_empresa.setPrefHeight(newHeight);

        // Centrar
        formulario_empresa.setLayoutX((parentWidth - newWidth) / 2);
        formulario_empresa.setLayoutY((parentHeight - newHeight) / 2);
    }

    // Configurar botones de acción en cada fila
    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(param -> new TableCell<>() {
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
                    EmpresaModel empresa = getTableView().getItems().get(getIndex());
                    editarEmpresa(empresa);
                });

                btnEliminar.setOnAction(event -> {
                    EmpresaModel empresa = getTableView().getItems().get(getIndex());
                    confirmarEliminar(empresa);
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
    }

    // Mostrar formulario al hacer click en "Agregar empresa"
    @FXML
    void abrirFormularioEmpresa(MouseEvent event) {
        limpiarFormulario();
        empresaEditando = null;
        btn_agregar.setText("Guardar");
        formulario_empresa.setVisible(true);
    }

    // Cancelar formulario
    @FXML
    private void cancelar() {
        limpiarFormulario();
        empresaEditando = null;
        formulario_empresa.setVisible(false);
    }

    // Guardar empresa (agregar o editar)
    @FXML
    private void guardarEmpresa() {
        String nombre = txt_nombre_empresa.getText();
        String nit = txt_nit.getText();
        String nrc = txt_nrc.getText();
        String direccion = txt_direccion.getText();
        String telefono = txt_telefono.getText();
        String correo = txt_correo.getText();
        String actividad_economica = cb_actividad_eco.getSelectionModel().getSelectedItem();

        if (nombre.isEmpty() || nit.isEmpty() || nrc.isEmpty() || direccion.isEmpty() || telefono.isEmpty() || correo.isEmpty() || actividad_economica == null) {
            mostrarAlerta("Error", "Debe completar los campos", Alert.AlertType.ERROR);
            return;
        }

        try (Connection conn = ConexionDB.connection()) {
            if (empresaEditando == null) {
                // Modo agregar
                // Insertar correo y obtener id
                String sqlCorreo = "INSERT INTO tblcorreos (correo) VALUES (?) RETURNING idcorreo";
                PreparedStatement psCorreo = conn.prepareStatement(sqlCorreo);
                psCorreo.setString(1, correo);
                ResultSet rsCorreo = psCorreo.executeQuery();
                int idCorreo = 0;
                if (rsCorreo.next()) {
                    idCorreo = rsCorreo.getInt("idcorreo");
                }

                // Insertar empresa con actividad económica
                String sqlEmpresa = """
                        INSERT INTO tblempresas (nombre, nit, nrc, direccion, telefono, idcorreo, actividad_economica)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """;
                PreparedStatement psEmpresa = conn.prepareStatement(sqlEmpresa);
                psEmpresa.setString(1, nombre);
                psEmpresa.setString(2, nit);
                psEmpresa.setString(3, nrc);
                psEmpresa.setString(4, direccion);
                psEmpresa.setString(5, telefono);
                psEmpresa.setInt(6, idCorreo);
                psEmpresa.setString(7, actividad_economica);
                psEmpresa.executeUpdate();

                mostrarAlerta("Éxito", "Empresa registrada correctamente.", Alert.AlertType.INFORMATION);
            } else {
                // Modo editar
                // Actualizar correo
                String sqlCorreo = "UPDATE tblcorreos SET correo = ? WHERE idcorreo = ?";
                PreparedStatement psCorreo = conn.prepareStatement(sqlCorreo);
                psCorreo.setString(1, correo);
                psCorreo.setInt(2, empresaEditando.getIdCorreo());
                psCorreo.executeUpdate();

                // Actualizar empresa
                String sqlEmpresa = """
                        UPDATE tblempresas 
                        SET nombre = ?, nit = ?, nrc = ?, direccion = ?, telefono = ?, actividad_economica = ?
                        WHERE idempresa = ?
                        """;
                PreparedStatement psEmpresa = conn.prepareStatement(sqlEmpresa);
                psEmpresa.setString(1, nombre);
                psEmpresa.setString(2, nit);
                psEmpresa.setString(3, nrc);
                psEmpresa.setString(4, direccion);
                psEmpresa.setString(5, telefono);
                psEmpresa.setString(6, actividad_economica);
                psEmpresa.setInt(7, empresaEditando.getId());
                psEmpresa.executeUpdate();

                mostrarAlerta("Éxito", "Empresa actualizada correctamente.", Alert.AlertType.INFORMATION);
            }

            // Recargar tabla y cerrar formulario
            cargarEmpresas();
            limpiarFormulario();
            empresaEditando = null;
            formulario_empresa.setVisible(false);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo guardar la empresa: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Editar empresa
    private void editarEmpresa(EmpresaModel empresa) {
        empresaEditando = empresa;

        txt_nombre_empresa.setText(empresa.getNombre());
        txt_nit.setText(empresa.getNit());
        txt_nrc.setText(empresa.getNrc());
        txt_direccion.setText(empresa.getDireccion());
        txt_telefono.setText(empresa.getTelefono());
        txt_correo.setText(empresa.getCorreo());
        cb_actividad_eco.getSelectionModel().select(empresa.getActividadEconomica());

        btn_agregar.setText("Actualizar");
        formulario_empresa.setVisible(true);
    }

    // Confirmar y eliminar empresa
    private void confirmarEliminar(EmpresaModel empresa) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar esta empresa?");
        confirmacion.setContentText(empresa.getNombre());

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                eliminarEmpresa(empresa);
            }
        });
    }

    // Eliminar empresa de la base de datos
    private void eliminarEmpresa(EmpresaModel empresa) {
        try (Connection conn = ConexionDB.connection()) {
            // Eliminar empresa
            String sqlEmpresa = "DELETE FROM tblempresas WHERE idempresa = ?";
            PreparedStatement psEmpresa = conn.prepareStatement(sqlEmpresa);
            psEmpresa.setInt(1, empresa.getId());
            psEmpresa.executeUpdate();

            // Eliminar correo asociado
            String sqlCorreo = "DELETE FROM tblcorreos WHERE idcorreo = ?";
            PreparedStatement psCorreo = conn.prepareStatement(sqlCorreo);
            psCorreo.setInt(1, empresa.getIdCorreo());
            psCorreo.executeUpdate();

            // Recargar tabla
            cargarEmpresas();
            mostrarAlerta("Éxito", "Empresa eliminada correctamente.", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo eliminar la empresa: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Cargar empresas desde la base de datos
    private void cargarEmpresas() {
        listaEmpresas.clear();
        try (Connection conn = ConexionDB.connection()) {
            String sql = """
                    SELECT e.idempresa, e.nombre, e.nit, e.nrc, e.direccion, 
                           e.telefono, e.idcorreo, c.correo, e.actividad_economica
                    FROM tblempresas e
                    LEFT JOIN tblcorreos c ON e.idcorreo = c.idcorreo
                    ORDER BY e.idempresa
                    """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                EmpresaModel empresa = new EmpresaModel(
                        rs.getInt("idempresa"),
                        rs.getString("nombre"),
                        rs.getString("nit"),
                        rs.getString("nrc"),
                        rs.getString("direccion"),
                        rs.getString("telefono"),
                        rs.getInt("idcorreo"),
                        rs.getString("correo"),
                        rs.getString("actividad_economica")
                );
                listaEmpresas.add(empresa);
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar las empresas: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Limpiar formulario
    private void limpiarFormulario() {
        txt_nombre_empresa.clear();
        txt_nit.clear();
        txt_nrc.clear();
        txt_direccion.clear();
        txt_telefono.clear();
        txt_correo.clear();
        cb_actividad_eco.getSelectionModel().clearSelection();
    }

    // Mostrar alertas
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}