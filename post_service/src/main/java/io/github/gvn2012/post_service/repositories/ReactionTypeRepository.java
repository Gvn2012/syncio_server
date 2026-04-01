package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactionTypeRepository extends JpaRepository<ReactionType, Short> {

}
