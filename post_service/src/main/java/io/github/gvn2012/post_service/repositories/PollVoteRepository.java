package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, UUID> {

    boolean existsByPollPostIdAndUserId(UUID pollPostId, UUID userId);

    boolean existsByPollPostIdAndUserIdAndOptionId(UUID pollPostId, UUID userId, UUID optionId);
}
