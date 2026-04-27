package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.dtos.requests.PostCreateRequest;
import io.github.gvn2012.post_service.dtos.requests.PostUpdateRequest;
import io.github.gvn2012.post_service.dtos.responses.PostResponse;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IPostService {
    PostResponse createPost(PostCreateRequest request, UUID authorId);
    PostResponse getPostById(UUID id, UUID viewerId);
    List<PostResponse> getPostsByAuthor(UUID authorId, UUID viewerId, Pageable pageable);
    List<PostResponse> getPostsByStatus(PostStatus status, UUID viewerId, Pageable pageable);
    PostResponse updatePostContent(UUID postId, UUID editorId, PostUpdateRequest request);
    void deletePost(UUID id, UUID userId);
    void archivePost(UUID id, UUID userId);
    PostResponse pinPost(UUID id, UUID userId);
    PostResponse unpinPost(UUID id, UUID userId);
    PostResponse sharePost(UUID originalPostId, UUID sharerId, String shareContent);
    List<PostResponse> searchPosts(String keyword, UUID viewerId, Pageable pageable);
    void updateEngagementMetrics(UUID postId, int viewInc, int reactionInc, int commentInc, int shareInc);
    void reindexAllPosts();
}
