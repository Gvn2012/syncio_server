package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PostMediaAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostMediaAttachmentRepository extends JpaRepository<PostMediaAttachment, UUID> {
    void deleteByPostId(UUID postId);
}
