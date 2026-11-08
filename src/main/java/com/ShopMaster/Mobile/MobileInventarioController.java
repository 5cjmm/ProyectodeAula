package com.ShopMaster.Mobile;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consultas de inventario para la app móvil. La tienda se toma del token,
 * nunca de un parámetro.
 *
 *  GET /api/inventario/resumen
 *  GET /api/inventario/estadisticas
 *  GET /api/productos
 *  GET /api/productos/stock-critico
 *  GET /api/productos/{id}
 */
@RestController
public class MobileInventarioController {

    private final MobileContextService contexto;
    private final MobileInventarioService inventario;

    public MobileInventarioController(MobileContextService contexto, MobileInventarioService inventario) {
        this.contexto = contexto;
        this.inventario = inventario;
    }

    @GetMapping("/api/inventario/resumen")
    public ResponseEntity<Map<String, Object>> resumen() {
        return ResponseEntity.ok(inventario.resumen(contexto.tiendaActual().id()));
    }

    @GetMapping("/api/inventario/estadisticas")
    public ResponseEntity<Map<String, Object>> estadisticas(@RequestParam(name = "dias", required = false) String dias) {
        return ResponseEntity.ok(inventario.estadisticas(contexto.tiendaActual().id(), dias));
    }

    @GetMapping("/api/productos")
    public ResponseEntity<Map<String, Object>> listar(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "orden", required = false) String orden,
            @RequestParam(name = "pagina", required = false) String pagina,
            @RequestParam(name = "tamano", required = false) String tamano) {
        String tiendaId = contexto.tiendaActual().id();
        return ResponseEntity.ok(inventario.listar(tiendaId, q, categoria, estado, orden, pagina, tamano));
    }

    // Se declara antes que /{id} para que "stock-critico" no se tome como un id.
    @GetMapping("/api/productos/stock-critico")
    public ResponseEntity<Map<String, Object>> stockCritico(
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "limite", required = false) String limite) {
        return ResponseEntity.ok(inventario.stockCritico(contexto.tiendaActual().id(), estado, limite));
    }

    @GetMapping("/api/productos/{id}")
    public ResponseEntity<Map<String, Object>> detalle(@PathVariable("id") String id) {
        return ResponseEntity.ok(inventario.detalle(contexto.tiendaActual().id(), id));
    }
}
