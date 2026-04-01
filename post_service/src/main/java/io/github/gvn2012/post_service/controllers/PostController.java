package io.github.gvn2012.post_service.controllers;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.dtos.requests.PostCreateRequest;
import io.github.gvn2012.post_service.dtos.requests.PostUpdateRequest;
import io.github.gvn2012.post_service.dtos.responses.PostResponse;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
import io.github.gvn2012.post_service.services.interfaces.IPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final IPostService postService;

    @PostMapping
    public ResponseEntity<APIResource<PostResponse>> createPost(
            @RequestHeader("X-User-ID") UUID authorId,
            @RequestBody PostCreateRequest request) {
        return ResponseEntity.ok(APIResource.ok("Post created", postService.createPost(request, authorId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResource<PostResponse>> getPost(@PathVariable UUID id) {
        return ResponseEntity.ok(APIResource.ok("Post retrieved", postService.getPostById(id)));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<APIResource<List<PostResponse>>> getPostsByAuthor(
            @PathVariable UUID authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(APIResource.ok("Posts retrieved",
                postService.getPostsByAuthor(authorId, PageRequest.of(page, size))));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<APIResource<List<PostResponse>>> getPostsByStatus(
            @PathVariable PostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(APIResource.ok("Posts retrieved",
                postService.getPostsByStatus(status, PageRequest.of(page, size))));
    }

    @GetMapping("/search")
    public ResponseEntity<APIResource<List<PostResponse>>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(APIResource.ok("Search results",
                postService.searchPosts(keyword, PageRequest.of(page, size))));
    }

    @PatchMapping("/{id}/content")
    public ResponseEntity<APIResource<PostResponse>> updatePostContent(
            @PathVariable UUID id,
            @RequestHeader("X-User-ID") UUID editorId,
            @RequestBody PostUpdateRequest request) {
        return ResponseEntity.ok(APIResource.ok("Post updated",
                postService.updatePostContent(id, editorId, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Void> archivePost(@PathVariable UUID id) {
        postService.archivePost(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/pin")
    public ResponseEntity<APIResource<PostResponse>> pinPost(@PathVariable UUID id) {
        return ResponseEntity.ok(APIResource.ok("Post pinned", postService.pinPost(id)));
    }

    @PatchMapping("/{id}/unpin")
    public ResponseEntity<APIResource<PostResponse>> unpinPost(@PathVariable UUID id) {
        return ResponseEntity.ok(APIResource.ok("Post unpinned", postService.unpinPost(id)));
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<APIResource<PostResponse>> sharePost(
            @PathVariable UUID id,
            @RequestHeader("X-User-ID") UUID sharerId,
            @RequestBody(required = false) String shareContent) {
        return ResponseEntity.ok(APIResource.ok("Post shared",
                postService.sharePost(id, sharerId, shareContent)));
    }
}
