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
        log.info("Mapped PostEvent: postId={}, post={}", event.getPostId(), event.getPost());
        event.setPostId(post.getId());
        event.setPost(post);
        log.info("Set PostEvent refs: postId={}, post.id={}", event.getPostId(), event.getPost().getId());
        postEventRepository.save(event);
        log.info("Saved PostEvent successfully");
        post.setEvent(event);
    }
}
