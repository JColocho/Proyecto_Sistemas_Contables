package com.proyecto_sistemas_contables.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class DialogoUtil {

    public static <T> T showDialog(String fxml, String title, Stage owner) {
        try {
            //Ruta completa dentro de resources
            FXMLLoader loader = new FXMLLoader(DialogoUtil.class.getResource("/com/proyecto_sistemas_contables/dialogos/" + fxml + ".fxml"
            ));

            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(owner);
            stage.setResizable(false);
            stage.showAndWait();

            //Devuelve lo que el controlador ponga con stage.setUserData()
            return (T) stage.getUserData();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
