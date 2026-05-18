package com.ShopMaster.Service;

import com.ShopMaster.Model.Deuda;
import com.ShopMaster.Model.Productos;
import com.ShopMaster.Repository.DeudaRepository;
import com.ShopMaster.Repository.ProductosRepository;
import com.ShopMaster.Repository.VentaRepository;
import com.ShopMaster.Model.Venta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ChatBotService — arquitectura LLM + MCP + Groq + Alertas Inteligentes
 */
@Service
public class ChatBotService {

    private static final Logger log = LoggerFactory.getLogger(ChatBotService.class);
    private static final int UMBRAL_BAJO_STOCK = 5;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private DeudaRepository deudaRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    // ──────────────────────────────────────────────
    //  Punto de entrada principal
    // ──────────────────────────────────────────────
    public String responder(String mensaje, String tiendaId) {
        McpTool herramienta = detectarHerramienta(mensaje);
        String contexto = ejecutarHerramienta(herramienta, tiendaId);
        return llamarGroq(mensaje, contexto);
    }

    // ──────────────────────────────────────────────
    //  Alertas para la campanita 🔔
    // ──────────────────────────────────────────────
    public Map<String, Object> obtenerAlertas(String tiendaId) {
        List<String> alertas = new ArrayList<>();

        List<Productos> bajoStock = productosRepository.findByTiendaId(tiendaId)
                .stream()
                .filter(p -> p.getCantidad() <= UMBRAL_BAJO_STOCK)
                .collect(Collectors.toList());

        bajoStock.forEach(p ->
                alertas.add("📦 Bajo stock: " + p.getNombre() + " (" + p.getCantidad() + " und.)"));

        List<Deuda> deudasPendientes = deudaRepository.findByTiendaId(tiendaId)
                .stream()
                .filter(d -> "NO PAGADA".equals(d.getEstado()) || "PARCIAL".equals(d.getEstado()))
                .collect(Collectors.toList());

        deudasPendientes.forEach(d ->
                alertas.add("💸 Deuda pendiente: " + d.getNombreCliente()
                        + " — $" + String.format("%,.0f", d.getTotalRestante())));

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("total", alertas.size());
        resultado.put("alertas", alertas);
        resultado.put("bajoStock", bajoStock.size());
        resultado.put("deudasPendientes", deudasPendientes.size());
        return resultado;
    }

    // ──────────────────────────────────────────────
    //  MCP: Detección de herramienta por intención
    // ──────────────────────────────────────────────
    private enum McpTool {
        VENTAS_HOY, BAJO_STOCK, DEUDAS, ALERTAS_GENERAL, DESCONOCIDO
    }

    private McpTool detectarHerramienta(String mensaje) {
        String m = mensaje.toLowerCase();
        if (m.contains("vend") && (m.contains("hoy") || m.contains("día") || m.contains("dia")))
            return McpTool.VENTAS_HOY;
        if (m.contains("venta") || m.contains("ingreso") || m.contains("vend"))
            return McpTool.VENTAS_HOY;
        if (m.contains("stock") || m.contains("inventario") || m.contains("producto")
                || m.contains("poco") || m.contains("agotad"))
            return McpTool.BAJO_STOCK;
        if (m.contains("deuda") || m.contains("deber") || m.contains("cobrar")
                || m.contains("cliente") || m.contains("pendiente"))
            return McpTool.DEUDAS;
        if (m.contains("alerta") || m.contains("campan") || m.contains("aviso")
                || m.contains("problema") || m.contains("urgente"))
            return McpTool.ALERTAS_GENERAL;
        return McpTool.DESCONOCIDO;
    }

    // ──────────────────────────────────────────────
    //  MCP: Ejecución de la herramienta seleccionada
    // ──────────────────────────────────────────────
    private String ejecutarHerramienta(McpTool herramienta, String tiendaId) {
        switch (herramienta) {
            case VENTAS_HOY:      return toolVentasHoy(tiendaId);
            case BAJO_STOCK:      return toolBajoStock(tiendaId);
            case DEUDAS:          return toolDeudas(tiendaId);
            case ALERTAS_GENERAL: return toolAlertasGeneral(tiendaId);
            default:              return "No hay datos específicos. Sugiere preguntar por ventas, stock o deudas.";
        }
    }

    // — Tool 1: Ventas de hoy ————————————————————
    private String toolVentasHoy(String tiendaId) {
        ZoneId bogota = ZoneId.of("America/Bogota");
        LocalDate hoy = LocalDate.now(bogota);
        Date desde = Date.from(hoy.atStartOfDay(bogota).toInstant());
        Date hasta = Date.from(hoy.plusDays(1).atStartOfDay(bogota).toInstant());

        List<Venta> ventas = ventaRepository.findByTiendaIdAndFechaBetween(tiendaId, desde, hasta);
        double total = ventas.stream().mapToDouble(Venta::getTotal).sum();
        int cantidad = ventas.size();

        if (cantidad == 0) {
            return "Hoy (" + hoy + ") no se han registrado ventas aún en esta tienda.";
        }
        return String.format("Hoy (%s) se han realizado %d venta(s) con un total de $%,.0f COP.",
                hoy, cantidad, total);
    }

    // — Tool 2: Bajo stock ———————————————————————
    private String toolBajoStock(String tiendaId) {
        List<Productos> bajos = productosRepository.findByTiendaId(tiendaId)
                .stream()
                .filter(p -> p.getCantidad() <= UMBRAL_BAJO_STOCK)
                .sorted(Comparator.comparingInt(Productos::getCantidad))
                .collect(Collectors.toList());

        if (bajos.isEmpty()) {
            return "Todos los productos tienen stock suficiente (más de " + UMBRAL_BAJO_STOCK + " unidades).";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Hay ").append(bajos.size()).append(" producto(s) con bajo stock (≤ ")
                .append(UMBRAL_BAJO_STOCK).append(" und.):\n");
        bajos.forEach(p ->
                sb.append("  • ").append(p.getNombre())
                        .append(": ").append(p.getCantidad()).append(" und. — $")
                        .append(String.format("%,.0f", p.getPrecio())).append("\n"));
        return sb.toString();
    }

    // — Tool 3: Deudas pendientes ————————————————
    private String toolDeudas(String tiendaId) {
        List<Deuda> deudas = deudaRepository.findByTiendaId(tiendaId)
                .stream()
                .filter(d -> !"PAGADA".equals(d.getEstado()))
                .sorted(Comparator.comparingDouble(Deuda::getTotalRestante).reversed())
                .collect(Collectors.toList());

        if (deudas.isEmpty()) {
            return "No hay deudas pendientes. ¡Todos los clientes están al día!";
        }

        double totalPendiente = deudas.stream().mapToDouble(Deuda::getTotalRestante).sum();
        StringBuilder sb = new StringBuilder();
        sb.append("Hay ").append(deudas.size()).append(" deuda(s) pendiente(s) por $")
                .append(String.format("%,.0f", totalPendiente)).append(" COP:\n");

        deudas.stream().limit(5).forEach(d ->
                sb.append("  • ").append(d.getNombreCliente())
                        .append(" — debe $").append(String.format("%,.0f", d.getTotalRestante()))
                        .append(" [").append(d.getEstado()).append("]\n"));

        if (deudas.size() > 5)
            sb.append("  ... y ").append(deudas.size() - 5).append(" más.\n");
        return sb.toString();
    }

    // — Tool 4: Resumen general de alertas ————————
    private String toolAlertasGeneral(String tiendaId) {
        return "=== INVENTARIO ===\n" + toolBajoStock(tiendaId)
                + "\n=== DEUDAS ===\n"   + toolDeudas(tiendaId);
    }

    // ──────────────────────────────────────────────
    //  LLM: Llamada a Groq
    // ──────────────────────────────────────────────
    private String llamarGroq(String mensajeUsuario, String contexto) {
        if (groqApiKey == null || groqApiKey.isBlank()) {
            log.warn("GROQ_API_KEY no configurada — devolviendo datos sin LLM.");
            return contexto;
        }

        String systemPrompt = "Eres el asistente de ShopMaster, una app para tiendas colombianas. "
                + "Tono amigable, directo, en español. Solo usas los datos proporcionados, nunca inventas cifras. "
                + "Respuestas cortas (máximo 4 líneas) con emojis ocasionales.";

        String userContent = "Datos del sistema:\n" + contexto
                + "\n\nPregunta del tendero: " + mensajeUsuario;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.1-8b-instant");
        requestBody.put("max_tokens", 400);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemPrompt);
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userContent);
        messages.add(sysMsg);
        messages.add(userMsg);
        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.groq.com/openai/v1/chat/completions",
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Map.class
            );

            Map<?, ?> body = response.getBody();
            if (body != null && body.containsKey("choices")) {
                List<?> choices = (List<?>) body.get("choices");
                if (!choices.isEmpty()) {
                    Map<?, ?> choice  = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) choice.get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            log.error("Error llamando a Groq: {}", e.getMessage());
            return contexto;
        }

        return contexto;
    }
}
