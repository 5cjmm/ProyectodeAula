package com.ShopMaster.Controller;
import org.springframework.data.domain.Page;
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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaRestController {

    private final VentaService ventaService;

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
}
