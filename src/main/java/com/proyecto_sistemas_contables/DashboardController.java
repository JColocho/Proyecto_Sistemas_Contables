package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.EmpresaModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML
    private Label lblGastosTotales;

    @FXML
    private Label lblIngresosTotales;

    @FXML
    private Label lblTextoUtilidadPerdida;

    @FXML
    private Label lblUtilidadPerdida;

    private EmpresaModel empresaSeleccionada;

    /**
     * Método para recibir la empresa seleccionada desde otro controlador
     */
    public void setEmpresa(EmpresaModel empresa) {
        this.empresaSeleccionada = empresa;
    }
}