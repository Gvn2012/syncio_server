package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PostContentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostContentVersionRepository extends JpaRepository<PostContentVersion, UUID> {
    List<PostContentVersion> findByPostIdOrderByVersionNumberDesc(UUID postId);
    List<PostContentVersion> findByPostIdOrderByVersionNumberAsc(UUID postId);
}
