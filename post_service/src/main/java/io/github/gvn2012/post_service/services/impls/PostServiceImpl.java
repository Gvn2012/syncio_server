package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
import io.github.gvn2012.post_service.entities.enums.PostVisibility;
import io.github.gvn2012.post_service.exceptions.NotFoundException;
import io.github.gvn2012.post_service.repositories.PostRepository;
import io.github.gvn2012.post_service.services.interfaces.IPostContentVersionService;
import io.github.gvn2012.post_service.services.interfaces.IPostService;
import io.github.gvn2012.post_service.services.kafka.PostEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.github.gvn2012.post_service.dtos.requests.PostCreateRequest;
import io.github.gvn2012.post_service.dtos.requests.PostUpdateRequest;
import io.github.gvn2012.post_service.dtos.responses.PostResponse;
import io.github.gvn2012.post_service.dtos.mappers.PostMapper;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements IPostService {

    private final PostRepository postRepository;
    private final IPostContentVersionService contentVersionService;
    private final PostEventProducer postEventProducer;
    private final ApplicationEventPublisher eventPublisher;
    private final UserValidationService userValidationService;
    private final io.github.gvn2012.post_service.dtos.mappers.PostMapper postMapper;

    @Override
    @Transactional
    public PostResponse createPost(io.github.gvn2012.post_service.dtos.requests.PostCreateRequest request, UUID authorId) {
        userValidationService.validateUserCanInteract(authorId);
        
        Post post = postMapper.toEntity(request);
        post.setAuthorId(authorId);
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        
        Post saved = postRepository.save(post);
        contentVersionService.captureNewVersion(saved, saved.getAuthorId(), saved.getContent());
        postEventProducer.publishPostCreated(saved.getId(), saved.getAuthorId());
        eventPublisher.publishEvent(new FeedFanoutWorker.PostCreatedEvent(saved));
        return postMapper.toResponse(saved);
    }

    private Post fetchPostById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: " + id));
    }

    @Override
    public PostResponse getPostById(UUID id) {
        return postMapper.toResponse(fetchPostById(id));
    }

    @Override
    public List<PostResponse> getPostsByAuthor(UUID authorId, Pageable pageable) {
        return postRepository.findByAuthorId(authorId, pageable)
                .stream().map(postMapper::toResponse).toList();
    }

    @Override
    public List<PostResponse> getPostsByStatus(PostStatus status, Pageable pageable) {
        return postRepository.findByStatus(status, pageable)
                .stream().map(postMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public PostResponse updatePostContent(UUID postId, UUID editorId, io.github.gvn2012.post_service.dtos.requests.PostUpdateRequest request) {
        userValidationService.validateUserCanInteract(editorId);
        
        Post post = fetchPostById(postId);
        contentVersionService.captureNewVersion(post, editorId, request.getContent());
        
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getLanguage() != null) post.setLanguage(request.getLanguage());
        if (request.getVisibility() != null) post.setVisibility(request.getVisibility());
        
        post.setEditCount(post.getEditCount() + 1);
        Post saved = postRepository.save(post);
        postEventProducer.publishPostUpdated(saved.getId(), saved.getAuthorId(), editorId);
        return postMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deletePost(UUID id) {
        Post post = fetchPostById(id);
        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
        postEventProducer.publishPostDeleted(post.getId(), post.getAuthorId());
    }

    @Override
    @Transactional
    public void archivePost(UUID id) {
        Post post = fetchPostById(id);
        post.setStatus(PostStatus.ARCHIVED);
        post.setArchivedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    @Override
    @Transactional
    public PostResponse pinPost(UUID id) {
        Post post = fetchPostById(id);
        post.setVisibility(PostVisibility.PUBLIC);
        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    @Transactional
    public PostResponse unpinPost(UUID id) {
        Post post = fetchPostById(id);
        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    @Transactional
    public PostResponse sharePost(UUID originalPostId, UUID sharerId, String shareContent) {
        userValidationService.validateUserCanInteract(sharerId);
        Post original = fetchPostById(originalPostId);
        userValidationService.validateNotBlocked(original.getAuthorId(), sharerId);
        
        Post shared = new Post();
        shared.setAuthorId(sharerId);
        shared.setContent(shareContent);
        shared.setOrgId(original.getOrgId());
        shared.setIsShared(true);
        shared.setParentPost(original);
        shared.setStatus(PostStatus.PUBLISHED);
        shared.setPublishedAt(LocalDateTime.now());
        
        Post saved = postRepository.save(shared);
        original.setShareCount(original.getShareCount() + 1);
        postRepository.save(original);
        
        postEventProducer.publishPostShared(original.getId(), original.getAuthorId(), sharerId);
        return postMapper.toResponse(saved);
    }

    @Override
    public List<PostResponse> searchPosts(String keyword, Pageable pageable) {
        return postRepository.searchByContentContaining(keyword, pageable)
                .stream().map(postMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void updateEngagementMetrics(UUID postId, int viewInc, int reactionInc, int commentInc, int shareInc) {
        Post post = fetchPostById(postId);
        post.setReactionCount(post.getReactionCount() + reactionInc);
        post.setCommentCount(post.getCommentCount() + commentInc);
        post.setShareCount(post.getShareCount() + shareInc);
        postRepository.save(post);
    }
}
