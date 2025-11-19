package com.ShopMaster.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @GetMapping("/tienda/{tiendaId}/todos")
    public ResponseEntity<List<ProductoConProveedores>> listarTodosProductos(@PathVariable String tiendaId) {
        try {
            List<ProductoConProveedores> productos = productosService.listarTodosPorTienda(tiendaId);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/crear")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public Productos crearProducto(@RequestBody Productos producto) {
        // ⚠️ NO reemplazar tiendaId con principal.getName()
       if (producto.getProveedorIdStrs() != null && !producto.getProveedorIdStrs().isEmpty()) {
                List<ObjectId> proveedorIds = producto.getProveedorIdStrs().stream()
                    .map(ObjectId::new)
                    .collect(Collectors.toList());
                producto.setProveedorIds(proveedorIds);
            }
        return productosService.guardarProducto(producto, producto.getTiendaId());
    }     
    
    @PutMapping("/actualizar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public Productos actualizarProducto(@PathVariable String id, @RequestBody Productos producto) {
        if (producto.getProveedorIdStrs() != null && !producto.getProveedorIdStrs().isEmpty()) {
                List<ObjectId> proveedorIds = producto.getProveedorIdStrs().stream()
                    .map(ObjectId::new)
                    .collect(Collectors.toList());
                producto.setProveedorIds(proveedorIds);
            }
        return productosService.actualizarProducto(id, producto);
    }


    @DeleteMapping("/eliminar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public void eliminarProducto(@PathVariable String id, @RequestParam String tiendaId) {
        productosService.eliminarProducto(id, tiendaId);
    }

}