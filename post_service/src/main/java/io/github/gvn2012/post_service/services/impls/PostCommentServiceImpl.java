package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostComment;
import io.github.gvn2012.post_service.exceptions.NotFoundException;
import io.github.gvn2012.post_service.repositories.PostCommentRepository;
import io.github.gvn2012.post_service.repositories.PostRepository;
import io.github.gvn2012.post_service.services.interfaces.IPostCommentService;
import io.github.gvn2012.post_service.services.kafka.PostEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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

    @Override
    @Transactional
    public PostComment addComment(UUID postId, UUID authorId, String content, UUID parentCommentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));
        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setContent(content);
        comment.setUserId(authorId);
        if (parentCommentId != null) {
            PostComment parent = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new NotFoundException("Parent comment not found: " + parentCommentId));
            comment.setParentComment(parent);
        }
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        PostComment saved = commentRepository.save(comment);
        postEventProducer.publishPostCommented(postId, post.getAuthorId(), authorId);
        return saved;
    }

    @Override
    public PostComment getCommentById(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));
    }

    @Override
    @Transactional
    public PostComment updateComment(UUID commentId, UUID authorId, String newContent) {
        PostComment comment = getCommentById(commentId);
        comment.setContent(newContent);
        comment.setUpdatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId, UUID authorId) {
        PostComment comment = getCommentById(commentId);
        Post post = comment.getPost();
        commentRepository.delete(comment);
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        postRepository.save(post);
    }

    @Override
    public List<PostComment> getCommentsByPost(UUID postId, Pageable pageable) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);
    }

    @Override
    public List<PostComment> getReplies(UUID parentCommentId, Pageable pageable) {
        return commentRepository.findByParentCommentId(parentCommentId, pageable);
    }

    @Override
    @Transactional
    public void pinComment(UUID commentId) {
        PostComment comment = getCommentById(commentId);
        comment.setIsPinned(true);
        commentRepository.save(comment);
    }

    @Override
    public long getCommentCount(UUID postId) {
        return commentRepository.countByPostId(postId);
    }
}
