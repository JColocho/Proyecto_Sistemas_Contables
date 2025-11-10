package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.models.UsuarioModel;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class UsuariosController {

    @FXML private TextField txtFiltrar;

    @FXML private Button btnAgregar;
    @FXML private Button btnBuscar;

    @FXML private TableColumn<UsuarioModel, Integer> clId;
    @FXML private TableColumn<UsuarioModel, String> clUsuario;
    @FXML private TableColumn<UsuarioModel, String> clNombre;
    @FXML private TableColumn<UsuarioModel, String> clApellido;
    @FXML private TableColumn<UsuarioModel, String> clCorreo;
    @FXML private TableColumn<UsuarioModel, String> clAcceso;
    @FXML private TableColumn<UsuarioModel, ?> clAccion;

    @FXML private TableView<UsuarioModel> tbUsuarios;

    @FXML
    public void initialize() {
        System.out.println("id Usuario Actual: " + EmpresaController.idUsuarioSesion);


        clId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        clUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        clNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        clApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        clCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        clAcceso.setCellValueFactory(new PropertyValueFactory<>("nivelAcceso"));

        cargarUsuarios();
    }

    private void cargarUsuarios() {
        ObservableList<UsuarioModel> usuarios = UsuarioModel.obtenerUsuarios(EmpresaController.idUsuarioSesion);
        tbUsuarios.setItems(usuarios);
    }


}
