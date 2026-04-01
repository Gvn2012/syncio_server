package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.entities.*;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
import io.github.gvn2012.post_service.entities.enums.PostVisibility;
import io.github.gvn2012.post_service.exceptions.NotFoundException;
import io.github.gvn2012.post_service.repositories.*;
import io.github.gvn2012.post_service.services.interfaces.IPostContentVersionService;
import io.github.gvn2012.post_service.services.interfaces.IPostService;
import io.github.gvn2012.post_service.services.kafka.PostEventProducer;
import io.github.gvn2012.post_service.dtos.requests.PostCreateRequest;
import io.github.gvn2012.post_service.dtos.requests.PostUpdateRequest;
import io.github.gvn2012.post_service.dtos.requests.MediaAttachmentRequest;
import io.github.gvn2012.post_service.dtos.responses.PostResponse;
import io.github.gvn2012.post_service.dtos.mappers.PostMapper;
import io.github.gvn2012.post_service.dtos.mappers.MediaAttachmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements IPostService {

    private final PostRepository postRepository;
    private final IPostContentVersionService contentVersionService;
    private final PostEventProducer postEventProducer;
    private final ApplicationEventPublisher eventPublisher;
    private final UserValidationService userValidationService;
    private final PostMapper postMapper;
    private final MediaAttachmentMapper mediaAttachmentMapper;
    private final PostMentionRepository mentionRepository;
    private final PostMediaAttachmentRepository attachmentRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    @Override
    @Transactional
    public PostResponse createPost(PostCreateRequest request, UUID authorId) {
        userValidationService.validateUserCanInteract(authorId);
        
        Post post = postMapper.toEntity(request);
        post.setAuthorId(authorId);
        post.setPublishedAt(LocalDateTime.now());
        
        Post saved = postRepository.save(post);
        
        processMentions(saved, request.getMentions());
        processTags(saved, request.getTags());
        processAttachments(saved, request.getAttachments());
        
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
    public PostResponse updatePostContent(UUID postId, UUID editorId, PostUpdateRequest request) {
        userValidationService.validateUserCanInteract(editorId);
        
        Post post = fetchPostById(postId);
        contentVersionService.captureNewVersion(post, editorId, request.getContent());
        
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getContentHtml() != null) post.setContentHtml(request.getContentHtml());
        if (request.getExcerpt() != null) post.setExcerpt(request.getExcerpt());
        if (request.getLanguage() != null) post.setLanguage(request.getLanguage());
        if (request.getVisibility() != null) post.setVisibility(request.getVisibility());
        if (request.getMetadata() != null) post.setMetadata(request.getMetadata());
        
        if (request.getMentions() != null) {
            mentionRepository.deleteByPostId(postId);
            processMentions(post, request.getMentions());
        }
        if (request.getTags() != null) {
            postTagRepository.deleteByPostId(postId);
            processTags(post, request.getTags());
        }
        if (request.getAttachments() != null) {
            attachmentRepository.deleteByPostId(postId);
            processAttachments(post, request.getAttachments());
        }
        
        post.setEditCount(post.getEditCount() + 1);
        Post saved = postRepository.save(post);
        postEventProducer.publishPostUpdated(saved.getId(), saved.getAuthorId(), editorId);
        return postMapper.toResponse(saved);
    }

    private void processMentions(Post post, List<UUID> userIds) {
        if (userIds == null) return;
        userIds.forEach(userId -> {
            PostMention mention = new PostMention(null, post, userId, io.github.gvn2012.post_service.entities.enums.MentionStatus.ACTIVE);
            mentionRepository.save(mention);
        });
    }

    private void processTags(Post post, List<String> tagNames) {
        if (tagNames == null) return;
        tagNames.forEach(name -> {
            Tag tag = tagRepository.findByName(name)
                    .orElseGet(() -> tagRepository.save(new Tag(null, name, name, 0L, 0L, false, false, null, null)));
            PostTag postTag = new PostTag(new io.github.gvn2012.post_service.entities.composite_keys.PostTagId(post.getId(), tag.getId()), post, tag);
            postTagRepository.save(postTag);
        });
    }

    private void processAttachments(Post post, List<MediaAttachmentRequest> requests) {
        if (requests == null) return;
        requests.forEach(req -> {
            PostMediaAttachment attachment = mediaAttachmentMapper.toEntity(req);
            attachment.setPost(post);
            attachmentRepository.save(attachment);
        });
    }

    @Override
    @Transactional
    public void deletePost(UUID id, UUID userId) {
        userValidationService.validateUserCanInteract(userId);
        Post post = fetchPostById(id);
        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
        postEventProducer.publishPostDeleted(post.getId(), post.getAuthorId());
    }

    @Override
    @Transactional
    public void archivePost(UUID id, UUID userId) {
        userValidationService.validateUserCanInteract(userId);
        Post post = fetchPostById(id);
        post.setStatus(PostStatus.ARCHIVED);
        postRepository.save(post);
    }

    @Override
    @Transactional
    public PostResponse pinPost(UUID id, UUID userId) {
        userValidationService.validateUserCanInteract(userId);
        Post post = fetchPostById(id);
        post.setIsPinned(true);
        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    @Transactional
    public PostResponse unpinPost(UUID id, UUID userId) {
        userValidationService.validateUserCanInteract(userId);
        Post post = fetchPostById(id);
        post.setIsPinned(false);
        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    @Transactional
    public PostResponse sharePost(UUID originalPostId, UUID sharerId, String shareContent) {
        userValidationService.validateUserCanInteract(sharerId);
        Post original = fetchPostById(originalPostId);
        
        Post sharedPost = new Post();
        sharedPost.setAuthorId(sharerId);
        sharedPost.setContent(shareContent);
        sharedPost.setIsShared(true);
        sharedPost.setParentPost(original);
        sharedPost.setStatus(PostStatus.PUBLISHED);
        sharedPost.setPostCategory(original.getPostCategory());
        sharedPost.setVisibility(original.getVisibility());
        sharedPost.setPublishedAt(LocalDateTime.now());
        
        Post saved = postRepository.save(sharedPost);
        original.setShareCount(original.getShareCount() + 1);
        postRepository.save(original);
        
        postEventProducer.publishPostCreated(saved.getId(), sharerId);
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
        if (viewInc != 0) post.setViewCount(post.getViewCount().add(java.math.BigInteger.valueOf(viewInc)));
        if (reactionInc != 0) post.setReactionCount(post.getReactionCount() + reactionInc);
        if (commentInc != 0) post.setCommentCount(post.getCommentCount() + commentInc);
        if (shareInc != 0) post.setShareCount(post.getShareCount() + shareInc);
        postRepository.save(post);
    }
}
