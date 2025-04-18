package com.ShopMaster.Repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.ShopMaster.Model.Proveedores;

public interface ProveedorRepository extends MongoRepository<Proveedores, String> {
    List<Proveedores> findAll();
}
