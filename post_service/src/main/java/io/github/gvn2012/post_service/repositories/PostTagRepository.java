package io.github.gvn2012.post_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.github.gvn2012.post_service.entities.PostTag;
import io.github.gvn2012.post_service.entities.composite_keys.PostTagId;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {

}
