package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("view_seleccionar_empresa.fxml")
        );

        // Tamaño inicial
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);

        stage.setTitle("Gestión de Empresas");
        stage.setScene(scene);

        // Tamaño mínimo
        stage.setMinWidth(1280);
        stage.setMinHeight(720);

        // Tamaño máximo
        stage.setMaxWidth(1920);
        stage.setMaxHeight(1080);

        ConexionDB conexion = new ConexionDB();
        conexion.connection();

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}