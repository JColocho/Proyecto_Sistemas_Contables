package com.proyecto_sistemas_contables;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 650);
        stage.setTitle("Sistema Contable");
        stage.setScene(scene);
        stage.show();
    }
}
