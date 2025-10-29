package com.proyecto_sistemas_contables;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    //Stage principal para poder cambiar el contenido del mismo
    private static Stage mainStage;
    //Scene principal de la aplicación
    private static Scene mainScene;

    @Override
    public void start(Stage stage) throws IOException {
        //Cargar la vista inicial (login)
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view_seleccionar_empresa.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 650);
        //Agregamos un icono a la ventana
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/proyecto_sistemas_contables/static/img/icon.png")));
        //Guardar las referencias en stage y scene principales
        mainStage = stage;
        mainScene = scene;

        //Configuración de la ventana
        stage.setTitle("Sistema Contable");
        stage.setScene(scene);
        stage.show();
    }

    //Metodo para poder cambiar el contenido y escena
    public static void setRoot(String fxml) {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml+".fxml"));
            mainScene.setRoot(fxmlLoader.load());
            mainStage.setTitle("Sistema Contable");
        }
        catch (IOException e){
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
