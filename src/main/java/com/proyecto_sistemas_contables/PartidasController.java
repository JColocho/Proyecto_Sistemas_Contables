package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.EmpresaModel;
import com.proyecto_sistemas_contables.models.PartidaModel;
import com.proyecto_sistemas_contables.util.DialogoUtil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Date;
import java.util.Optional;

public class PartidasController {

    @FXML private TableView<PartidaModel> tbPartidas;
    @FXML private TableColumn<PartidaModel, Date> clFecha;
    @FXML private TableColumn<PartidaModel, String> clAsiento;
    @FXML private TableColumn<PartidaModel, String> clDetalle;
    @FXML private TableColumn<PartidaModel, String> clUsuario;
    @FXML private TableColumn<PartidaModel, Void> clAccion;

    @FXML private DatePicker dateInicial;
    @FXML private DatePicker dateFinal;
    @FXML private Button btnBuscar;
    @FXML private Button btnActualizar;
    @FXML private Button btnAgregarPartida;
    @FXML private Button btnLimpiar;

    public static int idUsuarioSesion;
    public static int idEmpresaSesion;
    public static String rolUsuarioSesion;

    @FXML
    public void initialize() {
        try{
            RegistroPartidaController.idUsuarioSesion = idUsuarioSesion;
            RegistroPartidaController.idEmpresaSesion = idEmpresaSesion;

            // Ajustar los anchos en porcentaje
            clFecha.prefWidthProperty().bind(tbPartidas.widthProperty().multiply(0.15));
            clAsiento.prefWidthProperty().bind(tbPartidas.widthProperty().multiply(0.10));
            clDetalle.prefWidthProperty().bind(tbPartidas .widthProperty().multiply(0.30));
            clUsuario.prefWidthProperty().bind(tbPartidas .widthProperty().multiply(0.25));
            clAccion.prefWidthProperty().bind(tbPartidas.widthProperty().multiply(0.20));


            clFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
            clAsiento.setCellValueFactory(new PropertyValueFactory<>("asiento"));
            clDetalle.setCellValueFactory(new PropertyValueFactory<>("concepto"));
            clUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
            clAccion.setCellFactory(param -> new TableCell<>() {
                private final Button btnEditar = new Button();
                private final Button btnEliminar = new Button();
                private final Button btnVer = new Button();
                private final HBox pane = new HBox(5);

                {
                    // Crear iconos
                    ImageView iconEditar = new ImageView(new Image(getClass().getResourceAsStream("/com/proyecto_sistemas_contables/static/img/write.png")));
                    iconEditar.setFitWidth(16);
                    iconEditar.setFitHeight(16);

                    ImageView iconEliminar = new ImageView(new Image(getClass().getResourceAsStream("/com/proyecto_sistemas_contables/static/img/bin.png")));
                    iconEliminar.setFitWidth(16);
                    iconEliminar.setFitHeight(16);
                    ImageView iconVer = new ImageView(new Image(getClass().getResourceAsStream("/com/proyecto_sistemas_contables/static/img/buscar.png")));
                    iconVer.setFitWidth(16);
                    iconVer.setFitHeight(16);


                    // Asignar iconos a los botones
                    btnEditar.setGraphic(iconEditar);
                    btnEliminar.setGraphic(iconEliminar);
                    btnVer.setGraphic(iconVer);

                    btnEditar.setStyle("-fx-background-color: rgb(210, 240, 240); -fx-text-fill: white; -fx-cursor: hand;");
                    btnEliminar.setStyle("-fx-background-color: rgb(243, 66, 53); -fx-text-fill: white; -fx-cursor: hand;");
                    btnVer.setStyle("-fx-background-color: rgb(210, 240, 240); -fx-text-fill: white; -fx-cursor: hand;");

                    btnEditar.setOnAction(event -> {
                        PartidaModel partida = getTableView().getItems().get(getIndex());
                        EditarPartidaController.idPartida = partida.getIdPartida();
                        EditarPartidaController.idEmpresaSesion = idEmpresaSesion;
                        EditarPartidaController.idUsuarioSesion = idUsuarioSesion;
                        Stage stage = (Stage) tbPartidas.getScene().getWindow();
                        DialogoUtil.showDialog("editar-partida-view", "Editar", stage);
                        Date inicio = dateInicial.getValue() != null ? java.sql.Date.valueOf(dateInicial.getValue()) : null;
                        Date fin = dateFinal.getValue() != null ? java.sql.Date.valueOf(dateFinal.getValue()) : null;
                        cargarPartidas(inicio, fin);
                    });

                    btnEliminar.setOnAction(event -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Eliminar partida");
                        alert.setHeaderText("¿Está seguro de eliminar esta partida?");
                        alert.setContentText("Esta acción no se puede revertir.");

                        Optional<ButtonType> resultado = alert.showAndWait();
                        if (resultado.get() == ButtonType.OK) {
                            PartidaModel partida = getTableView().getItems().get(getIndex());
                            partida.EliminarPartida(partida.getIdPartida(), idEmpresaSesion);
                            cargarPartidas(null, null);
                        }

                        Date inicio = dateInicial.getValue() != null ? java.sql.Date.valueOf(dateInicial.getValue()) : null;
                        Date fin = dateFinal.getValue() != null ? java.sql.Date.valueOf(dateFinal.getValue()) : null;
                        cargarPartidas(inicio, fin);
                    });

                    btnVer.setOnAction(event -> {
                        PartidaModel partida = getTableView().getItems().get(getIndex());
                        DetallePartidaController.idPartida = partida.getIdPartida();
                        DetallePartidaController.idEmpresaSesion = idEmpresaSesion;
                        Stage stage = (Stage) tbPartidas.getScene().getWindow();
                        DialogoUtil.showDialog("detalle-partida-view", "Detalle", stage);
                    });

                    if ("Contador".equalsIgnoreCase(rolUsuarioSesion)) {
                        btnEditar.setDisable(true);
                        btnEliminar.setDisable(true);
                    }
                    pane.setAlignment(Pos.CENTER);
                    pane.getChildren().addAll(btnVer,btnEditar, btnEliminar);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : pane);
                }
            });

            cargarPartidas(null, null);

            btnAgregarPartida.setOnAction(event -> {
                Stage stage = (Stage) tbPartidas.getScene().getWindow();
                DialogoUtil.showDialog("registro-partida-view", "Agregar partida", stage);

                Date inicio = dateInicial.getValue() != null ? java.sql.Date.valueOf(dateInicial.getValue()) : null;
                Date fin = dateFinal.getValue() != null ? java.sql.Date.valueOf(dateFinal.getValue()) : null;
                cargarPartidas(inicio, fin);
            });
            btnBuscar.setOnAction(event -> {
                Date inicio = dateInicial.getValue() != null ? java.sql.Date.valueOf(dateInicial.getValue()) : null;
                Date fin = dateFinal.getValue() != null ? java.sql.Date.valueOf(dateFinal.getValue()) : null;
                cargarPartidas(inicio, fin);
            });
            btnLimpiar.setOnAction(event -> {
                dateInicial.setValue(null);
                dateFinal.setValue(null);
                cargarPartidas(null, null);
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void cargarPartidas(Date inicio, Date fin) {
        ObservableList<PartidaModel> partidas = PartidaModel.obtenerPartidas(inicio, fin, idEmpresaSesion);
        tbPartidas.setItems(partidas);
    }


}
