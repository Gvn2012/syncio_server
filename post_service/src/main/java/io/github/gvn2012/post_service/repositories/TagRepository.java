package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {

    java.util.Optional<Tag> findByName(String name);
}
