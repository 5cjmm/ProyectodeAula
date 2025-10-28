package com.ShopMaster.Repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ShopMaster.Model.Proveedor;

@Repository
public interface ProveedorRepository extends MongoRepository<Proveedor, String> {
    boolean existsByNitAndTiendaId(String nit, String tiendaId);
    boolean existsByTelefonoAndTiendaId(String telefono, String tiendaId);

    boolean existsByNitAndTiendaIdAndIdNot(String nit, String tiendaId, String id);
    boolean existsByTelefonoAndTiendaIdAndIdNot(String telefono, String tiendaId, String id);
    Page<Proveedor> findByTiendaId(String tiendaId, Pageable pageable);
    List<Proveedor> findByTiendaId(String tiendaId);
    List<Proveedor> findByNombreContainingIgnoreCaseAndTiendaId(String nombre, String tiendaId);
}