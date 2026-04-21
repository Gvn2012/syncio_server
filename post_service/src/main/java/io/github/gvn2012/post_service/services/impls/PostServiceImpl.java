package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.dtos.mappers.MediaAttachmentMapper;
import io.github.gvn2012.post_service.dtos.mappers.PostMapper;
import io.github.gvn2012.post_service.dtos.requests.MediaAttachmentRequest;
import io.github.gvn2012.post_service.dtos.requests.PostCreateRequest;
import io.github.gvn2012.post_service.dtos.requests.PostUpdateRequest;
import io.github.gvn2012.post_service.dtos.requests.SignedUrlRequestDTO;
import io.github.gvn2012.post_service.dtos.responses.PostResponse;
import io.github.gvn2012.post_service.dtos.responses.SignedUrlResponseDTO;
import io.github.gvn2012.post_service.dtos.responses.UserSummaryResponse;
import io.github.gvn2012.post_service.entities.*;
import io.github.gvn2012.post_service.entities.composite_keys.PostTagId;
import io.github.gvn2012.post_service.entities.enums.AttachmentUploadStatus;
import io.github.gvn2012.post_service.entities.enums.MentionStatus;
import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
import io.github.gvn2012.shared.kafka_events.PostSearchEvent;
import io.github.gvn2012.shared.kafka_events.PostSearchEvent.OperationType;
import lombok.extern.slf4j.Slf4j;
import io.github.gvn2012.post_service.exceptions.NotFoundException;
import io.github.gvn2012.post_service.repositories.*;
import io.github.gvn2012.post_service.services.interfaces.IModerationService;
import io.github.gvn2012.post_service.services.interfaces.IPostContentVersionService;
import io.github.gvn2012.post_service.services.interfaces.IPostService;
import io.github.gvn2012.post_service.services.interfaces.ISimilarityService;
import io.github.gvn2012.post_service.services.kafka.PostEventProducer;
import io.github.gvn2012.post_service.services.subtypes.PostSubtypeProcessor;
import io.github.gvn2012.post_service.clients.UserClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
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
    private final UserClient userClient;
    private final io.github.gvn2012.post_service.clients.UploadClient uploadClient;
    private final UserSummaryService userSummaryService;
    private final PostReactionRepository postReactionRepository;
    private final ISimilarityService similarityService;
    private final IModerationService moderationService;
    private final Map<PostCategory, PostSubtypeProcessor> subtypeProcessors;

    public PostServiceImpl(
            PostRepository postRepository,
            IPostContentVersionService contentVersionService,
            PostEventProducer postEventProducer,
            ApplicationEventPublisher eventPublisher,
            UserValidationService userValidationService,
            PostMapper postMapper,
            MediaAttachmentMapper mediaAttachmentMapper,
            PostMentionRepository mentionRepository,
            PostMediaAttachmentRepository attachmentRepository,
            TagRepository tagRepository,
            PostTagRepository postTagRepository,
            UserClient userClient,
            io.github.gvn2012.post_service.clients.UploadClient uploadClient,
            UserSummaryService userSummaryService,
            PostReactionRepository postReactionRepository,
            ISimilarityService similarityService,
            IModerationService moderationService,
            List<PostSubtypeProcessor> processors) {
        this.postRepository = postRepository;
        this.contentVersionService = contentVersionService;
        this.postEventProducer = postEventProducer;
        this.eventPublisher = eventPublisher;
        this.userValidationService = userValidationService;
        this.postMapper = postMapper;
        this.mediaAttachmentMapper = mediaAttachmentMapper;
        this.mentionRepository = mentionRepository;
        this.attachmentRepository = attachmentRepository;
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
        this.userClient = userClient;
        this.uploadClient = uploadClient;
        this.userSummaryService = userSummaryService;
        this.postReactionRepository = postReactionRepository;
        this.similarityService = similarityService;
        this.moderationService = moderationService;
        this.subtypeProcessors = processors.stream()
                .collect(Collectors.toMap(PostSubtypeProcessor::supportedCategory, Function.identity()));
    }

    @Override
    @Transactional
    public PostResponse createPost(PostCreateRequest request, UUID authorId) {
        log.info("Starting createPost for author: {}", authorId);
        userValidationService.validateUserCanInteract(authorId);

        // Near-duplicate check
        if (similarityService.isDuplicate(request.getContent())) {
            throw new io.github.gvn2012.post_service.exceptions.BadRequestException(
                    "This post is too similar to another recent post. Please wait or change the content.");
        }

        Post post = postMapper.toEntity(request);
        log.debug("Converted request to entity for author: {}", authorId);
        post.setAuthorId(authorId);
        post.setPublishedAt(LocalDateTime.now());

        Post saved = postRepository.save(post);
        log.info("Saved post with ID: {}", saved.getId());

        // Moderation & Censorship Scan
        moderationService.moderatePost(authorId, saved);

        processMentions(saved, request.getMentions());
        processTags(saved, request.getTags());
        log.debug("Processed mentions and tags for post: {}", saved.getId());

        List<String> presignedUrls = null;
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            presignedUrls = processAttachmentsWithPresign(saved, request.getAttachments());
            log.debug("Processed {} attachments for post: {}", request.getAttachments().size(), saved.getId());
        }
        processSubtypes(saved, request);
        log.debug("Processed subtypes for post: {}", saved.getId());

        contentVersionService.captureNewVersion(saved, saved.getAuthorId(), saved.getContent());
        log.debug("Captured content version for post: {}", saved.getId());

        enrichAndPublish(saved);
        log.info("Finished enrichAndPublish for post: {}", saved.getId());

        eventPublisher.publishEvent(new FeedFanoutWorker.PostCreatedEvent(saved));
        log.info("Published FeedFanoutWorker.PostCreatedEvent for post: {}", saved.getId());

        PostResponse response = postMapper.toResponse(saved);
        if (presignedUrls != null && response.getAttachments() != null && !response.getAttachments().isEmpty()) {
            int max = Math.min(presignedUrls.size(), response.getAttachments().size());
            for (int i = 0; i < max; i++) {
                response.getAttachments().get(i).setUploadUrl(presignedUrls.get(i));
            }
        }
        log.info("Successfully completed createPost for post: {}", saved.getId());
        return enrichPost(response, authorId);
    }

    private void processSubtypes(Post post, PostCreateRequest request) {
        PostSubtypeProcessor processor = subtypeProcessors.get(post.getPostCategory());
        if (processor != null) {
            processor.process(post, request);
        }
    }

    private void enrichAndPublish(Post post) {
        String authorName = userClient.getUserName(post.getAuthorId()).block();

        List<UUID> mentions = post.getMentions() != null
                ? post.getMentions().stream().map(PostMention::getUserId).toList()
                : Collections.emptyList();

        List<UUID> assignees = Optional.ofNullable(post.getTask())
                .map(task -> task.getAssignees().stream().map(PostTaskAssignee::getUserId).toList())
                .orElse(Collections.emptyList());

        postEventProducer.publishPostCreated(
                post.getId(),
                post.getAuthorId(),
                authorName,
                post.getPostCategory().name(),
                mentions,
                assignees);

        postEventProducer.publishPostSearchIndexing(PostSearchEvent.builder()
                .postId(post.getId())
                .authorId(post.getAuthorId())
                .content(post.getContent())
                .publishedAt(post.getPublishedAt())
                .status(post.getStatus().name())
                .operationType(OperationType.UPSERT)
                .build());
    }

    private Post fetchPostById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: " + id));
    }

    @Override
    public PostResponse getPostById(UUID id, UUID viewerId) {
        Post post = fetchPostById(id);
        userValidationService.validateCanView(post, viewerId);
        return enrichPost(postMapper.toResponse(post), viewerId);
    }

    @Override
    public List<PostResponse> getPostsByAuthor(UUID authorId, UUID viewerId, Pageable pageable) {
        List<Post> posts = postRepository.findByAuthorId(authorId, pageable).stream().toList();
        return enrichPosts(posts, viewerId);
    }

    @Override
    public List<PostResponse> getPostsByStatus(PostStatus status, UUID viewerId, Pageable pageable) {
        List<Post> posts = postRepository.findByStatus(status, pageable).stream().toList();
        return enrichPosts(posts, viewerId);
    }

    private PostResponse enrichPost(PostResponse response, UUID viewerId) {
        if (response == null)
            return null;

        response.setAuthorInfo(userSummaryService.getSummary(response.getAuthorId()));

        if (viewerId != null) {
            postReactionRepository.findByPostIdAndUserId(response.getId(), viewerId)
                    .ifPresent(reaction -> response.setViewerReaction(reaction.getReactionType().getCode()));

            boolean isShared = postRepository
                    .findSharedPostIdsByAuthor(viewerId, Collections.singleton(response.getId()))
                    .contains(response.getId());
            response.setSharedByViewer(isShared);
        }

        return response;
    }

    private List<PostResponse> enrichPosts(List<Post> posts, UUID viewerId) {
        if (posts == null || posts.isEmpty())
            return List.of();

        Set<UUID> postIds = posts.stream().map(Post::getId).collect(Collectors.toSet());
        Set<UUID> authorIds = posts.stream().map(Post::getAuthorId).collect(Collectors.toSet());

        Map<UUID, UserSummaryResponse> summaries = userSummaryService.getSummaries(authorIds);

        Map<UUID, String> reactionsMap = new HashMap<>();
        Set<UUID> sharedPostIds = new HashSet<>();

        if (viewerId != null) {
            postReactionRepository.findByUserIdAndPostIdIn(viewerId, postIds)
                    .forEach(r -> reactionsMap.put(r.getPost().getId(), r.getReactionType().getCode()));

            sharedPostIds = postRepository.findSharedPostIdsByAuthor(viewerId, postIds);
        }

        final Set<UUID> sharedIdsFinal = sharedPostIds;

        return posts.stream().map(post -> {
            PostResponse res = postMapper.toResponse(post);
            res.setAuthorInfo(summaries.get(post.getAuthorId()));

            if (viewerId != null) {
                res.setViewerReaction(reactionsMap.get(post.getId()));
                res.setSharedByViewer(sharedIdsFinal.contains(post.getId()));
            }

            return res;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PostResponse updatePostContent(UUID postId, UUID editorId, PostUpdateRequest request) {
        userValidationService.validateUserCanInteract(editorId);

        Post post = fetchPostById(postId);
        validateOwnership(post, editorId);

        contentVersionService.captureNewVersion(post, editorId, request.getContent());

        if (request.getContent() != null)
            post.setContent(request.getContent());
        if (request.getContentHtml() != null)
            post.setContentHtml(request.getContentHtml());
        if (request.getExcerpt() != null)
            post.setExcerpt(request.getExcerpt());
        if (request.getLanguage() != null)
            post.setLanguage(request.getLanguage());
        if (request.getVisibility() != null)
            post.setVisibility(request.getVisibility());
        if (request.getMetadata() != null)
            post.setMetadata(request.getMetadata());

        if (request.getMentions() != null) {
            mentionRepository.deleteByPostId(postId);
            processMentions(post, request.getMentions());
        }
        if (request.getTags() != null) {
            postTagRepository.deleteByPostId(postId);
            processTags(post, request.getTags());
        }
        List<String> presignedUrls = null;
        if (request.getAttachments() != null) {
            attachmentRepository.deleteByPostId(postId);
            presignedUrls = processAttachmentsWithPresign(post, request.getAttachments());
        }

        moderationService.moderatePost(editorId, post);

        post.setEditCount(post.getEditCount() + 1);
        Post saved = postRepository.save(post);
        postEventProducer.publishPostUpdated(saved.getId(), saved.getAuthorId(), editorId);
        postEventProducer.publishPostSearchIndexing(PostSearchEvent.builder()
                .postId(saved.getId())
                .authorId(saved.getAuthorId())
                .content(saved.getContent())
                .publishedAt(saved.getPublishedAt())
                .status(saved.getStatus().name())
                .operationType(OperationType.UPSERT)
                .build());

        PostResponse response = postMapper.toResponse(saved);
        if (presignedUrls != null && response.getAttachments() != null && !response.getAttachments().isEmpty()) {
            int max = Math.min(presignedUrls.size(), response.getAttachments().size());
            for (int i = 0; i < max; i++) {
                if (presignedUrls.get(i) != null) {
                    response.getAttachments().get(i).setUploadUrl(presignedUrls.get(i));
                }
            }
        }
        return enrichPost(response, editorId);
    }

    private void validateOwnership(Post post, UUID userId) {
        if (!post.getAuthorId().equals(userId)) {
            throw new io.github.gvn2012.post_service.exceptions.ForbiddenException(
                    "User is not the author of this post");
        }
    }

    private void processMentions(Post post, List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty())
            return;
        List<PostMention> mentions = userIds.stream()
                .map(userId -> new PostMention(null, post, userId,
                        MentionStatus.ACTIVE))
                .toList();
        mentionRepository.saveAll(mentions);
    }

    private void processTags(Post post, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty())
            return;
        List<PostTag> postTags = tagNames.stream()
                .map(name -> {
                    Tag tag = tagRepository.findByName(name)
                            .orElseGet(() -> tagRepository
                                    .save(new Tag(null, name, name, 0L, 0L, false, false, null, null)));
                    return new PostTag(new PostTagId(
                            post.getId(), tag.getId()), post, tag);
                })
                .toList();
        postTagRepository.saveAll(postTags);
    }

    private List<String> processAttachmentsWithPresign(Post post, List<MediaAttachmentRequest> requests) {
        if (requests == null || requests.isEmpty())
            return null;

        List<PostMediaAttachment> attachments = new java.util.ArrayList<>();
        java.util.Map<String, String> pathContentTypes = new java.util.LinkedHashMap<>();
        java.util.List<String> orderedPaths = new java.util.ArrayList<>();

        java.util.Map<String, String> pathToImageId = new java.util.LinkedHashMap<>();
        for (MediaAttachmentRequest req : requests) {
            String imageId = java.util.UUID.randomUUID().toString();
            String path = "post_img/" + post.getId() + "/" + imageId;
            if (req.getFileName() != null && !req.getFileName().isEmpty()) {
                path += "-" + req.getFileName();
            }
            pathToImageId.put(path, imageId);
            pathContentTypes.put(path, req.getMimeType());
            orderedPaths.add(path);
        }

        SignedUrlRequestDTO reqDto = new SignedUrlRequestDTO(pathContentTypes);
        SignedUrlResponseDTO resDto = uploadClient.getSignedUrls(reqDto);
        java.util.Map<String, String> signedUrls = resDto != null && resDto.getSignedUrls() != null
                ? resDto.getSignedUrls()
                : java.util.Collections.emptyMap();

        int index = 0;
        List<String> orderedUploadUrls = new java.util.ArrayList<>();

        for (String path : orderedPaths) {
            MediaAttachmentRequest req = requests.get(index++);
            PostMediaAttachment attachment = mediaAttachmentMapper.toEntity(req);
            attachment.setPost(post);
            attachment.setObjectPath(path);
            attachment.setExternalId(pathToImageId.get(path));

            String signedPutUrl = signedUrls.get(path);
            orderedUploadUrls.add(signedPutUrl);

            if (signedPutUrl != null && !signedPutUrl.isEmpty()) {
                int qm = signedPutUrl.indexOf('?');
                attachment.setUrl(qm != -1 ? signedPutUrl.substring(0, qm) : signedPutUrl);
            } else {
                attachment.setUrl("");
            }
            attachment.setUploadStatus(AttachmentUploadStatus.PENDING);
            attachments.add(attachment);
        }

        attachmentRepository.saveAll(attachments);
        post.setAttachments(new java.util.LinkedHashSet<>(attachments));
        return orderedUploadUrls;
    }

    @Override
    @Transactional
    public void deletePost(UUID id, UUID userId) {
        userValidationService.validateUserCanInteract(userId);
        Post post = fetchPostById(id);
        validateOwnership(post, userId);
        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
        postEventProducer.publishPostDeleted(post.getId(), post.getAuthorId());
        postEventProducer.publishPostSearchIndexing(PostSearchEvent.builder()
                .postId(post.getId())
                .operationType(OperationType.DELETE)
                .build());
    }

    @Override
    @Transactional
    public void archivePost(UUID id, UUID userId) {
        userValidationService.validateUserCanInteract(userId);
        Post post = fetchPostById(id);
        validateOwnership(post, userId);
        post.setStatus(PostStatus.ARCHIVED);
        postRepository.save(post);
        postEventProducer.publishPostSearchIndexing(PostSearchEvent.builder()
                .postId(post.getId())
                .authorId(post.getAuthorId())
                .content(post.getContent())
                .publishedAt(post.getPublishedAt())
                .status(post.getStatus().name())
                .operationType(OperationType.UPSERT)
                .build());
    }

    @Override
    @Transactional
    public PostResponse pinPost(UUID id, UUID userId) {
        userValidationService.validateUserCanInteract(userId);
        Post post = fetchPostById(id);
        validateOwnership(post, userId);
        post.setIsPinned(true);
        return enrichPost(postMapper.toResponse(postRepository.save(post)), userId);
    }

    @Override
    @Transactional
    public PostResponse unpinPost(UUID id, UUID userId) {
        userValidationService.validateUserCanInteract(userId);
        Post post = fetchPostById(id);
        validateOwnership(post, userId);
        post.setIsPinned(false);
        return enrichPost(postMapper.toResponse(postRepository.save(post)), userId);
    }

    @Override
    @Transactional
    public PostResponse sharePost(UUID originalPostId, UUID sharerId, String shareContent) {
        userValidationService.validateUserCanInteract(sharerId);
        Post original = fetchPostById(originalPostId);
        userValidationService.validateCanView(original, sharerId);

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
        postRepository.incrementShareCount(originalPostId, 1);

        enrichAndPublish(saved);
        postEventProducer.publishPostSearchIndexing(PostSearchEvent.builder()
                .postId(saved.getId())
                .authorId(saved.getAuthorId())
                .content(saved.getContent())
                .publishedAt(saved.getPublishedAt())
                .status(saved.getStatus().name())
                .operationType(OperationType.UPSERT)
                .build());
        return enrichPost(postMapper.toResponse(saved), sharerId);
    }

    @Override
    public List<PostResponse> searchPosts(String keyword, UUID viewerId, Pageable pageable) {
        List<Post> posts = postRepository.searchByContentContaining(keyword, pageable).stream().toList();
        return enrichPosts(posts, viewerId);
    }

    @Override
    @Transactional
    public void updateEngagementMetrics(UUID postId, int viewInc, int reactionInc, int commentInc, int shareInc) {
        if (viewInc != 0)
            postRepository.incrementViewCount(postId, viewInc);
        if (reactionInc != 0)
            postRepository.incrementReactionCount(postId, reactionInc);
        if (commentInc != 0)
            postRepository.incrementCommentCount(postId, commentInc);
        if (shareInc != 0)
            postRepository.incrementShareCount(postId, shareInc);
    }
}
