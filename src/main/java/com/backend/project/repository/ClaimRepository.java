package com.backend.project.repository;

import com.backend.project.model.Claim;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClaimRepository extends MongoRepository<Claim, String> {
}
