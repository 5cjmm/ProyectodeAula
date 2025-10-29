package com.ShopMaster.Repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ShopMaster.Model.Venta;

@Repository
public interface VentaRepository extends MongoRepository<Venta, String> {
    Page<Venta> findByTiendaId(String tiendaId, Pageable pageable);
    List<Venta> findByTiendaIdAndFechaBetween(String tiendaId, Date inicio, Date fin);
    Page<Venta> findByTiendaIdAndFechaBetween(String tiendaId, Date inicio, Date fin, Pageable pageable);

    // Optimizado: sumar total de ventas por tienda vía aggregation (sin límite de página)
    @Aggregation(pipeline = {
            "{ $match: { tiendaId: ?0 } }",
            "{ $group: { _id: null, total: { $sum: '$total' } } }",
            "{ $project: { _id: 0, total: 1 } }"
    })
    Double sumTotalByTiendaId(String tiendaId);

    // Conteo de ventas en un rango de fechas
    long countByTiendaIdAndFechaBetween(String tiendaId, Date inicio, Date fin);

    // Últimas 5 ventas por fecha
    List<Venta> findTop5ByTiendaIdOrderByFechaDesc(String tiendaId);
}


