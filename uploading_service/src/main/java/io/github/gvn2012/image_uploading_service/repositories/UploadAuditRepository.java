package io.github.gvn2012.image_uploading_service.repositories;

import io.github.gvn2012.image_uploading_service.models.UploadAudit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UploadAuditRepository extends MongoRepository<UploadAudit, String> {
    Optional<UploadAudit> findByImageId(String imageId);
}
