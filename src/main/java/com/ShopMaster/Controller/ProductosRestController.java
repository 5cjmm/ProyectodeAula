package com.ShopMaster.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import com.ShopMaster.Model.ProductoConProveedores;
import com.ShopMaster.Model.Productos;
import com.ShopMaster.Service.ProductosService;

@RestController
@RequestMapping("/api/productos")
public class ProductosRestController {

    @Autowired
    private ProductosService productosService;

    // Listar productos con paginación
    @GetMapping("/tienda/{tiendaId}")
    public ResponseEntity<Page<ProductoConProveedores>> listarProductos(
            @PathVariable String tiendaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductoConProveedores> productos = productosService.listarPorTienda(tiendaId, pageable);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Crear producto
    @PostMapping
    public ResponseEntity<String> crearProducto(@RequestBody Productos producto) {
        try {
            // Convertir proveedorIdStrs a ObjectIds
            if (producto.getProveedorIdStrs() != null && !producto.getProveedorIdStrs().isEmpty()) {
                List<ObjectId> proveedorIds = producto.getProveedorIdStrs().stream()
                    .map(ObjectId::new)
                    .collect(Collectors.toList());
                producto.setProveedorIds(proveedorIds);
            }

            productosService.guardarProducto(producto, producto.getTiendaId());
            return ResponseEntity.ok("Producto creado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor");
        }
    }

    // Actualizar producto
    @PutMapping("/{id}")
    public ResponseEntity<String> actualizarProducto(@PathVariable String id, @RequestBody Productos producto) {
        try {
            // Convertir proveedorIdStrs a ObjectIds
            if (producto.getProveedorIdStrs() != null && !producto.getProveedorIdStrs().isEmpty()) {
                List<ObjectId> proveedorIds = producto.getProveedorIdStrs().stream()
                    .map(ObjectId::new)
                    .collect(Collectors.toList());
                producto.setProveedorIds(proveedorIds);
            }

            productosService.actualizarProducto(id, producto);
            return ResponseEntity.ok("Producto actualizado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor");
        }
    }

    // Eliminar producto
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarProducto(@PathVariable String id, @RequestParam String tiendaId) {
        try {
            productosService.eliminarProducto(id, tiendaId);
            return ResponseEntity.ok("Producto eliminado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor");
        }
    }
}
