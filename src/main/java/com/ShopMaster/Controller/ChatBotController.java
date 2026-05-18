package com.ShopMaster.Controller;

import com.ShopMaster.Service.ChatBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ChatBotController
 * Expone el endpoint que recibe mensajes del chatbot flotante,
 * consulta los datos reales (ventas, inventario, deudas) y llama
 * a Groq para generar la respuesta en lenguaje natural.
 *
 * Endpoint: POST /api/chatbot/mensaje
 * Body:  { "mensaje": "¿Cuánto vendí hoy?", "tiendaId": "abc123" }
 */
@RestController
@RequestMapping("/api/chatbot")
public class ChatBotController {
    @Autowired
    private  ChatBotService chatBotService;

    @PostMapping("/mensaje")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public ResponseEntity<Map<String, String>> responder(@RequestBody Map<String, String> body) {
        String mensaje  = body.getOrDefault("mensaje",  "").trim();
        String tiendaId = body.getOrDefault("tiendaId", "").trim();

        if (mensaje.isEmpty() || tiendaId.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("respuesta", "Faltan parámetros: mensaje o tiendaId."));
        }

        String respuesta = chatBotService.responder(mensaje, tiendaId);
        return ResponseEntity.ok(Map.of("respuesta", respuesta));
    }

    /**
     * Resumen de alertas activas (bajo stock + deudas vencidas).
     * Usado por la campanita 🔔 para mostrar el badge con el conteo.
     *
     * GET /api/chatbot/alertas?tiendaId=abc123
     */
    @GetMapping("/alertas")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public ResponseEntity<Map<String, Object>> alertas(@RequestParam String tiendaId) {
        Map<String, Object> resumen = chatBotService.obtenerAlertas(tiendaId);
        return ResponseEntity.ok(resumen);
    }
}
