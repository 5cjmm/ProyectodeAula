package com.ShopMaster.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ShopMaster.Model.Deuda;

public interface DeudaRepository extends MongoRepository<Deuda, String> {
    List<Deuda> findByCedulaCliente(String cedulaCliente);
    List<Deuda> findByEstado(String estado);
    List<Deuda> findByFechaVentaBetween(LocalDateTime  inicio, LocalDateTime  fin);
}
