package com.soumyajit.apigateway.repository;

import com.soumyajit.apigateway.model.ApiLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiLogRepository extends MongoRepository<ApiLog, String> {
}
