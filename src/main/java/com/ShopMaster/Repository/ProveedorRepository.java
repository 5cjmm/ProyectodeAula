package com.ShopMaster.Repository;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.ShopMaster.Model.Proveedor;

public interface ProveedorRepository extends MongoRepository<Proveedor, ObjectId> {
    
}
