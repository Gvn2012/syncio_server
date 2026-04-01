package io.github.gvn2012.post_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.github.gvn2012.post_service.entities.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

}
