package com.ShopMaster.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ShopMaster.Model.PdfCounter;

public interface PdfCounterRepository extends MongoRepository<PdfCounter, String> {}
