package com.proyecto_sistemas_contables.models;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.sql.*;
public class EmpresaModel {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty nombre;
    private final SimpleStringProperty nit;
    private final SimpleStringProperty nrc;
    private final SimpleStringProperty direccion;
    private final SimpleStringProperty telefono;
    private final SimpleIntegerProperty idCorreo;
    private final SimpleStringProperty correo;
    private final SimpleStringProperty actividadEconomica;

    public EmpresaModel(int id, String nombre, String nit, String nrc, String direccion,
                   String telefono, int idCorreo, String correo, String actividadEconomica) {
        this.id = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.nit = new SimpleStringProperty(nit);
        this.nrc = new SimpleStringProperty(nrc);
        this.direccion = new SimpleStringProperty(direccion);
        this.telefono = new SimpleStringProperty(telefono);
        this.idCorreo = new SimpleIntegerProperty(idCorreo);
        this.correo = new SimpleStringProperty(correo);
        this.actividadEconomica = new SimpleStringProperty(actividadEconomica);
    }

    // Getters
    public int getId() { return id.get(); }
    public String getNombre() { return nombre.get(); }
    public String getNit() { return nit.get(); }
    public String getNrc() { return nrc.get(); }
    public String getDireccion() { return direccion.get(); }
    public String getTelefono() { return telefono.get(); }
    public int getIdCorreo() { return idCorreo.get(); }
    public String getCorreo() { return correo.get(); }
    public String getActividadEconomica() { return actividadEconomica.get(); }

    // Property getters (necesarios para TableView)
    public SimpleIntegerProperty idProperty() { return id; }
    public SimpleStringProperty nombreProperty() { return nombre; }
    public SimpleStringProperty nitProperty() { return nit; }
    public SimpleStringProperty nrcProperty() { return nrc; }
    public SimpleStringProperty direccionProperty() { return direccion; }
    public SimpleStringProperty telefonoProperty() { return telefono; }
    public SimpleStringProperty correoProperty() { return correo; }
    public SimpleStringProperty actividadEconomicaProperty() { return actividadEconomica; }
}
