package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.dtos.mappers.MediaAttachmentMapper;
import io.github.gvn2012.post_service.dtos.mappers.PostMapper;
import io.github.gvn2012.post_service.dtos.requests.MediaAttachmentRequest;
import io.github.gvn2012.post_service.dtos.requests.PostCreateRequest;
import io.github.gvn2012.post_service.dtos.requests.PostUpdateRequest;
import io.github.gvn2012.post_service.dtos.responses.PostResponse;
import io.github.gvn2012.post_service.entities.*;
import io.github.gvn2012.post_service.entities.composite_keys.PostTagId;
import io.github.gvn2012.post_service.entities.enums.AttachmentUploadStatus;
import io.github.gvn2012.post_service.entities.enums.MentionStatus;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
import io.github.gvn2012.shared.kafka_events.PostSearchEvent;
import io.github.gvn2012.shared.kafka_events.PostSearchEvent.OperationType;
import io.github.gvn2012.post_service.exceptions.NotFoundException;
import io.github.gvn2012.post_service.repositories.*;
import io.github.gvn2012.post_service.dtos.mappers.*;
import io.github.gvn2012.post_service.services.interfaces.IPostContentVersionService;
import io.github.gvn2012.post_service.services.interfaces.IPostService;
import io.github.gvn2012.post_service.services.kafka.PostEventProducer;
import io.github.gvn2012.post_service.clients.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    private final PostEventRepository postEventRepository;
    private final PostEventMapper postEventMapper;
    private final PostPollRepository postPollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final PostPollMapper postPollMapper;
    private final PostTaskRepository postTaskRepository;
    private final PostTaskAssigneeRepository postTaskAssigneeRepository;
    private final PostTaskMapper postTaskMapper;
    private final PostAnnouncementRepository postAnnouncementRepository;
    private final PostAnnouncementMapper postAnnouncementMapper;
    private final UserClient userClient;
    private final io.github.gvn2012.post_service.clients.UploadClient uploadClient;

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
        List<String> presignedUrls = null;
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            presignedUrls = processAttachmentsWithPresign(saved, request.getAttachments());
        }
        processSubtypes(saved, request);

        contentVersionService.captureNewVersion(saved, saved.getAuthorId(), saved.getContent());

        enrichAndPublish(saved);

        eventPublisher.publishEvent(new FeedFanoutWorker.PostCreatedEvent(saved));

        PostResponse response = postMapper.toResponse(saved);
        if (presignedUrls != null && response.getAttachments() != null && !response.getAttachments().isEmpty()) {
            int max = Math.min(presignedUrls.size(), response.getAttachments().size());
            for (int i = 0; i < max; i++) {
                response.getAttachments().get(i).setUploadUrl(presignedUrls.get(i));
            }
        }
        return response;
    }

    private void processSubtypes(Post post, PostCreateRequest request) {
        switch (post.getPostCategory()) {
            case EVENT -> {
                if (request.getEvent() != null) {
                    PostEvent event = postEventMapper.toEntity(request.getEvent());
                    event.setPost(post);
                    postEventRepository.save(event);
                    post.setEvent(event);
                }
            }
            case POLL -> {
                if (request.getPoll() != null) {
                    PostPoll poll = postPollMapper.toEntity(request.getPoll());
                    poll.setPost(post);
                    PostPoll savedPoll = postPollRepository.save(poll);
                    if (request.getPoll().getOptions() != null) {
                        List<PollOption> options = request.getPoll().getOptions().stream()
                                .map(optReq -> {
                                    PollOption opt = postPollMapper.toOptionEntity(optReq);
                                    opt.setPoll(savedPoll);
                                    return opt;
                                }).toList();
                        if (!options.isEmpty()) {
                            pollOptionRepository.saveAll(options);
                            savedPoll.setOptions(new java.util.LinkedHashSet<>(options));
                        }
                    }
                    post.setPoll(savedPoll);
                }
            }
            case TASK -> {
                if (request.getTask() != null) {
                    PostTask task = postTaskMapper.toEntity(request.getTask());
                    task.setPost(post);
                    PostTask savedTask = postTaskRepository.save(task);
                    if (request.getTask().getAssignees() != null) {
                        List<PostTaskAssignee> assignees = request.getTask().getAssignees().stream()
                                .map(uid -> new PostTaskAssignee(null, savedTask, uid,
                                        LocalDateTime.now()))
                                .toList();
                        if (!assignees.isEmpty()) {
                            postTaskAssigneeRepository.saveAll(assignees);
                            savedTask.setAssignees(new java.util.LinkedHashSet<>(assignees));
                        }
                    }
                    post.setTask(savedTask);
                }
            }
            case ANNOUNCEMENT -> {
                if (request.getAnnouncement() != null) {
                    PostAnnouncement announcement = postAnnouncementMapper.toEntity(request.getAnnouncement());
                    announcement.setPost(post);
                    postAnnouncementRepository.save(announcement);
                    post.setAnnouncement(announcement);
                }
            }
            default -> {
            }
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
        return postMapper.toResponse(post);
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
        return response;
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
        java.util.Set<String> objectPaths = new java.util.LinkedHashSet<>();
        
        for (MediaAttachmentRequest req : requests) {
            String path = "posts/" + post.getId() + "/" + java.util.UUID.randomUUID();
            if (req.getFileName() != null && !req.getFileName().isEmpty()) {
                path += "-" + req.getFileName();
            }
            objectPaths.add(path);
        }

        io.github.gvn2012.post_service.dtos.requests.SignedUrlRequestDTO reqDto = 
            new io.github.gvn2012.post_service.dtos.requests.SignedUrlRequestDTO(objectPaths);
        io.github.gvn2012.post_service.dtos.responses.SignedUrlResponseDTO resDto = uploadClient.getSignedUrls(reqDto);
        java.util.Map<String, String> signedUrls = resDto != null && resDto.getSignedUrls() != null 
            ? resDto.getSignedUrls() : java.util.Collections.emptyMap();

        int index = 0;
        List<String> orderedUploadUrls = new java.util.ArrayList<>();

        for (String path : objectPaths) {
            MediaAttachmentRequest req = requests.get(index++);
            PostMediaAttachment attachment = mediaAttachmentMapper.toEntity(req);
            attachment.setPost(post);
            attachment.setObjectPath(path);
            
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
        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    @Transactional
    public PostResponse unpinPost(UUID id, UUID userId) {
        userValidationService.validateUserCanInteract(userId);
        Post post = fetchPostById(id);
        validateOwnership(post, userId);
        post.setIsPinned(false);
        return postMapper.toResponse(postRepository.save(post));
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
