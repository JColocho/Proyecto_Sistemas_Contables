package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.CatalogoCuentaModel;
import com.proyecto_sistemas_contables.models.DetallePartidaModel;
import com.proyecto_sistemas_contables.models.EmpresaModel;
import com.proyecto_sistemas_contables.models.PartidaModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class DetallePartidaController {
    @FXML
    private Button btnCerrar;

    @FXML
    private Button btnVerDoc;

    @FXML
    private TableColumn<DetallePartidaModel, Double> clAbono;

    @FXML
    private TableColumn<DetallePartidaModel, Double> clCargo;

    @FXML
    private TableColumn<DetallePartidaModel, String> clCuenta;

    @FXML
    private TextField txtFecha;

    @FXML
    private TableView<DetallePartidaModel> tbDetalle;

    @FXML
    private Text textDiferencia;

    @FXML
    private Text textTotalAbonos;

    @FXML
    private Text textTotalCargos;

    @FXML
    private TextArea txtConcepto;

    @FXML
    private TextField txtNumeroDoc;

    @FXML
    private TextField txtTipoDoc;

    public static int idPartida;
    private String documentoRuta;
    public static int idEmpresaSesion;
    @FXML
    public void initialize() {
        try{
            txtFecha.setEditable(false);
            tbDetalle.setEditable(false);
            txtConcepto.setEditable(false);
            txtNumeroDoc.setEditable(false);
            txtTipoDoc.setEditable(false);

            clCuenta.setCellValueFactory(new PropertyValueFactory<>("cuenta"));
            clAbono.setCellValueFactory(new PropertyValueFactory<>("abono"));
            clCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));

            cargarDetallePartida();

            File documento = new File(documentoRuta);
            btnVerDoc.setOnAction(e -> {
                try{
                    Desktop.getDesktop().open(documento);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            btnCerrar.setOnAction(e -> {
                Stage stage = (Stage) btnCerrar.getScene().getWindow();
                stage.close();
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

    }
    //Método para cargar todos los datos de la partida
    public void cargarDetallePartida() throws SQLException {
        PartidaModel partidaModel = new PartidaModel();
        partidaModel = partidaModel.obtenerPartida(idPartida);
        txtConcepto.setText(partidaModel.getConcepto());
        txtTipoDoc.setText(partidaModel.getTipoDocumento());
        txtNumeroDoc.setText(partidaModel.getNumeroDocumento());
        txtFecha.setText(partidaModel.getFecha().toString());

        //Cargamos la tabla con los datos del detalle de partida
        DetallePartidaModel detallePartidaModel = new DetallePartidaModel();
        tbDetalle.setItems(detallePartidaModel.obtenerDetallePartida(idPartida));
        EmpresaModel empresaModel = new EmpresaModel();
        //Obtenemos la ruta del documento fuente que se subió
        documentoRuta = "src/main/resources/com/proyecto_sistemas_contables/documentos_partidas/" + empresaModel.idBuscarEmpresa(idEmpresaSesion) +"/" + partidaModel.getNumeroDocumento() + ".pdf";

        //Cálculo de total de cargos, abonos y diferencia
        double cargo = 0.00;
        double abono = 0.00;
        double diferencia = 0.00;
        for (DetallePartidaModel dp : detallePartidaModel.obtenerDetallePartida(idPartida)) {
            cargo += dp.getCargo();
            abono += dp.getAbono();
        }

        diferencia = Math.abs(cargo - abono);

        textTotalCargos.setText(String.format("%.2f", cargo));
        textTotalAbonos.setText(String.format("%.2f", abono));
        textDiferencia.setText(String.format("%.2f", diferencia));
    }
}
