package io.github.gvn2012.post_service.services.subtypes;

import io.github.gvn2012.post_service.dtos.requests.PostCreateRequest;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.enums.PostCategory;

public interface PostSubtypeProcessor {
    PostCategory supportedCategory();
    void process(Post post, PostCreateRequest request);
}
