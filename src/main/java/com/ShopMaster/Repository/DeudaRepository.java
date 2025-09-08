package com.ShopMaster.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.ShopMaster.Model.Deuda;

public interface DeudaRepository extends MongoRepository<Deuda, String> {
    // Buscar deudas por tienda con paginación
    Page<Deuda> findByTiendaId(String tiendaId, Pageable pageable);
    
    // Buscar por tienda y estado
    Page<Deuda> findByTiendaIdAndEstado(String tiendaId, String estado, Pageable pageable);
    
    // Buscar por tienda y nombre de cliente (contiene texto)
    @Query("{'tiendaId': ?0, 'nombreCliente': {$regex: ?1, $options: 'i'}}")
    Page<Deuda> findByTiendaIdAndNombreClienteContainingIgnoreCase(String tiendaId, String nombreCliente, Pageable pageable);
    
    // Buscar por tienda, estado y nombre de cliente
    @Query("{'tiendaId': ?0, 'estado': ?1, 'nombreCliente': {$regex: ?2, $options: 'i'}}")
    Page<Deuda> findByTiendaIdAndEstadoAndNombreClienteContainingIgnoreCase(String tiendaId, String estado, String nombreCliente, Pageable pageable);
    
    // Buscar por tienda y fecha
    @Query("{'tiendaId': ?0, 'fechaVenta': {$gte: ?1, $lte: ?2}}")
    Page<Deuda> findByTiendaIdAndFechaVentaBetween(String tiendaId, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    
    // Buscar por cédula del cliente
    List<Deuda> findByCedulaClienteAndTiendaId(String cedulaCliente, String tiendaId);
    
    // Contar deudas por estado y tienda
    long countByTiendaIdAndEstado(String tiendaId, String estado);

    Page<Deuda> findByTiendaIdAndNombreClienteContainingIgnoreCaseAndEstado(String tiendaId, String buscar,
            String estado, Pageable pageable);
}
    
