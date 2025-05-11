package com.ShopMaster.Repository;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.ShopMaster.Model.Productos;

public interface ProductosRepository extends MongoRepository<Productos, ObjectId> {

    List<Productos> findByNombreContainingIgnoreCase(String nombre);

    Productos findByNombre(String nombreProducto);

    Optional<Productos> findByCodigo(String codigo);

}