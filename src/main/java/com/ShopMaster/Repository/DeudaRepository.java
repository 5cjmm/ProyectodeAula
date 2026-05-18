package com.ShopMaster.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ShopMaster.Model.Deuda;
@Repository
public interface DeudaRepository extends MongoRepository<Deuda, String> {

    // ── Consultas con Soft Delete (usar estas en todos los Services) ──
    List<Deuda>     findByTiendaIdAndActivoTrue(String tiendaId);
    List<Deuda>     findByTiendaIdAndFechaVentaBetweenAndActivoTrue(
            String tiendaId, LocalDateTime inicio, LocalDateTime fin);
    Optional<Deuda> findByCedulaClienteAndTiendaIdAndActivoTrue(String cedulaCliente, String tiendaId);
    Optional<Deuda> findByCedulaClienteAndActivoTrue(String cedulaCliente);
    List<Deuda>     findTop5ByTiendaIdAndActivoTrueOrderByFechaVentaDesc(String tiendaId);
    long            countByTiendaIdAndEstadoNotIgnoreCaseAndActivoTrue(String tiendaId, String estado);

    // ── Métodos legacy (mantener para compatibilidad) ──
    List<Deuda>     findByTiendaId(String tiendaId);
    List<Deuda>     findByTiendaIdAndFechaVentaBetween(String tiendaId, LocalDateTime inicio, LocalDateTime fin);
    Optional<Deuda> findByCedulaClienteAndTiendaId(String cedulaCliente, String tiendaId);
    Optional<Deuda> findByCedulaCliente(String cedulaCliente);
    List<Deuda>     findTop5ByTiendaIdOrderByFechaVentaDesc(String tiendaId);
    long            countByTiendaIdAndEstadoNotIgnoreCase(String tiendaId, String estado);
}

//    List<Deuda> findByTiendaId(String tiendaId);
//    List<Deuda> findByTiendaIdAndFechaVentaBetween(String tiendaId, LocalDateTime inicio, LocalDateTime fin);
//    Optional<Deuda> findByCedulaClienteAndTiendaId(String cedulaCliente, String tiendaId);
//    Optional<Deuda> findByCedulaCliente(String cedulaCliente);
//
//    // Últimas 5 deudas por fecha de venta (desc)
//    List<Deuda> findTop5ByTiendaIdOrderByFechaVentaDesc(String tiendaId);
//
//    // Conteo de deudas activas (estado != PAGADA)
//    long countByTiendaIdAndEstadoNotIgnoreCase(String tiendaId, String estado);
//}
    
