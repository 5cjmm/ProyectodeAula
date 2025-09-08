package com.ShopMaster.Service;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.Proveedor;
import com.ShopMaster.Repository.ProveedorRepository;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    // Crear proveedor asociado a una tienda
    public Proveedor guardarProveedor(Proveedor proveedor) {
        if (proveedorRepository.existsByNitAndTiendaId(proveedor.getNit(), proveedor.getTiendaId())) {
            throw new RuntimeException("Ya existe un proveedor con ese NIT en esta tienda");
        }

        // Validar Teléfono único por tienda
        if (proveedorRepository.existsByTelefonoAndTiendaId(proveedor.getTelefono(), proveedor.getTiendaId())) {
            throw new RuntimeException("Ya existe un proveedor con ese teléfono en esta tienda");
        }
        return proveedorRepository.save(proveedor);
    }

    public List<Proveedor> obtenerProveedoresPorTienda(String tiendaId) {
        return proveedorRepository.findByTiendaId(tiendaId);
    }

    // Listar proveedores de una tienda
    public Page<Proveedor> obtenerProveedoresPorTienda(String tiendaId, int page, int size) {
        return proveedorRepository.findByTiendaId(tiendaId, PageRequest.of(page, size));
    }
    // Actualizar proveedor asegurando que pertenece a la tienda
    public Proveedor actualizarProveedor(String id, Proveedor proveedor) {
        Proveedor existente = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        if (proveedorRepository.existsByNitAndTiendaIdAndIdNot(proveedor.getNit(), proveedor.getTiendaId(), proveedor.getId())) {
            throw new RuntimeException("El NIT ya está en uso por otro proveedor en esta tienda");
        }

        if (proveedorRepository.existsByTelefonoAndTiendaIdAndIdNot(proveedor.getTelefono(), proveedor.getTiendaId(), proveedor.getId())) {
            throw new RuntimeException("El Teléfono ya está en uso por otro proveedor en esta tienda");
        }

        // mantenemos el mismo id y tiendaId
        proveedor.setId(id);
        proveedor.setTiendaId(existente.getTiendaId());
        return proveedorRepository.save(proveedor);
    }

    // Eliminar proveedor asegurando que pertenece a la tienda
    public void eliminarProveedor(String id, String tiendaId) {
        Proveedor existente = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        if (!existente.getTiendaId().equals(tiendaId)) {
            throw new RuntimeException("No tienes permiso para eliminar este proveedor");
        }

        proveedorRepository.deleteById(id);
    }
}
