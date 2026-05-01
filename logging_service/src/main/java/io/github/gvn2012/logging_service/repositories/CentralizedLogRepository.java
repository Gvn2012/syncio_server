package io.github.gvn2012.logging_service.repositories;

import io.github.gvn2012.logging_service.entities.CentralizedLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CentralizedLogRepository extends MongoRepository<CentralizedLog, String> {
}
