package io.github.gvn2012.relationship_service.repositories;

import io.github.gvn2012.relationship_service.entities.UserFriend;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserFriendRepository extends JpaRepository<UserFriend, UUID> {

    Optional<UserFriend> findByUser1IdAndUser2Id(UUID user1Id, UUID user2Id);

    @Query("""
            select uf from UserFriend uf
            where (uf.user1Id = :userId or uf.user2Id = :userId)
              and uf.status = :status
            """)
    List<UserFriend> findAllByUserIdAndStatus(@Param("userId") UUID userId,
                                              @Param("status") RelationshipStatus status);

    @Query("""
            select uf from UserFriend uf
            where (uf.user1Id = :userId or uf.user2Id = :userId)
              and uf.status = :status
            """)
    Page<UserFriend> findAllByUserIdAndStatus(@Param("userId") UUID userId,
                                              @Param("status") RelationshipStatus status,
                                              Pageable pageable);

    @Query("""
            select count(uf) from UserFriend uf
            where (uf.user1Id = :userId or uf.user2Id = :userId)
              and uf.status = :status
            """)
    long countAllByUserIdAndStatus(@Param("userId") UUID userId,
                                   @Param("status") RelationshipStatus status);
}
