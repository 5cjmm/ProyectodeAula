package com.ShopMaster.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ShopMaster.Model.Deuda;
@Repository
public interface DeudaRepository extends MongoRepository<Deuda, String> {
    Page<Deuda> findByTiendaId(String tiendaId, Pageable pageable);
    List<Deuda> findByTiendaIdAndFechaVentaBetween(String tiendaId, LocalDateTime inicio, LocalDateTime fin);

    // Últimas 5 deudas por fecha de venta (desc)
    List<Deuda> findTop5ByTiendaIdOrderByFechaVentaDesc(String tiendaId);

    // Conteo de deudas activas (estado != PAGADA)
    long countByTiendaIdAndEstadoNotIgnoreCase(String tiendaId, String estado);
}
    
