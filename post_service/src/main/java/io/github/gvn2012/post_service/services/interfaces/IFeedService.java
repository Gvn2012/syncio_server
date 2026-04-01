package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.entities.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface IFeedService {
    List<Post> getHybridFeed(UUID recipientId, LocalDateTime cursor, int limit);
    List<Post> getTrendingPosts(LocalDateTime since, int limit);
    List<Post> getFollowingFeed(UUID recipientId, LocalDateTime cursor, int limit);
}
