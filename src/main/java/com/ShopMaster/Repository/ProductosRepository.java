package com.ShopMaster.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ShopMaster.Model.Productos;

public interface ProductosRepository extends MongoRepository<Productos, String> {
    List<Productos> findAll();
    Optional<Productos> findByCodigo(int codigo);

}