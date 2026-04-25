package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.dtos.responses.PostReactionGroupResponse;
import io.github.gvn2012.post_service.dtos.responses.ReactorSummaryResponse;
import io.github.gvn2012.post_service.dtos.responses.UserSummaryResponse;
import io.github.gvn2012.post_service.entities.*;
import io.github.gvn2012.post_service.entities.enums.ReactionType;
import io.github.gvn2012.post_service.exceptions.NotFoundException;
import io.github.gvn2012.post_service.repositories.*;
import io.github.gvn2012.post_service.services.interfaces.IInteractionVelocityService;
import io.github.gvn2012.post_service.services.interfaces.IPostReactionService;
import io.github.gvn2012.post_service.services.kafka.PostEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    private final UserSummaryService userSummaryService;
    private final SocialRelationshipService socialRelationshipService;

    @org.springframework.beans.factory.annotation.Value("${syncio.gateway.host:http://syncio.site}")
    private String gatewayHost;

    @Override
    @Transactional
    public void addPostReaction(UUID postId, UUID userId, ReactionType type) {
        Post post = fetchPostById(postId);
        userValidationService.validateUserCanInteract(userId);
        
        postReactionRepository.save(PostReaction.builder()
                .post(post)
                .userId(userId)
                .reactionType(type)
                .build());
        postRepository.incrementReactionCount(postId, 1);
        
        velocityService.recordInteraction(postId, IInteractionVelocityService.InteractionType.LIKE);
        postEventProducer.publishPostReacted(postId, post.getAuthorId(), userId);
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
                    Post post = existing.getPost();
                    postEventProducer.publishPostReacted(postId, post.getAuthorId(), userId);
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
        PostComment comment = fetchCommentById(commentId);
        userValidationService.validateUserCanInteract(userId);
        postCommentReactionRepository.save(PostCommentReaction.builder()
                .comment(comment)
                .userId(userId)
                .reactionType(type)
                .build());
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

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "postDetailedReactions", key = "#postId.toString() + '-' + #currentUserId.toString()")
    public List<PostReactionGroupResponse> getDetailedPostReactions(UUID postId, UUID currentUserId) {
        List<PostReaction> reactions = postReactionRepository.findByPostId(postId);
        return groupReactions(reactions, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "commentDetailedReactions", key = "#commentId.toString() + '-' + #currentUserId.toString()")
    public List<PostReactionGroupResponse> getDetailedCommentReactions(UUID commentId, UUID currentUserId) {
        List<PostCommentReaction> reactions = postCommentReactionRepository.findByCommentId(commentId);
        return groupReactions(reactions, currentUserId);
    }

    private List<PostReactionGroupResponse> groupReactions(List<?> reactions, UUID currentUserId) {
        if (reactions == null || reactions.isEmpty()) return List.of();

        Set<UUID> reactorIds = reactions.stream()
                .map(r -> {
                    if (r instanceof PostReaction) return ((PostReaction) r).getUserId();
                    if (r instanceof PostCommentReaction) return ((PostCommentReaction) r).getUserId();
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, UserSummaryResponse> userMap = userSummaryService.getSummaries(reactorIds);
        Set<UUID> friendIds = new HashSet<>(socialRelationshipService.getFriendIds(currentUserId));
        Set<UUID> blockedIds = new HashSet<>(socialRelationshipService.getBlockedList(currentUserId));
        Set<UUID> blockedByIds = new HashSet<>(socialRelationshipService.getBlockedByList(currentUserId));

        Map<ReactionType, List<ReactorSummaryResponse>> grouped = reactions.stream()
                .map(r -> {
                    UUID rid;
                    ReactionType type;
                    if (r instanceof PostReaction) {
                        rid = ((PostReaction) r).getUserId();
                        type = ((PostReaction) r).getReactionType();
                    } else if (r instanceof PostCommentReaction) {
                        rid = ((PostCommentReaction) r).getUserId();
                        type = ((PostCommentReaction) r).getReactionType();
                    } else return null;

                    boolean isSelf = rid.equals(currentUserId);
                    boolean isBlocked = !isSelf && (blockedIds.contains(rid) || blockedByIds.contains(rid));
                    
                    UserSummaryResponse summary = userMap.get(rid);
                    boolean isDeleted = !isSelf && (summary == null || !Boolean.TRUE.equals(summary.getActive()));

                    if (isBlocked || isDeleted) {
                        return Map.entry(type, ReactorSummaryResponse.builder()
                                .userId(null)
                                .username("unknown")
                                .fullName("Unknown User")
                                .avatarUrl(null)
                                .isFriend(false)
                                .isBlocked(isBlocked)
                                .build());
                    }

                    String avatarUrl = summary.getAvatarUrl();
                    if (summary.getAvatarPath() != null && !summary.getAvatarPath().isBlank()) {
                        avatarUrl = String.format("%s/api/v1/upload/view?path=%s", gatewayHost, summary.getAvatarPath());
                    }

                    return Map.entry(type, ReactorSummaryResponse.builder()
                            .userId(rid)
                            .username(summary.getUsername())
                            .fullName(summary.getDisplayName())
                            .avatarUrl(avatarUrl)
                            .isFriend(isSelf || friendIds.contains(rid))
                            .isBlocked(false)
                            .build());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        return grouped.entrySet().stream()
                .map(e -> PostReactionGroupResponse.builder()
                        .reactionType(e.getKey())
                        .count(e.getValue().size())
                        .reactors(e.getValue())
                        .build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());
    }

    private Post fetchPostById(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
    }

    private PostComment fetchCommentById(UUID commentId) {
        return postCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));
    }
}
