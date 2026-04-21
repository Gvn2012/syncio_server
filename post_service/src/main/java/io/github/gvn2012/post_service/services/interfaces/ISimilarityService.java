package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.dtos.responses.PostResponse;
import java.util.List;
import java.util.UUID;

public interface ISimilarityService {

    List<PostResponse> findSimilarPosts(UUID postId, int limit);

    boolean isDuplicate(String content);
}
