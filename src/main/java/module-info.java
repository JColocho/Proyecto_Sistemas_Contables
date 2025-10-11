module com.proyecto_sistemas_contables {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires jbcrypt;

    opens com.proyecto_sistemas_contables to javafx.fxml;
    opens com.proyecto_sistemas_contables.models to javafx.base;
    opens com.proyecto_sistemas_contables.Conexion to javafx.base;
    exports com.proyecto_sistemas_contables;
}