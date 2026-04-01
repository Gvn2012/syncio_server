package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.FeedItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FeedItemRepository extends JpaRepository<FeedItem, UUID> {

    @Query("SELECT f FROM FeedItem f WHERE f.recipientId = :recipientId AND f.isHidden = false ORDER BY f.weightScore DESC, f.createdAt DESC")
    List<FeedItem> findByRecipientIdOrderByWeightScoreDescCreatedAtDesc(
            @Param("recipientId") UUID recipientId,
            Pageable pageable
    );

    void deleteBySourcePostId(UUID postId);

    boolean existsBySourcePostIdAndRecipientId(UUID sourcePostId, UUID recipientId);

    @Modifying
    @Query("UPDATE FeedItem f SET f.isRead = true WHERE f.id = :feedItemId")
    void markAsRead(@Param("feedItemId") UUID feedItemId);
}
