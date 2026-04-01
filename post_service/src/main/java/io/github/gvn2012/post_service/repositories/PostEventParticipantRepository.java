package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PostEventParticipant;
import io.github.gvn2012.post_service.entities.enums.EventParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostEventParticipantRepository extends JpaRepository<PostEventParticipant, UUID> {

    Optional<PostEventParticipant> findByEventPostIdAndUserId(UUID eventPostId, UUID userId);
    
    List<PostEventParticipant> findByEventPostId(UUID eventPostId);
    
    long countByEventPostIdAndStatus(UUID eventPostId, EventParticipantStatus status);
}
