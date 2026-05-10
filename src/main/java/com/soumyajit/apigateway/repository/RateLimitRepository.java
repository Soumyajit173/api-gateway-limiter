package com.soumyajit.apigateway.repository;

import com.soumyajit.apigateway.model.RateLimitCounter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RateLimitRepository extends MongoRepository<RateLimitCounter, String> {
    Optional<RateLimitCounter> findByKey(String key);
    boolean existsByKey(String key);
}
