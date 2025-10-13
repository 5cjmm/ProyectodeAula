/*package com.ShopMaster.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ShopMaster.Model.Abono;
import com.ShopMaster.Model.Deuda;
import com.ShopMaster.Model.ProductoVendido;
import com.ShopMaster.Repository.DeudaRepository;
import com.ShopMaster.Service.DeudaService;

@RestController
@RequestMapping("/api/deudas")
public class DeudaRestController {

    @Autowired
    private DeudaService deudaService;

    @Autowired
    private DeudaRepository deudaRepository;

    @GetMapping("/tienda/{tiendaId}")
    public ResponseEntity<?> listarDeudas(
            @PathVariable String tiendaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String estado) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("fechaVenta").descending());
            Page<Deuda> deudas;

            if (search.isEmpty() && estado.isEmpty()) {
                deudas = deudaRepository.findByTiendaId(tiendaId, pageable);
            } else if (!search.isEmpty() && estado.isEmpty()) {
                deudas = deudaRepository.findByTiendaIdAndNombreClienteContainingIgnoreCase(tiendaId, search, pageable);
            } else if (search.isEmpty() && !estado.isEmpty()) {
                deudas = deudaRepository.findByTiendaIdAndEstado(tiendaId, estado, pageable);
            } else {
                deudas = deudaRepository.findByTiendaIdAndNombreClienteContainingIgnoreCaseAndEstado(tiendaId, search, estado, pageable);
            }

            return ResponseEntity.ok(deudas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al cargar deudas: " + e.getMessage()));
        }
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarDeuda(@RequestBody Map<String, Object> deudaData) {
        try {
            String cedulaCliente = (String) deudaData.get("cedulaCliente");
            String nombreCliente = (String) deudaData.get("nombreCliente");
            String tiendaId = (String) deudaData.get("tiendaId");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productosData = (List<Map<String, Object>>) deudaData.get("productos");

            if (productosData == null || productosData.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No hay productos en la deuda"));
            }

            // Verificar si ya existe una deuda para este cliente
            Optional<Deuda> deudaExistente = deudaRepository.findByCedulaCliente(cedulaCliente);
            if (deudaExistente.isPresent() && !"PAGADA".equals(deudaExistente.get().getEstado())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El cliente ya tiene una deuda pendiente"));
            }

            // Crear productos vendidos
            List<ProductoVendido> productosVendidos = new java.util.ArrayList<>();
            double total = 0;

            for (Map<String, Object> prodData : productosData) {
                ProductoVendido prodVendido = new ProductoVendido();
                prodVendido.setCodigo((String) prodData.get("codigo"));
                prodVendido.setNombre((String) prodData.get("nombre"));
                prodVendido.setCantidad(((Number) prodData.get("cantidad")).intValue());
                prodVendido.setPrecio(((Number) prodData.get("precio")).doubleValue());
                productosVendidos.add(prodVendido);

                total += prodVendido.getCantidad() * prodVendido.getPrecio();
            }

            // Crear deuda
            Deuda deuda = new Deuda(cedulaCliente, nombreCliente, productosVendidos, total, LocalDateTime.now(), tiendaId);
            Deuda deudaGuardada = deudaService.registrarDeuda(deuda);

            return ResponseEntity.ok(Map.of(
                    "message", "Deuda registrada exitosamente",
                    "deudaId", deudaGuardada.getId(),
                    "total", total
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar deuda: " + e.getMessage()));
        }
    }

    @PutMapping("/abonar/{id}")
    public ResponseEntity<?> abonarDeuda(@PathVariable String id, @RequestBody Map<String, Object> abonoData) {
        try {
            double monto = ((Number) abonoData.get("monto")).doubleValue();
            
            Optional<Deuda> deudaOptional = deudaService.obtenerDeudaPorId(id);
            if (deudaOptional.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La deuda no existe"));
            }

            Deuda deuda = deudaOptional.get();
            if (monto > deuda.getTotalRestante()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El monto es mayor que el saldo restante"));
            }

            Deuda deudaActualizada = deudaService.registrarAbono(id, monto);
            return ResponseEntity.ok(Map.of(
                    "message", "Abono registrado exitosamente",
                    "deuda", deudaActualizada
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar abono: " + e.getMessage()));
        }
    }

    @GetMapping("/historial/{id}")
    public ResponseEntity<?> obtenerHistorialAbonos(@PathVariable String id) {
        try {
            Optional<Deuda> deudaOptional = deudaService.obtenerDeudaPorId(id);
            if (deudaOptional.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La deuda no existe"));
            }

            List<Abono> historial = deudaOptional.get().getHistorialAbonos();
            return ResponseEntity.ok(historial);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener historial: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarDeuda(@PathVariable String id) {
        try {
            Optional<Deuda> deudaOptional = deudaService.obtenerDeudaPorId(id);
            if (deudaOptional.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La deuda no existe"));
            }

            Deuda deuda = deudaOptional.get();
            if (!"PAGADA".equals(deuda.getEstado())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Solo se pueden eliminar deudas pagadas"));
            }

            deudaService.eliminar(id);
            return ResponseEntity.ok(Map.of("message", "Deuda eliminada exitosamente"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar deuda: " + e.getMessage()));
        }
    }
}
*/