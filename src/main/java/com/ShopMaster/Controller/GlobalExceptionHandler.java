package com.ShopMaster.Controller;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // ⚠️ Manejo de duplicados (MongoDB índices únicos)
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<String> handleDuplicateKeyException(DuplicateKeyException ex) {
        String msg = ex.getMessage();
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

    // ⚠️ Manejo genérico de errores
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("⚠️ Ocurrió un error inesperado: " + ex.getMessage());
    }
}
