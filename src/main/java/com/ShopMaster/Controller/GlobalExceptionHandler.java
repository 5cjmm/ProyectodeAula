package com.ShopMaster.Controller;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<String> handleDuplicateKeyException(DuplicateKeyException ex) {
        String msg = ex.getMessage().toLowerCase();

        if (msg.contains("nit")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El NIT ya está registrado 🚫");
        } else if (msg.contains("telefono")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El teléfono ya está registrado 🚫");
        } else if (msg.contains("email")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El correo electrónico ya está registrado 🚫");
        } else if (msg.contains("username")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El nombre de usuario ya existe 🚫");
        } else if (msg.contains("codigo")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El código ya está registrado 🚫");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Campo duplicado 🚫");
    }

    /*@ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        String msg = ex.getMessage();

        if (msg.toLowerCase().contains("nit")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El NIT ya está registrado 🚫");
        }
        if (msg.toLowerCase().contains("telefono")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El teléfono ya está registrado 🚫");
        }
        if (msg.toLowerCase().contains("email")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El correo electrónico ya está registrado 🚫");
        }
        if (msg.toLowerCase().contains("username")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El nombre de usuario ya existe 🚫");
        }
        if (msg.toLowerCase().contains("codigo")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El código ya está registrado 🚫");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("⚠️ " + msg);
    }*/

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        ex.printStackTrace(); // log en consola
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("⚠️ Ocurrió un error inesperado: " + ex.getMessage());
    }
}
