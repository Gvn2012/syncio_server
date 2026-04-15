package io.github.gvn2012.post_service.services.subtypes;

import io.github.gvn2012.post_service.dtos.mappers.PostEventMapper;
import io.github.gvn2012.post_service.dtos.requests.PostCreateRequest;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostEvent;
import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.repositories.PostEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSubtypeProcessor implements PostSubtypeProcessor {

    private final PostEventMapper postEventMapper;
    private final PostEventRepository postEventRepository;

    @Override
    public PostCategory supportedCategory() {
        return PostCategory.EVENT;
    }

    @Override
    public void process(Post post, PostCreateRequest request) {
        if (request.getEvent() == null) return;
        log.info("Processing EVENT subtype for post id={}", post.getId());
        PostEvent event = postEventMapper.toEntity(request.getEvent());
        event.setPost(post);
        PostEvent saved = postEventRepository.save(event);
        post.setEvent(saved);
        log.info("Saved PostEvent successfully, derived postId={}", saved.getPostId());
    }
}
