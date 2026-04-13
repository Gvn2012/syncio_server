package io.github.gvn2012.relationship_service.repositories;

import io.github.gvn2012.relationship_service.entities.FriendRequest;
import io.github.gvn2012.relationship_service.entities.enums.FriendRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {

    Optional<FriendRequest> findBySenderUserIdAndReceiverUserIdAndStatus(
            UUID senderUserId, UUID receiverUserId, FriendRequestStatus status);

    List<FriendRequest> findAllByReceiverUserIdAndStatus(UUID receiverUserId, FriendRequestStatus status);

    List<FriendRequest> findAllBySenderUserIdAndStatus(UUID senderUserId, FriendRequestStatus status);

    boolean existsBySenderUserIdAndReceiverUserIdAndStatus(UUID senderUserId, UUID receiverUserId, FriendRequestStatus status);

    Page<FriendRequest> findAllByReceiverUserIdAndStatus(UUID receiverUserId, FriendRequestStatus status, Pageable pageable);

    Page<FriendRequest> findAllBySenderUserIdAndStatus(UUID senderUserId, FriendRequestStatus status, Pageable pageable);

    @Query("""
            select fr from FriendRequest fr
            where fr.status = :status
              and (fr.senderUserId = :userId or fr.receiverUserId = :userId)
            """)
    Page<FriendRequest> findAllPendingByUserId(@Param("userId") UUID userId,
                                               @Param("status") FriendRequestStatus status,
                                               Pageable pageable);

}
