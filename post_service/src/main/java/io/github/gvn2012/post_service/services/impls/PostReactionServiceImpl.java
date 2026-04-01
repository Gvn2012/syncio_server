package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostReaction;
import io.github.gvn2012.post_service.entities.ReactionType;
import io.github.gvn2012.post_service.exceptions.NotFoundException;
import io.github.gvn2012.post_service.repositories.PostReactionRepository;
import io.github.gvn2012.post_service.repositories.PostRepository;
import io.github.gvn2012.post_service.repositories.ReactionTypeRepository;
import io.github.gvn2012.post_service.services.interfaces.IPostReactionService;
import io.github.gvn2012.post_service.services.kafka.PostEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostReactionServiceImpl implements IPostReactionService {

    private final PostReactionRepository postReactionRepository;
    private final PostRepository postRepository;
    private final ReactionTypeRepository reactionTypeRepository;
    private final PostEventProducer postEventProducer;

    @Override
    @Transactional
    public void addPostReaction(UUID postId, UUID userId, Short reactionTypeId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));
        ReactionType type = reactionTypeRepository.findById(reactionTypeId)
                .orElseThrow(() -> new NotFoundException("Reaction type not found: " + reactionTypeId));

        PostReaction reaction = new PostReaction();
        reaction.setPost(post);
        reaction.setUserId(userId);
        reaction.setReactionType(type);
        postReactionRepository.save(reaction);

        post.setReactionCount(post.getReactionCount() + 1);
        postRepository.save(post);
        postEventProducer.publishPostReacted(postId, post.getAuthorId(), userId);
    }

    @Override
    @Transactional
    public void removePostReaction(UUID postId, UUID userId) {
        postReactionRepository.deleteByPostIdAndUserId(postId, userId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));
        post.setReactionCount(Math.max(0, post.getReactionCount() - 1));
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void toggleReaction(UUID postId, UUID userId, Short reactionTypeId) {
        if (hasUserReacted(postId, userId)) {
            removePostReaction(postId, userId);
        } else {
            addPostReaction(postId, userId, reactionTypeId);
        }
    }

    @Override
    public List<PostReaction> getReactionsByPost(UUID postId) {
        return postReactionRepository.findByPostId(postId);
    }

    @Override
    public boolean hasUserReacted(UUID postId, UUID userId) {
        return postReactionRepository.existsByPostIdAndUserId(postId, userId);
    }

    @Override
    @Transactional
    public void addCommentReaction(UUID commentId, UUID userId, Short reactionTypeId) {
        ReactionType type = reactionTypeRepository.findById(reactionTypeId)
                .orElseThrow(() -> new NotFoundException("Reaction type not found: " + reactionTypeId));
    }

    @Override
    @Transactional
    public void removeCommentReaction(UUID commentId, UUID userId) {
    }
}
