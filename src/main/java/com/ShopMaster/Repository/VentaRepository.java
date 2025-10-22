package com.ShopMaster.Repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ShopMaster.Model.Venta;
    @Repository
public interface VentaRepository extends MongoRepository<Venta, String> {
    Page<Venta> findByTiendaId(String tiendaId, Pageable pageable);
    List<Venta> findByTiendaIdAndFechaBetween(String tiendaId, Date inicio, Date fin);
}


