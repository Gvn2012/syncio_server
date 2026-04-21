package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.dtos.mappers.CommentMapper;
import io.github.gvn2012.post_service.dtos.responses.CommentResponse;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostComment;
import io.github.gvn2012.post_service.exceptions.ForbiddenException;
import io.github.gvn2012.post_service.exceptions.NotFoundException;
import io.github.gvn2012.post_service.repositories.PostCommentRepository;
import io.github.gvn2012.post_service.repositories.PostRepository;
import io.github.gvn2012.post_service.services.interfaces.IInteractionVelocityService;
import io.github.gvn2012.post_service.services.interfaces.IPostCommentService;
import io.github.gvn2012.post_service.services.kafka.PostEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements IPostCommentService {

    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;
    private final PostEventProducer postEventProducer;
    private final CommentMapper commentMapper;
    private final UserValidationService userValidationService;
    private final IInteractionVelocityService velocityService;

    @Override
    @Transactional
    public CommentResponse addComment(@NonNull UUID postId, UUID authorId, String content, UUID parentCommentId) {
        userValidationService.validateUserCanInteract(authorId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));
        userValidationService.validateCanView(post, authorId);

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setContent(content);
        comment.setUserId(authorId);

        if (parentCommentId != null) {
            PostComment parent = fetchCommentById(parentCommentId);
            comment.setParentComment(parent);
            comment.setDepth(parent.getDepth() + 1);
            commentRepository.incrementReplyCount(parentCommentId, 1);
        } else {
            comment.setDepth(0);
        }

        postRepository.incrementCommentCount(postId, 1);
        velocityService.recordInteraction(postId, IInteractionVelocityService.InteractionType.COMMENT);
        PostComment saved = commentRepository.save(comment);
        postEventProducer.publishPostCommented(postId, post.getAuthorId(), authorId);
        return commentMapper.toResponse(saved);
    }

    private PostComment fetchCommentById(@NonNull UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(@NonNull UUID commentId) {
        return commentMapper.toResponse(fetchCommentById(commentId));
    }

    @Override
    @Transactional
    public CommentResponse updateComment(@NonNull UUID commentId, UUID authorId, String newContent) {
        userValidationService.validateUserCanInteract(authorId);
        PostComment comment = fetchCommentById(commentId);
        validateOwnership(comment, authorId);

        comment.setContent(newContent);
        comment.setUpdatedAt(LocalDateTime.now());
        return commentMapper.toResponse(commentRepository.save(comment));
    }

    private void validateOwnership(PostComment comment, UUID userId) {
        if (!comment.getUserId().equals(userId)) {
            throw new ForbiddenException("User is not the author of this comment");
        }
    }

    @Override
    @Transactional
    public void deleteComment(@NonNull UUID commentId, @NonNull UUID authorId) {
        userValidationService.validateUserCanInteract(authorId);
        PostComment comment = fetchCommentById(commentId);
        validateOwnership(comment, authorId);

        UUID postId = comment.getPost().getId();
        commentRepository.delete(comment);

        postRepository.incrementCommentCount(postId, -1);
        if (comment.getParentComment() != null) {
            commentRepository.incrementReplyCount(comment.getParentComment().getId(), -1);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(@NonNull UUID postId, Pageable pageable) {
        return commentRepository.findByPostIdAndParentCommentIdIsNullOrderByCreatedAtDesc(postId, pageable)
                .stream().map(commentMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getReplies(@NonNull UUID parentCommentId, Pageable pageable) {
        return commentRepository.findByParentCommentId(parentCommentId, pageable)
                .stream().map(commentMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void pinComment(@NonNull UUID commentId) {
        PostComment comment = fetchCommentById(commentId);
        comment.setIsPinned(true);
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCommentCount(@NonNull UUID postId) {
        return commentRepository.countByPostId(postId);
    }
}
