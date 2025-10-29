package com.ShopMaster.Controller;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ShopMaster.Model.Venta;
import com.ShopMaster.Service.VentaService;
import com.ShopMaster.Service.PdfService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaRestController {

    private final VentaService ventaService;
    private final PdfService pdfService;

    // 🔹 Registrar una nueva venta
    @PostMapping("/crear")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public Venta registrarVenta(@RequestBody Venta venta) {
        if (venta.getTiendaId() == null || venta.getProductos() == null || venta.getProductos().isEmpty()) {
            throw new RuntimeException("Datos incompletos para registrar venta");
        }
        return ventaService.registrarVenta(venta);
    }

    // 🔹 Listar ventas por tienda
    @GetMapping("/tienda/{tiendaId}")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public Page<Venta> listarVentasPorTienda(
            @PathVariable String tiendaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ventaService.obtenerVentasPorTienda(tiendaId, page, size);
    }

    // 🔹 Últimas 5 ventas (ordenadas por fecha desc) para vistas tipo resumen
    @GetMapping("/tienda/{tiendaId}/ultimas")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public java.util.List<Venta> ultimasVentas(@PathVariable String tiendaId,
                                              @RequestParam(defaultValue = "5") int limite) {
        return ventaService.obtenerUltimasVentas(tiendaId, Math.max(1, Math.min(limite, 20)));
    }

    // 🔹 Ventas mensuales (últimos N meses)
    @GetMapping("/tienda/{tiendaId}/mensuales")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public java.util.List<java.util.Map<String, Object>> ventasMensuales(
            @PathVariable String tiendaId,
            @RequestParam(defaultValue = "12") int meses) {
        return ventaService.obtenerVentasMensuales(tiendaId, meses);
    }

    // 🔹 Generar PDF del recibo de una venta
    @GetMapping("/{ventaId}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public ResponseEntity<byte[]> generarPdf(@PathVariable("ventaId") String ventaId) {
        byte[] pdf = pdfService.generarReciboVenta(ventaId);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=Factura-" + ventaId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }

    // 🔹 Ventas por día (paginado)
    @GetMapping("/tienda/{tiendaId}/por-dia")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public Page<Venta> ventasPorDia(
            @PathVariable String tiendaId,
            @RequestParam String fecha, // yyyy-MM-dd
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        java.time.LocalDate day = java.time.LocalDate.parse(fecha);
        java.time.ZoneId zone = java.time.ZoneId.of("America/Bogota");
        java.util.Date from = java.util.Date.from(day.atStartOfDay(zone).toInstant());
        java.util.Date to = java.util.Date.from(day.plusDays(1).atStartOfDay(zone).toInstant());
        return ventaService.obtenerVentasPorRango(tiendaId, from, to, page, size);
    }

    // 🔹 Resumen de un día (totales del día sin paginar)
    @GetMapping("/tienda/{tiendaId}/por-dia/resumen")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public java.util.Map<String, Object> resumenPorDia(
            @PathVariable String tiendaId,
            @RequestParam String fecha) {
        java.time.LocalDate day = java.time.LocalDate.parse(fecha);
        java.time.ZoneId zone = java.time.ZoneId.of("America/Bogota");
        java.util.Date from = java.util.Date.from(day.atStartOfDay(zone).toInstant());
        java.util.Date to = java.util.Date.from(day.plusDays(1).atStartOfDay(zone).toInstant());
        return ventaService.obtenerResumenPorRango(tiendaId, from, to);
    }

    // 🔹 PDF de informe de ventas por día
    @GetMapping("/tienda/{tiendaId}/informe/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public ResponseEntity<byte[]> informePdfPorDia(
            @PathVariable String tiendaId,
            @RequestParam String fecha) {
        java.time.LocalDate day = java.time.LocalDate.parse(fecha);
        java.time.ZoneId zone = java.time.ZoneId.of("America/Bogota");
        java.util.Date from = java.util.Date.from(day.atStartOfDay(zone).toInstant());
        java.util.Date to = java.util.Date.from(day.plusDays(1).atStartOfDay(zone).toInstant());
    byte[] pdf = pdfService.generarInformeVentasDia(tiendaId, from, to, fecha);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=InformeVentas-" + fecha + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}
