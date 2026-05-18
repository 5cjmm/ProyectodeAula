package com.ShopMaster.Service;

import java.util.List;

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

    // ── Crear ────────────────────────────────────────────────────
    public Proveedor guardarProveedor(Proveedor proveedor) {
        if (proveedorRepository.existsByNitAndTiendaIdAndActivoTrue(
                proveedor.getNit(), proveedor.getTiendaId())) {
            throw new RuntimeException("Ya existe un proveedor con ese NIT en esta tienda");
        }
        if (proveedorRepository.existsByTelefonoAndTiendaIdAndActivoTrue(
                proveedor.getTelefono(), proveedor.getTiendaId())) {
            throw new RuntimeException("Ya existe un proveedor con ese teléfono en esta tienda");
        }
        proveedor.setActivo(true);
        return proveedorRepository.save(proveedor);
    }

    // ── Listar ───────────────────────────────────────────────────
    public List<Proveedor> obtenerProveedoresPorTienda(String tiendaId) {
        return proveedorRepository.findByTiendaIdAndActivoTrue(tiendaId);
    }

    public Page<Proveedor> obtenerProveedoresPorTienda(String tiendaId, int page, int size) {
        return proveedorRepository.findByTiendaIdAndActivoTrue(tiendaId, PageRequest.of(page, size));
    }

    public List<Proveedor> buscarPorNombre(String tiendaId, String nombre) {
        return proveedorRepository.findByNombreContainingIgnoreCaseAndTiendaIdAndActivoTrue(nombre, tiendaId);
    }

    // ── Actualizar ───────────────────────────────────────────────
    public Proveedor actualizarProveedor(String id, Proveedor proveedor) {
        Proveedor existente = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        if (proveedorRepository.existsByNitAndTiendaIdAndIdNotAndActivoTrue(
                proveedor.getNit(), existente.getTiendaId(), id)) {
            throw new RuntimeException("El NIT ya está en uso por otro proveedor en esta tienda");
        }
        if (proveedorRepository.existsByTelefonoAndTiendaIdAndIdNotAndActivoTrue(
                proveedor.getTelefono(), existente.getTiendaId(), id)) {
            throw new RuntimeException("El teléfono ya está en uso por otro proveedor en esta tienda");
        }

        proveedor.setId(id);
        proveedor.setTiendaId(existente.getTiendaId());
        proveedor.setActivo(true);
        return proveedorRepository.save(proveedor);
    }

    // ── Eliminar (Soft Delete) ───────────────────────────────────
    public void eliminarProveedor(String id, String tiendaId) {
        Proveedor p = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + id));
        p.setActivo(false);
        proveedorRepository.save(p);
    }
}
