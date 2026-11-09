package com.ShopMaster.Mobile;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Predicción de precios contra la inflación del DANE (solo lectura, requiere token).
 *
 *  GET /api/prediccion/productos
 *  GET /api/prediccion/precios?producto=Arroz Diana x 500g&anios=3
 */
@RestController
public class MobilePrediccionController {

    private final MobilePrediccionService prediccion;

    public MobilePrediccionController(MobilePrediccionService prediccion) {
        this.prediccion = prediccion;
    }

    @GetMapping("/api/prediccion/productos")
    public ResponseEntity<Map<String, Object>> productos() {
        return ResponseEntity.ok(prediccion.productos());
    }

    @GetMapping("/api/prediccion/precios")
    public ResponseEntity<Map<String, Object>> precios(
            @RequestParam(name = "producto", required = false) String producto,
            @RequestParam(name = "anios", required = false) String anios) {
        return ResponseEntity.ok(prediccion.precios(producto, anios));
    }
}
