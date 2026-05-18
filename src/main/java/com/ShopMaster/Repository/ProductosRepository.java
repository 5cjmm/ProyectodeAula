package com.ShopMaster.Repository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.ShopMaster.Model.Productos;

public interface ProductosRepository extends MongoRepository<Productos, String> {
    // ── Consultas originales (NO usar en vistas, ignoran soft delete) ──
    List<Productos> findByTiendaId(String tiendaId);

    // ── Consultas con Soft Delete (usar estas en todos los Services) ──
    List<Productos>  findByTiendaIdAndActivoTrue(String tiendaId);
    Page<Productos>  findByTiendaIdAndActivoTrue(String tiendaId, Pageable pageable);

    List<Productos>  findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
    Productos        findByNombreAndActivoTrue(String nombre);
    List<Productos>  findByCantidadGreaterThanAndActivoTrue(int cantidad);
    List<Productos>  findByTiendaIdAndCantidadGreaterThanAndActivoTrue(String tiendaId, int cantidad);

    Page<Productos>  findByTiendaIdAndCantidadLessThanEqualAndActivoTrue(
            String tiendaId, int cantidad, Pageable pageable);

    boolean existsByCodigoAndTiendaIdAndActivoTrue(String codigo, String tiendaId);
    boolean existsByCodigoAndTiendaIdAndIdNotAndActivoTrue(String codigo, String tiendaId, String id);
    Productos findByCodigoAndActivoTrue(String codigo);

    // Para el chatbot (bajo stock activos)
    List<Productos> findByTiendaIdAndActivoTrue(String tiendaId, org.springframework.data.domain.Sort sort);

    // Métodos legacy (mantener para compatibilidad con código existente)
    List<Productos>  findByNombreContainingIgnoreCase(String nombre);
    Productos        findByNombre(String nombreProducto);
    List<Productos>  findByCantidadGreaterThan(int cantidad);
    boolean          existsByCodigoAndTiendaId(String codigo, String tiendaId);
    boolean          existsByCodigoAndTiendaIdAndIdNot(String codigo, String tiendaId, String id);
    Productos        findByCodigo(String codigo);
    Page<Productos>  findByTiendaId(String tiendaId, Pageable pageable);
    List<Productos>  findByTiendaIdAndCantidadGreaterThan(String tiendaId, int i);
    Page<Productos>  findByTiendaIdAndCantidadLessThanEqual(String tiendaId, int cantidad, Pageable pageable);
}

//    List<Productos> findByTiendaId(String tiendaId);
//    List<Productos> findByNombreContainingIgnoreCase(String nombre);
//
//    Productos findByNombre(String nombreProducto);
//    List<Productos> findByCantidadGreaterThan(int cantidad);
//
//    boolean existsByCodigoAndTiendaId(String codigo, String tiendaId);
//
//    boolean existsByCodigoAndTiendaIdAndIdNot(String codigo, String tiendaId, String id);
//    Productos findByCodigo(String codigo);
//    Page<Productos> findByTiendaId(String tiendaId, Pageable pageable);
//    List<Productos> findByTiendaIdAndCantidadGreaterThan(String tiendaId, int i);
//
//    // Nueva consulta paginada para productos con cantidad menor o igual a un umbral (ej: stock bajo)
//    Page<Productos> findByTiendaIdAndCantidadLessThanEqual(String tiendaId, int cantidad, Pageable pageable);
//
//}