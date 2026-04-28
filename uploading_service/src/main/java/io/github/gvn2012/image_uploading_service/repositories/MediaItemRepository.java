package io.github.gvn2012.image_uploading_service.repositories;

import io.github.gvn2012.image_uploading_service.models.MediaItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaItemRepository extends MongoRepository<MediaItem, String> {
}
