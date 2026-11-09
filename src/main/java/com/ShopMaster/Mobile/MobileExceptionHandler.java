package com.ShopMaster.Mobile;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ShopMaster.Controller.JwtAuthController;

/**
 * Convierte los errores de los controladores móviles al formato del contrato
 * ({"success": false, "message": ...}). Solo aplica a esos controladores, así el
 * GlobalExceptionHandler de la web sigue funcionando igual.
 */
@RestControllerAdvice(assignableTypes = {
        JwtAuthController.class,
        MobilePerfilController.class,
        MobileInventarioController.class,
        MobileChatController.class,
        MobilePrediccionController.class
})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MobileExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(MobileExceptionHandler.class);

    @ExceptionHandler(MobileException.class)
    public ResponseEntity<Map<String, Object>> negocio(MobileException ex) {
        return ResponseEntity.status(ex.getStatus()).body(MobileApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> cuerpoInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(MobileApiResponse.error("La solicitud no es válida."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> inesperado(Exception ex) {
        log.error("Error inesperado en la API móvil", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MobileApiResponse.error("Error interno del servidor"));
    }
}
