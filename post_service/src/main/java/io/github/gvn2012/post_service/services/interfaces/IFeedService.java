package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.dtos.responses.PostResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface IFeedService {
    List<PostResponse> getHybridFeed(UUID recipientId, LocalDateTime cursor, int limit);
    List<PostResponse> getTrendingPosts(UUID viewerId, LocalDateTime since, int limit);
    List<PostResponse> getFollowingFeed(UUID recipientId, LocalDateTime cursor, int limit);
}
