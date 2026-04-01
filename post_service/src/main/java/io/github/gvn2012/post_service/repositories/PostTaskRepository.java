package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PostTask;
import io.github.gvn2012.post_service.entities.enums.TaskStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostTaskRepository extends JpaRepository<PostTask, UUID> {

    List<PostTask> findByStatus(TaskStatus status, Pageable pageable);

    @Query("SELECT t FROM PostTask t JOIN PostTaskAssignee a ON a.task = t WHERE a.userId = :userId")
    List<PostTask> findByAssigneeUserId(@Param("userId") UUID userId, Pageable pageable);
}
