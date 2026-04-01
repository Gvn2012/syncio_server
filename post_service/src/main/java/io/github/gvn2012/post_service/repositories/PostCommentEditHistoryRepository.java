package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PostCommentEditHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostCommentEditHistoryRepository extends JpaRepository<PostCommentEditHistory, UUID> {

}
