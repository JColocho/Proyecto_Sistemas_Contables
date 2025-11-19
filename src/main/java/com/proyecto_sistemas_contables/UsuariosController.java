package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import com.proyecto_sistemas_contables.models.UsuarioModel;
import com.proyecto_sistemas_contables.util.DialogoUtil;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UsuariosController {
    @FXML private TextField txtCorreo;
    @FXML private TextField txtUsuario;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;

    @FXML private ComboBox<String> cmbEstado;

    @FXML private Button btnAgregar;
    @FXML private Button btnBuscar;

    @FXML private TableColumn<UsuarioModel, Integer> clId;
    @FXML private TableColumn<UsuarioModel, String> clUsuario;
    @FXML private TableColumn<UsuarioModel, String> clNombre;
    @FXML private TableColumn<UsuarioModel, String> clApellido;
    @FXML private TableColumn<UsuarioModel, String> clCorreo;
    @FXML private TableColumn<UsuarioModel, Boolean> clActivo;
    @FXML private TableColumn<UsuarioModel, String> clAcceso;
    @FXML private TableColumn<UsuarioModel, Void> clAccion;

    @FXML private TableView<UsuarioModel> tbUsuarios;
    private FilteredList<UsuarioModel> filteredUsuarios;    // filtros de la tabla usuarios

    @FXML
    public void initialize() {
        System.out.println("id Usuario Actual: " + EmpresaController.idUsuarioSesion);

        cmbEstado.getItems().addAll("Todos", "Activo", "Inactivo");
        cmbEstado.getSelectionModel().select("Todos");  // valor por defecto


        clId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        clUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        clNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        clApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        clCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));

        clActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        clActivo.setCellFactory(column -> new TableCell<UsuarioModel, Boolean>() {
            @Override
            protected void updateItem(Boolean activo, boolean empty) {
                super.updateItem(activo, empty);

                if (empty || activo == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                if (activo) {
                    setText("Activo");
                    setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                } else {
                    setText("Inactivo");
                    setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }
            }
        });


        clAcceso.setCellValueFactory(new PropertyValueFactory<>("nivelAcceso"));

        clAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button();
            private final Button btnEliminar = new Button();
            private final HBox pane = new HBox(5);

            {
                // Iconos
                ImageView iconEditar = new ImageView(new Image(getClass().getResourceAsStream(
                        "/com/proyecto_sistemas_contables/static/img/write.png")));
                iconEditar.setFitWidth(16);
                iconEditar.setFitHeight(16);

                ImageView iconEliminar = new ImageView(new Image(getClass().getResourceAsStream(
                        "/com/proyecto_sistemas_contables/static/img/bin.png")));
                iconEliminar.setFitWidth(16);
                iconEliminar.setFitHeight(16);

                // Asignar iconos a los botones
                btnEditar.setGraphic(iconEditar);
                btnEliminar.setGraphic(iconEliminar);

                // Estilos
                btnEditar.setStyle("-fx-background-color: rgb(210, 240, 240); -fx-text-fill: white; -fx-cursor: hand;");
                btnEliminar.setStyle("-fx-background-color: rgb(243, 66, 53); -fx-text-fill: white; -fx-cursor: hand;");

                // Acción de editar usuario
                btnEditar.setOnAction(event -> {
                    UsuarioModel usuario = getTableView().getItems().get(getIndex());
                    System.out.println("Editar usuario: " + usuario.getNombreUsuario());
                    // Abre ventana de edicion de usuario
                    // DialogoUtil.showDialog("editar-usuario-view", "Editar usuario", (Stage) tbUsuarios.getScene().getWindow());
                });

                // Acción de eliminar usuario
                btnEliminar.setOnAction(event -> {
                    UsuarioModel usuario = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Eliminar usuario");
                    alert.setHeaderText("¿Está seguro de eliminar al usuario " + usuario.getNombreUsuario() + "?");
                    alert.setContentText("Esta acción no se puede revertir.");

                    alert.showAndWait().ifPresent(res -> {
                        if (res == ButtonType.OK) {
                            //logica para desactivar usuario
                            System.out.println("usuario "+ usuario.getNombreUsuario() + " eliminado"    );
                        }
                    });
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


        cargarUsuarios();
        agregarFiltros();

    }

    @FXML
    private void agregarUsuario(ActionEvent event) {
        Stage stage = (Stage) tbUsuarios.getScene().getWindow();
        DialogoUtil.showDialog("registrar-usuario-view", "Agregar usuario", stage);
        cargarUsuarios();
    }
    private void cargarUsuarios() {
        ObservableList<UsuarioModel> usuarios = UsuarioModel.obtenerUsuarios(EmpresaController.idUsuarioSesion);
        filteredUsuarios = new FilteredList<>(usuarios, p -> true);
        tbUsuarios.setItems(filteredUsuarios);
    }

    private void agregarFiltros() {
        txtUsuario.textProperty().addListener((obs, oldValue, newValue) -> filtrar());
        txtNombre.textProperty().addListener((obs, oldValue, newValue) -> filtrar());
        txtApellido.textProperty().addListener((obs, oldValue, newValue) -> filtrar());
        txtCorreo.textProperty().addListener((obs, oldValue, newValue) -> filtrar());

        cmbEstado.valueProperty().addListener((obs, oldVal, newVal) -> filtrar());
    }


    private void filtrar() {
        if (filteredUsuarios == null) return;

        filteredUsuarios.setPredicate(usuario -> {

            // Campos de texto
            String usuarioTxt = txtUsuario.getText().toLowerCase().trim();
            String nombreTxt = txtNombre.getText().toLowerCase().trim();
            String apellidoTxt = txtApellido.getText().toLowerCase().trim();
            String correoTxt = txtCorreo.getText().toLowerCase().trim();

            // Estado desde el combo
            String estado = cmbEstado.getValue() == null ? "" : cmbEstado.getValue().toString();

            // Filtros
            boolean coincideUsuario = usuario.getNombreUsuario().toLowerCase().contains(usuarioTxt);
            boolean coincideNombre = usuario.getNombre().toLowerCase().contains(nombreTxt);
            boolean coincideApellido = usuario.getApellido().toLowerCase().contains(apellidoTxt);
            boolean coincideCorreo = usuario.getCorreo().toLowerCase().contains(correoTxt);

            boolean coincideEstado = true;

            if (estado.equals("Activo")) {
                coincideEstado = usuario.isActivo();
            } else if (estado.equals("Inactivo")) {
                coincideEstado = !usuario.isActivo();
            } // Si es "Todos", coincideEstado queda true

            return coincideUsuario && coincideNombre && coincideApellido && coincideCorreo && coincideEstado;
        });
    }

    @FXML
    private void limpiarFiltros() {
        txtCorreo.clear();
        txtUsuario.clear();
        txtNombre.clear();
        txtApellido.clear();
        cmbEstado.getSelectionModel().select("Todos");

        cargarUsuarios();
    }
}
