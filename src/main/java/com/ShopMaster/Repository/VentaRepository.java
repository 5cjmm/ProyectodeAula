package com.ShopMaster.Repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.ShopMaster.Model.Venta;
    @Repository
public interface VentaRepository extends MongoRepository<Venta, String> {
    
    // Buscar ventas por tienda con paginación
    Page<Venta> findByTiendaId(String tiendaId, Pageable pageable);
    
    // Buscar ventas por tienda y rango de fechas
    @Query("{'tiendaId': ?0, 'fecha': {$gte: ?1, $lte: ?2}}")
    Page<Venta> findByTiendaIdAndFechaBetween(String tiendaId, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    
    // Buscar ventas por tienda y total mayor a
    @Query("{'tiendaId': ?0, 'total': {$gte: ?1}}")
    Page<Venta> findByTiendaIdAndTotalGreaterThanEqual(String tiendaId, Double total, Pageable pageable);
    
    // Contar ventas por tienda
    long countByTiendaId(String tiendaId);
    
    // Buscar ventas recientes por tienda
    @Query("{'tiendaId': ?0}")
    Page<Venta> findByTiendaIdOrderByFechaDesc(String tiendaId, Pageable pageable);

    Page<Venta> findByTiendaIdAndProductosNombreContainingIgnoreCase(String tiendaId, String buscar, Pageable pageable);
}
