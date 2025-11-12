package com.ShopMaster.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ShopMaster.Model.Abono;
import com.ShopMaster.Model.Deuda;
import com.ShopMaster.Service.DeudaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/deudas")
@RequiredArgsConstructor
public class DeudaRestController {

    private final DeudaService deudaService;

    // 🔹 Registrar deuda (desde el Punto de Venta)
    @PostMapping("/crear")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public ResponseEntity<String> registrarDeuda(@RequestBody Deuda deuda) {
        try {
            deudaService.registrarDeuda(deuda);
            return ResponseEntity.ok("Deuda registrada");
        } catch (RuntimeException ex) {
            if ("ACTUALIZADA".equals(ex.getMessage())) {
                return ResponseEntity.ok("Deuda actualizada");
            }
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // 🔹 Listar deudas por tienda
    @GetMapping("/tienda/{tiendaId}")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public List<Deuda> listarDeudasPorTienda(@PathVariable String tiendaId) {
        return deudaService.obtenerDeudasPorTienda(tiendaId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public ResponseEntity<Deuda> obtenerDeudaPorId(@PathVariable String id) {
        Deuda deuda = deudaService.obtenerDeudaPorId(id)
                .orElseThrow(() -> new RuntimeException("Deuda no encontrada"));
        return ResponseEntity.ok(deuda);
    }

    // 🔹 Registrar abono a una deuda
    @PostMapping("/{id}/abonar")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public Deuda registrarAbono(
            @PathVariable String id,
            @RequestBody Abono abono) {
        return deudaService.registrarAbono(id, abono);
    }

    // 🔹 Eliminar deuda (si se requiere)
    @DeleteMapping("/eliminar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public void eliminarDeuda(@PathVariable String id) {
        deudaService.eliminarDeuda(id);
    }
}
