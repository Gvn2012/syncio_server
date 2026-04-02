package io.github.gvn2012.post_service.controllers;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.dtos.responses.CommentResponse;
import io.github.gvn2012.post_service.services.interfaces.IPostCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostCommentController {

    private final IPostCommentService commentService;

    @PostMapping("/{pid}/comments")
    public ResponseEntity<APIResource<CommentResponse>> addComment(
            @NonNull @PathVariable("pid") UUID postId,
            @RequestHeader("X-User-Id") UUID authorId,
            @RequestParam String content,
            @RequestParam(required = false) UUID parentId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResource.success(commentService.addComment(postId, authorId, content, parentId)));
    }

    @GetMapping("/comments/{cmid}")
    public ResponseEntity<APIResource<CommentResponse>> getCommentById(@NonNull @PathVariable("cmid") UUID commentId) {
        return ResponseEntity.ok(APIResource.success(commentService.getCommentById(commentId)));
    }

    @PutMapping("/comments/{cmid}")
    public ResponseEntity<APIResource<CommentResponse>> updateComment(
            @NonNull @PathVariable("cmid") UUID commentId,
            @RequestHeader("X-User-Id") UUID authorId,
            @RequestParam String content) {
        return ResponseEntity.ok(APIResource.success(commentService.updateComment(commentId, authorId, content)));
    }

    @DeleteMapping("/comments/{cmid}")
    public ResponseEntity<APIResource<Void>> deleteComment(
            @NonNull @PathVariable("cmid") UUID commentId,
            @NonNull @RequestHeader("X-User-Id") UUID authorId) {
        commentService.deleteComment(commentId, authorId);
        return ResponseEntity.ok(APIResource.success(null));
    }

    @GetMapping("/{pid}/comments")
    public ResponseEntity<APIResource<List<CommentResponse>>> getCommentsByPost(
            @NonNull @PathVariable("pid") UUID postId,
            Pageable pageable) {
        return ResponseEntity.ok(APIResource.success(commentService.getCommentsByPost(postId, pageable)));
    }

    @GetMapping("/comments/{cmid}/replies")
    public ResponseEntity<APIResource<List<CommentResponse>>> getReplies(
            @NonNull @PathVariable("cmid") UUID commentId,
            Pageable pageable) {
        return ResponseEntity.ok(APIResource.success(commentService.getReplies(commentId, pageable)));
    }

    @PostMapping("/comments/{cmid}/pin")
    public ResponseEntity<APIResource<Void>> pinComment(@NonNull @PathVariable("cmid") UUID commentId) {
        commentService.pinComment(commentId);
        return ResponseEntity.ok(APIResource.success(null));
    }
}
