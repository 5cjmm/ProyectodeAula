package com.ShopMaster.Repository;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.ShopMaster.Model.Productos;
import com.ShopMaster.Model.Proveedor;

public interface ProductosRepository extends MongoRepository<Productos, String> {
    List<Productos> findByTiendaId(String tiendaId);
    List<Productos> findByNombreContainingIgnoreCase(String nombre);

    Productos findByNombre(String nombreProducto);
    List<Productos> findByCantidadGreaterThan(int cantidad);

    boolean existsByCodigoAndTiendaId(String codigo, String tiendaId);

    boolean existsByCodigoAndTiendaIdAndIdNot(String codigo, String tiendaId, String id);
    Optional<Productos> findByCodigo(String codigo);
    Page<Productos> findByTiendaId(String tiendaId, Pageable pageable);

}