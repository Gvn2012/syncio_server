package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PostTaskAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostTaskAssigneeRepository extends JpaRepository<PostTaskAssignee, UUID> {

    boolean existsByTaskIdAndUserId(UUID taskId, UUID userId);

    void deleteByTaskIdAndUserId(UUID taskId, UUID userId);
}
