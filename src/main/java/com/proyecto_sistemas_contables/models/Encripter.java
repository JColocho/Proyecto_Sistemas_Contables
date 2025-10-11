package com.proyecto_sistemas_contables.models;

import org.mindrot.jbcrypt.BCrypt;

public class Encripter {
    //Genera un hash seguro para la contraseña
    public static String encrypt(String clave) {
        return BCrypt.hashpw(clave, BCrypt.gensalt(12));
    }

    //Compara si la clave coiciden con el hash almacenado
    public static boolean verificarClave(String clave, String encriptado) {
        return BCrypt.checkpw(clave, encriptado);
    }
}
