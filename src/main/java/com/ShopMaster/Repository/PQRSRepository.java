package com.ShopMaster.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ShopMaster.Model.PQRS;

public interface PQRSRepository extends MongoRepository<PQRS, String> {
}