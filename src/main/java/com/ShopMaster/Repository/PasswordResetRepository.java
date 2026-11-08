package com.ShopMaster.Repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ShopMaster.Model.PasswordReset;

@Repository
public interface PasswordResetRepository extends MongoRepository<PasswordReset, String> {

    Optional<PasswordReset> findByEmail(String email);

    void deleteByEmail(String email);
}
