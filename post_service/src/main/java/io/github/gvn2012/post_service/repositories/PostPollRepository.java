package io.github.gvn2012.post_service.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.github.gvn2012.post_service.entities.PostPoll;

@Repository
public interface PostPollRepository extends JpaRepository<PostPoll, UUID> {

}
