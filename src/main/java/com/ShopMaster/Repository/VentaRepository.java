package com.ShopMaster.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ShopMaster.Model.Venta;

public interface VentaRepository extends MongoRepository<Venta, String> {
    
}