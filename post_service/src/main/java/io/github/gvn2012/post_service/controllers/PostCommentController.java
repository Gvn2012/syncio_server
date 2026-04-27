package io.github.gvn2012.post_service.controllers;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.dtos.requests.CommentRequest;
import io.github.gvn2012.post_service.dtos.responses.CommentPagedResponse;
import io.github.gvn2012.post_service.dtos.responses.CommentResponse;
import io.github.gvn2012.post_service.services.interfaces.IPostCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResource.success(commentService.addComment(postId, authorId, request.getContent(),
                        request.getParentCommentId())));
    }

    @GetMapping("/{pid}/comments/{cmid}")
    public ResponseEntity<APIResource<CommentResponse>> getCommentById(
            @NonNull @PathVariable("pid") UUID postId,
            @NonNull @PathVariable("cmid") UUID commentId,
            @RequestHeader(value = "X-User-Id", required = false) UUID viewerId) {
        return ResponseEntity.ok(APIResource.success(commentService.getCommentById(postId, commentId, viewerId)));
    }

    @PutMapping("/{pid}/comments/{cmid}")
    public ResponseEntity<APIResource<CommentResponse>> updateComment(
            @NonNull @PathVariable("pid") UUID postId,
            @NonNull @PathVariable("cmid") UUID commentId,
            @RequestHeader("X-User-Id") UUID authorId,
            @RequestBody CommentRequest request) {
        return ResponseEntity.ok(
                APIResource.success(commentService.updateComment(postId, commentId, authorId, request.getContent())));
    }

    @DeleteMapping("/{pid}/comments/{cmid}")
    public ResponseEntity<APIResource<Void>> deleteComment(
            @NonNull @PathVariable("pid") UUID postId,
            @NonNull @PathVariable("cmid") UUID commentId,
            @NonNull @RequestHeader("X-User-Id") UUID authorId) {
        commentService.deleteComment(postId, commentId, authorId);
        return ResponseEntity.ok(APIResource.success(null));
    }

    @GetMapping("/{pid}/comments")
    public ResponseEntity<APIResource<CommentPagedResponse>> getCommentsByPost(
            @NonNull @PathVariable("pid") UUID postId,
            @RequestHeader(value = "X-User-Id", required = false) UUID viewerId,
            @PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(APIResource.success(commentService.getCommentsByPost(postId, viewerId, pageable)));
    }

    @GetMapping("/{pid}/comments/{cmid}/replies")
    public ResponseEntity<APIResource<CommentPagedResponse>> getReplies(
            @NonNull @PathVariable("pid") UUID postId,
            @NonNull @PathVariable("cmid") UUID commentId,
            @RequestHeader(value = "X-User-Id", required = false) UUID viewerId,
            @PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(APIResource.success(commentService.getReplies(postId, commentId, viewerId, pageable)));
    }

    @PostMapping("/{pid}/comments/{cmid}/pin")
    public ResponseEntity<APIResource<Void>> pinComment(
            @NonNull @PathVariable("pid") UUID postId,
            @NonNull @PathVariable("cmid") UUID commentId) {
        commentService.pinComment(postId, commentId);
        return ResponseEntity.ok(APIResource.success(null));
    }
}
