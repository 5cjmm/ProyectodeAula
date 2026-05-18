package com.ShopMaster.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ShopMaster.Model.Proveedor;

@Repository
public interface ProveedorRepository extends MongoRepository<Proveedor, String> {

    // ── Con Soft Delete ──────────────────────────────────────────
    List<Proveedor>     findByTiendaIdAndActivoTrue(String tiendaId);
    Page<Proveedor>     findByTiendaIdAndActivoTrue(String tiendaId, Pageable pageable);
    Optional<Proveedor> findByNitAndActivoTrue(String nit);

    boolean existsByNitAndTiendaIdAndActivoTrue(String nit, String tiendaId);
    boolean existsByTelefonoAndTiendaIdAndActivoTrue(String telefono, String tiendaId);
    boolean existsByNitAndTiendaIdAndIdNotAndActivoTrue(String nit, String tiendaId, String id);
    boolean existsByTelefonoAndTiendaIdAndIdNotAndActivoTrue(String telefono, String tiendaId, String id);

    List<Proveedor> findByNombreContainingIgnoreCaseAndTiendaIdAndActivoTrue(String nombre, String tiendaId);

    // ── Legacy (sin soft delete, no usar en vistas nuevas) ───────
    List<Proveedor>     findByTiendaId(String tiendaId);
    Page<Proveedor>     findByTiendaId(String tiendaId, Pageable pageable);
    Optional<Proveedor> findByNit(String nit);

    boolean existsByNitAndTiendaId(String nit, String tiendaId);
    boolean existsByTelefonoAndTiendaId(String telefono, String tiendaId);
    boolean existsByNitAndTiendaIdAndIdNot(String nit, String tiendaId, String id);
    boolean existsByTelefonoAndTiendaIdAndIdNot(String telefono, String tiendaId, String id);

    List<Proveedor> findByNombreContainingIgnoreCaseAndTiendaId(String nombre, String tiendaId);
}
