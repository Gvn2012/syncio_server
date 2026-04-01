package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PostPoll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface PostPollRepository extends JpaRepository<PostPoll, UUID> {

    @Modifying
    @Transactional
    @Query("UPDATE PostPoll p SET p.isClosed = true WHERE p.isClosed = false AND p.expiresAt IS NOT NULL AND p.expiresAt < :now")
    void closeExpired(@Param("now") LocalDateTime now);
}
