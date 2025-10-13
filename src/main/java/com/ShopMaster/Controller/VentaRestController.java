/*package com.ShopMaster.Controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ShopMaster.Model.ProductoVendido;
import com.ShopMaster.Model.Productos;
import com.ShopMaster.Model.Venta;
import com.ShopMaster.Repository.ProductosRepository;
import com.ShopMaster.Repository.VentaRepository;
import com.ShopMaster.Service.VentaService;

@RestController
@RequestMapping("/api/ventas")
public class VentaRestController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductosRepository productosRepository;

    @GetMapping("/tienda/{tiendaId}")
    public ResponseEntity<?> listarVentas(
            @PathVariable String tiendaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
            Page<Venta> ventas;

            if (search.isEmpty()) {
                ventas = ventaRepository.findByTiendaId(tiendaId, pageable);
            } else {
                // Buscar por ID de venta o fecha
                ventas = ventaRepository.findByTiendaIdAndIdContainingIgnoreCase(tiendaId, search, pageable);
            }

            return ResponseEntity.ok(ventas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al cargar ventas: " + e.getMessage()));
        }
    }

    @PostMapping("/procesar")
    public ResponseEntity<?> procesarVenta(@RequestBody Map<String, Object> ventaData) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productosData = (List<Map<String, Object>>) ventaData.get("productos");
            String tiendaId = (String) ventaData.get("tiendaId");

            if (productosData == null || productosData.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No hay productos en la venta"));
            }

            // Validar stock y crear productos vendidos
            List<ProductoVendido> productosVendidos = new java.util.ArrayList<>();
            double total = 0;

            for (Map<String, Object> prodData : productosData) {
                String codigo = (String) prodData.get("codigo");
                String nombre = (String) prodData.get("nombre");
                int cantidad = ((Number) prodData.get("cantidad")).intValue();
                double precio = ((Number) prodData.get("precio")).doubleValue();

                // Buscar producto en inventario
                Productos producto = productosRepository.findByCodigo(codigo);
                if (producto == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Producto no encontrado: " + nombre));
                }

                if (producto.getCantidad() < cantidad) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Stock insuficiente para: " + nombre + 
                                    ". Disponible: " + producto.getCantidad()));
                }

                // Actualizar stock
                producto.setCantidad(producto.getCantidad() - cantidad);
                productosRepository.save(producto);

                // Crear producto vendido
                ProductoVendido prodVendido = new ProductoVendido();
                prodVendido.setCodigo(codigo);
                prodVendido.setNombre(nombre);
                prodVendido.setCantidad(cantidad);
                prodVendido.setPrecio(precio);
                productosVendidos.add(prodVendido);

                total += cantidad * precio;
            }

            // Crear y guardar venta
            Venta venta = new Venta();
            venta.setFecha(new Date());
            venta.setTotal(total);
            venta.setTiendaId(tiendaId);
            venta.setProductos(productosVendidos);

            Venta ventaGuardada = ventaRepository.save(venta);

            return ResponseEntity.ok(Map.of(
                    "message", "Venta procesada exitosamente",
                    "ventaId", ventaGuardada.getId().toString(),
                    "total", total
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar venta: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarVenta(@PathVariable String id) {
        try {
            ventaService.eliminarVenta(new org.bson.types.ObjectId(id));
            return ResponseEntity.ok(Map.of("message", "Venta eliminada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar venta: " + e.getMessage()));
        }
    }

    @GetMapping("/productos/disponibles/{tiendaId}")
    public ResponseEntity<?> obtenerProductosDisponibles(@PathVariable String tiendaId) {
        try {
            List<Productos> productos = productosRepository.findByTiendaIdAndCantidadGreaterThan(tiendaId, 0);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al cargar productos: " + e.getMessage()));
        }
    }
}
*/