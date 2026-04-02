package io.github.gvn2012.post_service.controllers;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.services.interfaces.IPostReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts/reactions")
@RequiredArgsConstructor
public class PostReactionController {

    private final IPostReactionService postReactionService;

    @PostMapping("/{pid}/toggle")
    public ResponseEntity<APIResource<Void>> togglePostReaction(
            @PathVariable("pid") UUID postId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam Short typeId) {
        postReactionService.toggleReaction(postId, userId, typeId);
        return ResponseEntity.ok(APIResource.success(null));
    }

    @PostMapping("/comments/{cmid}/toggle")
    public ResponseEntity<APIResource<Void>> toggleCommentReaction(
            @PathVariable("cmid") UUID commentId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam Short typeId) {
        postReactionService.toggleCommentReaction(commentId, userId, typeId);
        return ResponseEntity.ok(APIResource.success(null));
    }
}
