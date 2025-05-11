package com.ShopMaster.Repository;

import com.ShopMaster.Model.Deuda;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DeudaRepository extends MongoRepository<Deuda, String> {
    List<Deuda> findByCedulaCliente(String cedulaCliente);
    List<Deuda> findByEstado(String estado);
}
