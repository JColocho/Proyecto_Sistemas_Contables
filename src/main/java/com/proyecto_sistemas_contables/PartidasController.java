package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.util.DialogoUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class PartidasController {

    @FXML
    private Button btnAgregarPartida;

    @FXML
    private TableView<?> tbPartidas;

    @FXML
    void agregarPartida(ActionEvent event) {
        Stage stage = (Stage) tbPartidas.getScene().getWindow();
        DialogoUtil.showDialog("registro-partida-view", "Agregar partida", stage);
    }

}
