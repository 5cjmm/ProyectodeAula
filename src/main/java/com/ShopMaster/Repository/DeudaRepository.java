package com.ShopMaster.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ShopMaster.Model.Deuda;
@Repository
public interface DeudaRepository extends MongoRepository<Deuda, String> {
    Page<Deuda> findByTiendaId(String tiendaId, Pageable pageable);
}
    
