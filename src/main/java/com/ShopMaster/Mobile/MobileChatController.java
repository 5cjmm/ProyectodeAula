package com.ShopMaster.Mobile;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ShopMaster.Service.ChatBotService;

/**
 * POST /api/chat — chatbot de la app móvil. Reutiliza el mismo ChatBotService (Groq) de la
 * web, pero la tienda sale del token y no del cuerpo de la petición, así un usuario nunca
 * puede consultar los datos de otra tienda. La clave de Groq se queda en el servidor.
 */
@RestController
@RequestMapping("/api/chat")
public class MobileChatController {

    static final int MAX_CARACTERES = 500;

    public record ChatRequest(String mensaje) {
    }

    private final MobileContextService contexto;
    private final ChatBotService chatBotService;

    public MobileChatController(MobileContextService contexto, ChatBotService chatBotService) {
        this.contexto = contexto;
        this.chatBotService = chatBotService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(@RequestBody ChatRequest req) {
        String mensaje = req.mensaje() == null ? "" : req.mensaje().trim();
        if (mensaje.isEmpty()) {
            throw MobileException.badRequest("Escribe un mensaje para el asistente");
        }
        if (mensaje.length() > MAX_CARACTERES) {
            throw MobileException.badRequest("El mensaje no puede superar los " + MAX_CARACTERES + " caracteres");
        }
        String tiendaId = contexto.tiendaActual().id();
        String respuesta = chatBotService.responder(mensaje, tiendaId);

        Map<String, Object> d = new LinkedHashMap<>();
        d.put("respuesta", respuesta);
        return ResponseEntity.ok(MobileApiResponse.ok(d));
    }
}
