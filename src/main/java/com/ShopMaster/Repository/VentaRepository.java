package com.ShopMaster.Repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ShopMaster.Model.Venta;

public interface VentaRepository extends MongoRepository<Venta, String> {
    List<Venta> findByFecha(Date fecha);
    List<Venta> findByFechaBetween(Date inicio, Date fin);

}