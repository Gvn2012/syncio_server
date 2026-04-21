package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.BannedWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BannedWordRepository extends JpaRepository<BannedWord, UUID> {
    Optional<BannedWord> findByWord(String word);
    boolean existsByWord(String word);
}
