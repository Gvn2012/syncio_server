package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.dtos.mappers.CommentMapper;
import io.github.gvn2012.post_service.dtos.responses.CommentPagedResponse;
import io.github.gvn2012.post_service.dtos.responses.CommentResponse;
import io.github.gvn2012.post_service.dtos.responses.UserSummaryResponse;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostComment;
import io.github.gvn2012.post_service.entities.enums.CommentStatus;
import io.github.gvn2012.post_service.exceptions.BadRequestException;
import io.github.gvn2012.post_service.exceptions.ForbiddenException;
import io.github.gvn2012.post_service.exceptions.NotFoundException;
import io.github.gvn2012.post_service.repositories.PostCommentReactionRepository;
import io.github.gvn2012.post_service.repositories.PostCommentRepository;
import io.github.gvn2012.post_service.repositories.PostRepository;
import io.github.gvn2012.post_service.services.events.PostCommentPopularityUpdatedEvent;
import io.github.gvn2012.post_service.services.interfaces.IInteractionVelocityService;
import io.github.gvn2012.post_service.services.interfaces.IPostCommentService;
import io.github.gvn2012.post_service.services.kafka.PostEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements IPostCommentService {

    private final PostCommentRepository commentRepository;
    private final PostCommentReactionRepository postCommentReactionRepository;
    private final PostRepository postRepository;
    private final PostEventProducer postEventProducer;
    private final CommentMapper commentMapper;
    private final UserValidationService userValidationService;
    private final IInteractionVelocityService velocityService;
    private final UserSummaryService userSummaryService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CommentResponse addComment(@NonNull UUID postId, UUID authorId, String content, UUID parentCommentId) {
        userValidationService.validateUserCanInteract(authorId);
        Post post = fetchAndValidatePost(postId, authorId);

        velocityService.recordInteraction(postId, IInteractionVelocityService.InteractionType.COMMENT);

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUserId(authorId);
        comment.setContent(content);
        comment.setStatus(CommentStatus.VISIBLE);

        if (parentCommentId != null) {
            PostComment parent = fetchCommentById(parentCommentId);
            validateCommentPostCorrelation(parent, postId);
            comment.setParentComment(parent);
            comment.setDepth(parent.getDepth() + 1);

            commentRepository.incrementReplyCount(parentCommentId, 1);
            eventPublisher.publishEvent(new PostCommentPopularityUpdatedEvent(parentCommentId));
        } else {
            comment.setDepth(0);
        }

        postRepository.incrementCommentCount(postId, 1);
        comment = commentRepository.save(comment);

        postEventProducer.publishPostCommented(postId, post.getAuthorId(), authorId);

        return enrichComments(List.of(commentMapper.toResponse(comment)), authorId).get(0);
    }

    private PostComment fetchCommentById(@NonNull UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + id));
    }

    private Post fetchAndValidatePost(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));
        userValidationService.validateCanView(post, userId);
        return post;
    }

    private void validateCommentPostCorrelation(PostComment comment, UUID postId) {
        if (!comment.getPost().getId().equals(postId)) {
            throw new BadRequestException("Comment does not belong to the specified post");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(@NonNull UUID postId, @NonNull UUID commentId, UUID viewerId) {
        fetchAndValidatePost(postId, viewerId);
        PostComment comment = fetchCommentById(commentId);
        validateCommentPostCorrelation(comment, postId);

        if (comment.getStatus() != CommentStatus.VISIBLE) {
            throw new NotFoundException("Comment not found");
        }
        return enrichComments(List.of(commentMapper.toResponse(comment)), viewerId).get(0);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(@NonNull UUID postId, @NonNull UUID commentId, UUID authorId,
            String newContent) {
        userValidationService.validateUserCanInteract(authorId);
        fetchAndValidatePost(postId, authorId);
        PostComment comment = fetchCommentById(commentId);
        validateCommentPostCorrelation(comment, postId);

        if (!comment.getUserId().equals(authorId)) {
            throw new ForbiddenException("Not allowed to update this comment");
        }
        comment.setContent(newContent);
        comment.setIsEdited(true);
        comment.setEditedAt(LocalDateTime.now());
        comment.setEditCount(comment.getEditCount() + 1);
        return enrichComments(List.of(commentMapper.toResponse(commentRepository.save(comment))), authorId).get(0);
    }

    @Override
    @Transactional
    public void deleteComment(@NonNull UUID postId, @NonNull UUID commentId, @NonNull UUID authorId) {
        userValidationService.validateUserCanInteract(authorId);
        fetchAndValidatePost(postId, authorId);
        PostComment comment = fetchCommentById(commentId);
        validateCommentPostCorrelation(comment, postId);

        if (!comment.getUserId().equals(authorId)) {
            throw new ForbiddenException("Not allowed to delete this comment");
        }

        // Recursive soft delete
        commentRepository.updateStatusRecursively(commentId, CommentStatus.DELETED);

        // Update counts
        postRepository.incrementCommentCount(postId, -1);

        if (comment.getParentComment() != null) {
            commentRepository.incrementReplyCount(comment.getParentComment().getId(), -1);
            eventPublisher.publishEvent(new PostCommentPopularityUpdatedEvent(comment.getParentComment().getId()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CommentPagedResponse getCommentsByPost(@NonNull UUID postId, UUID viewerId, Pageable pageable) {
        fetchAndValidatePost(postId, viewerId);
        Page<PostComment> commentPage = commentRepository.findRootComments(postId, CommentStatus.VISIBLE, pageable);
        return toPagedResponse(commentPage, viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentPagedResponse getReplies(@NonNull UUID postId, @NonNull UUID parentCommentId, UUID viewerId,
            Pageable pageable) {
        fetchAndValidatePost(postId, viewerId);
        PostComment parent = fetchCommentById(parentCommentId);
        validateCommentPostCorrelation(parent, postId);

        Page<PostComment> replyPage = commentRepository
                .findByParentCommentIdAndStatusOrderByCreatedAtDesc(parentCommentId, CommentStatus.VISIBLE, pageable);
        return toPagedResponse(replyPage, viewerId);
    }

    private CommentPagedResponse toPagedResponse(Page<PostComment> page, UUID viewerId) {
        List<CommentResponse> responses = page.getContent().stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());

        return CommentPagedResponse.builder()
                .comments(enrichComments(responses, viewerId))
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .hasNext(page.hasNext())
                .build();
    }

    @Override
    @Transactional
    public void pinComment(@NonNull UUID postId, @NonNull UUID commentId) {
        PostComment comment = fetchCommentById(commentId);
        validateCommentPostCorrelation(comment, postId);

        comment.setIsPinned(!comment.getIsPinned());
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCommentCount(@NonNull UUID postId) {
        return commentRepository.countByPostIdAndStatus(postId, CommentStatus.VISIBLE);
    }

    private List<CommentResponse> enrichComments(List<?> input, UUID viewerId) {
        if (input == null || input.isEmpty())
            return List.of();

        List<CommentResponse> responses;
        if (input.get(0) instanceof PostComment) {
            responses = input.stream().map(c -> commentMapper.toResponse((PostComment) c)).collect(Collectors.toList());
        } else {
            responses = (List<CommentResponse>) input;
        }

        Set<UUID> authorIds = responses.stream().map(CommentResponse::getUserId).collect(Collectors.toSet());
        Map<UUID, UserSummaryResponse> userMap = userSummaryService.getSummaries(authorIds);

        Map<UUID, String> reactionsMap = new HashMap<>();
        if (viewerId != null) {
            Set<UUID> commentIds = responses.stream().map(CommentResponse::getId).collect(Collectors.toSet());
            postCommentReactionRepository.findByUserIdAndCommentIdIn(viewerId, commentIds)
                    .forEach(r -> reactionsMap.put(r.getComment().getId(), r.getReactionType().name()));
        }

        responses.forEach(res -> {
            UUID aid = res.getUserId();
            UserSummaryResponse summary = userMap.get(aid);
            res.setAuthorInfo(summary);

            if (viewerId != null) {
                res.setViewerReaction(reactionsMap.get(res.getId()));
            }
        });

        return responses;
    }
}
