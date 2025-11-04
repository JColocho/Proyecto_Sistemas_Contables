package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.PartidaModel;
import com.proyecto_sistemas_contables.util.DialogoUtil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Date;

public class PartidasController {

    @FXML private TableView<PartidaModel> tbPartidas;
    @FXML private TableColumn<PartidaModel, Date> clFecha;
    @FXML private TableColumn<PartidaModel, String> clDetalle;
    @FXML private TableColumn<PartidaModel, String> clUsuario;
    @FXML private TableColumn<PartidaModel, Void> clAccion;

    @FXML private DatePicker dateInicial;
    @FXML private DatePicker dateFinal;
    @FXML private Button btnBuscar;
    @FXML private Button btnAgregarPartida;
    @FXML private Button btnLimpiar;

    @FXML
    void agregarPartida(ActionEvent event) {
        Stage stage = (Stage) tbPartidas.getScene().getWindow();
        DialogoUtil.showDialog("registro-partida-view", "Agregar partida", stage);
    }

    public static int idUsuarioSesion;
    public static int idEmpresaSesion;

    @FXML
    public void initialize() {
        try{
            RegistroPartidaController.idUsuarioSesion = idUsuarioSesion;
            RegistroPartidaController.idEmpresaSesion = idEmpresaSesion;

            // Ajustar los anchos en porcentaje
            clFecha.prefWidthProperty().bind(tbPartidas.widthProperty().multiply(0.15));
            clDetalle.prefWidthProperty().bind(tbPartidas .widthProperty().multiply(0.40));
            clUsuario.prefWidthProperty().bind(tbPartidas .widthProperty().multiply(0.25));
            clAccion.prefWidthProperty().bind(tbPartidas.widthProperty().multiply(0.20));


            clFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
            clDetalle.setCellValueFactory(new PropertyValueFactory<>("concepto"));
            clUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
            clAccion.setCellFactory(param -> new TableCell<>() {
                private final Button btn = new Button("Ver Detalle");
                {
                    btn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");
                    btn.setOnAction(event -> {

                        PartidaModel partida = getTableView().getItems().get(getIndex());
                        DetallePartidaController.idPartida = partida.getIdPartida();
                        DetallePartidaController.idEmpresaSesion = idEmpresaSesion;
                        Stage stage = (Stage) tbPartidas.getScene().getWindow();
                        DialogoUtil.showDialog("detalle-partida-view", "Detalle", stage);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) setGraphic(null);
                    else setGraphic(btn);
                }
            });

            cargarPartidas(null, null);

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
        ObservableList<PartidaModel> partidas = PartidaModel.obtenerPartidas(inicio, fin);
        tbPartidas.setItems(partidas);
    }


}
