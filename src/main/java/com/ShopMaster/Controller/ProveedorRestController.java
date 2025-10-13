package com.ShopMaster.Controller;

import org.springframework.data.domain.Page;
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

import com.ShopMaster.Model.Proveedor;
import com.ShopMaster.Service.ProveedorService;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorRestController {

    private final ProveedorService proveedorService;

    public ProveedorRestController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    // 🔹 Crear proveedor (solo admin o tendero)
    @PostMapping("/crear")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public Proveedor crearProveedor(@RequestBody Proveedor proveedor) {
        // ⚠️ NO reemplazar tiendaId con principal.getName()
        if (proveedor.getTiendaId() == null) {
            throw new RuntimeException("Debe especificar una tienda");
        }
        return proveedorService.guardarProveedor(proveedor);
    }

    // 🔹 Actualizar proveedor
    @PutMapping("/actualizar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public Proveedor actualizarProveedor(@PathVariable String id, @RequestBody Proveedor proveedor) {
        proveedor.setId(id); // usar el String id
        return proveedorService.actualizarProveedor(id, proveedor);
    }

    // 🔹 Eliminar proveedor
    @DeleteMapping("/eliminar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public void eliminarProveedor(@PathVariable String id, @RequestParam String tiendaId) {
        proveedorService.eliminarProveedor(id, tiendaId);
    }

    // 🔹 Listar proveedores por tienda
    @GetMapping("/tienda/{tiendaId}")
    @PreAuthorize("hasAnyRole('ADMIN','TENDERO')")
    public Page<Proveedor> listarPorTienda(@PathVariable String tiendaId, @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size) {
        return proveedorService.obtenerProveedoresPorTienda(tiendaId, page, size);
    }

    
}


