package io.github.gvn2012.post_service.controllers;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.dtos.responses.CommentResponse;
import io.github.gvn2012.post_service.services.interfaces.IPostCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostCommentController {

    private final IPostCommentService commentService;

    @PostMapping("/{postId}/comments")
    public ResponseEntity<APIResource<CommentResponse>> addComment(
            @PathVariable UUID postId,
            @RequestHeader("X-User-Id") UUID authorId,
            @RequestParam String content,
            @RequestParam(required = false) UUID parentId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResource.success(commentService.addComment(postId, authorId, content, parentId)));
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<APIResource<CommentResponse>> getCommentById(@PathVariable UUID commentId) {
        return ResponseEntity.ok(APIResource.success(commentService.getCommentById(commentId)));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<APIResource<CommentResponse>> updateComment(
            @PathVariable UUID commentId,
            @RequestHeader("X-User-Id") UUID authorId,
            @RequestParam String content) {
        return ResponseEntity.ok(APIResource.success(commentService.updateComment(commentId, authorId, content)));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<APIResource<Void>> deleteComment(
            @PathVariable UUID commentId,
            @RequestHeader("X-User-Id") UUID authorId) {
        commentService.deleteComment(commentId, authorId);
        return ResponseEntity.ok(APIResource.success(null));
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<APIResource<List<CommentResponse>>> getCommentsByPost(
            @PathVariable UUID postId,
            Pageable pageable) {
        return ResponseEntity.ok(APIResource.success(commentService.getCommentsByPost(postId, pageable)));
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<APIResource<List<CommentResponse>>> getReplies(
            @PathVariable UUID commentId,
            Pageable pageable) {
        return ResponseEntity.ok(APIResource.success(commentService.getReplies(commentId, pageable)));
    }

    @PostMapping("/comments/{commentId}/pin")
    public ResponseEntity<APIResource<Void>> pinComment(@PathVariable UUID commentId) {
        commentService.pinComment(commentId);
        return ResponseEntity.ok(APIResource.success(null));
    }
}
