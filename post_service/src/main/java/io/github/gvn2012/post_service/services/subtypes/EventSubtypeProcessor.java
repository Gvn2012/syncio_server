package io.github.gvn2012.post_service.services.subtypes;

import io.github.gvn2012.post_service.dtos.mappers.PostEventMapper;
import io.github.gvn2012.post_service.dtos.requests.PostCreateRequest;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostEvent;
import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.repositories.PostEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
        PostEvent event = postEventMapper.toEntity(request.getEvent());
        event.setPostId(post.getId());
        event.setPost(post);
        postEventRepository.save(event);
        post.setEvent(event);
    }
}
