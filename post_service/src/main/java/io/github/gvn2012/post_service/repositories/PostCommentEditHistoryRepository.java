package io.github.gvn2012.post_service.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.github.gvn2012.post_service.entities.PostCommentEditHistory;

@Repository
public interface PostCommentEditHistoryRepository extends JpaRepository<PostCommentEditHistory, UUID> {

}
