package io.github.gvn2012.search_service.services;

import io.github.gvn2012.search_service.documents.PostIndex;
import io.github.gvn2012.search_service.documents.UserIndex;
import io.github.gvn2012.search_service.repositories.PostSearchRepository;
import io.github.gvn2012.search_service.repositories.UserSearchRepository;
import io.github.gvn2012.shared.kafka_events.PostSearchEvent;
import io.github.gvn2012.shared.kafka_events.UserSearchEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchIndexingConsumer {

    private final UserSearchRepository userSearchRepository;
    private final PostSearchRepository postSearchRepository;

    @KafkaListener(topics = "user-search-indexing", groupId = "search-service-group")
    public void consumeUserIndexing(UserSearchEvent event) {
        log.info("Received UserSearchEvent: {} for user: {}", event.getOperationType(), event.getUserId());
        
        if (event.getOperationType() == UserSearchEvent.OperationType.DELETE) {
            userSearchRepository.deleteById(event.getUserId().toString());
            log.debug("Deleted user index for ID: {}", event.getUserId());
        } else {
            UserIndex document = UserIndex.builder()
                    .id(event.getUserId().toString())
                    .username(event.getUsername())
                    .fullName(event.getFullName())
                    .avatarUrl(event.getAvatarUrl())
                    .build();
            userSearchRepository.save(document);
            log.debug("Upserted user index for ID: {}", event.getUserId());
        }
    }

    @KafkaListener(topics = "post-search-indexing", groupId = "search-service-group")
    public void consumePostIndexing(PostSearchEvent event) {
        log.info("Received PostSearchEvent: {} for post: {}", event.getOperationType(), event.getPostId());

        if (event.getOperationType() == PostSearchEvent.OperationType.DELETE) {
            postSearchRepository.deleteById(event.getPostId().toString());
            log.debug("Deleted post index for ID: {}", event.getPostId());
        } else {
            // Only index active/published posts usually, but here we follow the event status
            if ("DELETED".equals(event.getStatus())) {
                postSearchRepository.deleteById(event.getPostId().toString());
                return;
            }

            PostIndex document = PostIndex.builder()
                    .id(event.getPostId().toString())
                    .authorId(event.getAuthorId().toString())
                    .content(event.getContent())
                    .publishedAt(event.getPublishedAt())
                    .status(event.getStatus())
                    .build();
            postSearchRepository.save(document);
            log.debug("Upserted post index for ID: {}", event.getPostId());
        }
    }
}
