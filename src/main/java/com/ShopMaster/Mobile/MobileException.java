package com.ShopMaster.Mobile;

import org.springframework.http.HttpStatus;

/** Error de negocio de la API móvil: lleva el código HTTP y el mensaje para el usuario. */
public class MobileException extends RuntimeException {

    private final HttpStatus status;

    public MobileException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static MobileException badRequest(String message) {
        return new MobileException(HttpStatus.BAD_REQUEST, message);
    }

    public static MobileException unauthorized(String message) {
        return new MobileException(HttpStatus.UNAUTHORIZED, message);
    }

    public static MobileException forbidden(String message) {
        return new MobileException(HttpStatus.FORBIDDEN, message);
    }

    public static MobileException notFound(String message) {
        return new MobileException(HttpStatus.NOT_FOUND, message);
    }
}
