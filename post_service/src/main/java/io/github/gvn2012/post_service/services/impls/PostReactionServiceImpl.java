package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.entities.*;
import io.github.gvn2012.post_service.entities.enums.ReactionType;
import io.github.gvn2012.post_service.exceptions.NotFoundException;
import io.github.gvn2012.post_service.repositories.*;
import io.github.gvn2012.post_service.services.interfaces.IInteractionVelocityService;
import io.github.gvn2012.post_service.services.interfaces.IPostReactionService;
import io.github.gvn2012.post_service.services.kafka.PostEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostReactionServiceImpl implements IPostReactionService {

    private final PostReactionRepository postReactionRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostCommentReactionRepository postCommentReactionRepository;
    private final PostEventProducer postEventProducer;
    private final UserValidationService userValidationService;
    private final IInteractionVelocityService velocityService;

    @Override
    @Transactional
    public void addPostReaction(UUID postId, UUID userId, ReactionType type) {
        Post post = fetchPostById(postId);
        userValidationService.validateCanView(post, userId);

        PostReaction reaction = PostReaction.builder()
                .post(post)
                .userId(userId)
                .reactionType(type)
                .build();
        postReactionRepository.save(reaction);

        postRepository.incrementReactionCount(postId, 1);
        velocityService.recordInteraction(postId, IInteractionVelocityService.InteractionType.LIKE);
        postEventProducer.publishPostReacted(postId, post.getAuthorId(), userId);
    }

    private Post fetchPostById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: " + id));
    }

    @Override
    @Transactional
    public void removePostReaction(UUID postId, UUID userId) {
        postReactionRepository.deleteByPostIdAndUserId(postId, userId);
        postRepository.incrementReactionCount(postId, -1);
    }

    @Override
    @Transactional
    public void toggleReaction(UUID postId, UUID userId, ReactionType type) {
        userValidationService.validateUserCanInteract(userId);
        
        postReactionRepository.findByPostIdAndUserId(postId, userId).ifPresentOrElse(
            existing -> {
                if (existing.getReactionType() == type) {
                    removePostReaction(postId, userId);
                } else {
                    existing.setReactionType(type);
                    postReactionRepository.save(existing);
                    velocityService.recordInteraction(postId, IInteractionVelocityService.InteractionType.LIKE);
                }
            },
            () -> addPostReaction(postId, userId, type)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostReaction> getReactionsByPost(UUID postId) {
        return postReactionRepository.findByPostId(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReacted(UUID postId, UUID userId) {
        return postReactionRepository.existsByPostIdAndUserId(postId, userId);
    }

    @Override
    @Transactional
    public void addCommentReaction(UUID commentId, UUID userId, ReactionType type) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));
        userValidationService.validateNotBlocked(comment.getUserId(), userId);

        PostCommentReaction reaction = PostCommentReaction.builder()
                .comment(comment)
                .userId(userId)
                .reactionType(type)
                .build();
        postCommentReactionRepository.save(reaction);

        postCommentRepository.incrementReactionCount(commentId, 1);
    }

    @Override
    @Transactional
    public void removeCommentReaction(UUID commentId, UUID userId) {
        postCommentReactionRepository.deleteByCommentIdAndUserId(commentId, userId);
        postCommentRepository.incrementReactionCount(commentId, -1);
    }

    @Override
    @Transactional
    public void toggleCommentReaction(UUID commentId, UUID userId, ReactionType type) {
        userValidationService.validateUserCanInteract(userId);
        
        postCommentReactionRepository.findByCommentIdAndUserId(commentId, userId).ifPresentOrElse(
            existing -> {
                if (existing.getReactionType() == type) {
                    removeCommentReaction(commentId, userId);
                } else {
                    existing.setReactionType(type);
                    postCommentReactionRepository.save(existing);
                }
            },
            () -> addCommentReaction(commentId, userId, type)
        );
    }
}
