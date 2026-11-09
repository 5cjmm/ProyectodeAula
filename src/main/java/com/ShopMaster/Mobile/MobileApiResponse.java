package com.ShopMaster.Mobile;

import java.util.LinkedHashMap;
import java.util.Map;

/** Envoltorio común de la API móvil: {"success": ..., "data": ..., "message": ...}. */
public final class MobileApiResponse {

    private MobileApiResponse() {
    }

    public static Map<String, Object> ok(Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("success", true);
        r.put("data", data);
        return r;
    }

    public static Map<String, Object> ok(Object data, Object paginacion) {
        Map<String, Object> r = ok(data);
        r.put("paginacion", paginacion);
        return r;
    }

    /** Respuesta exitosa que solo trae un mensaje para el usuario. */
    public static Map<String, Object> mensaje(String message) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("success", true);
        r.put("message", message);
        return r;
    }

    public static Map<String, Object> error(String message) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("success", false);
        r.put("message", message);
        return r;
    }
}
