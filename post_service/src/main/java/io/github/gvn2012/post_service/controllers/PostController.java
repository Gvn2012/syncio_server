package io.github.gvn2012.post_service.controllers;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.dtos.requests.PostCreateRequest;
import io.github.gvn2012.post_service.dtos.requests.PostUpdateRequest;
import io.github.gvn2012.post_service.dtos.responses.PostResponse;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
import io.github.gvn2012.post_service.services.interfaces.IPostService;
import io.github.gvn2012.post_service.services.interfaces.ISimilarityService;
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
    private final ISimilarityService similarityService;

    @GetMapping("/{pid}/similar")
    public ResponseEntity<APIResource<List<PostResponse>>> getSimilarPosts(
            @PathVariable("pid") UUID id,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(APIResource.ok("Similar posts retrieved", 
                similarityService.findSimilarPosts(id, limit)));
    }

    @PostMapping
    public ResponseEntity<APIResource<PostResponse>> createPost(
            @RequestHeader("X-User-ID") UUID authorId,
            @RequestBody PostCreateRequest request) {
        return ResponseEntity.ok(APIResource.ok("Post created", postService.createPost(request, authorId)));
    }

    @GetMapping("/{pid}")
    public ResponseEntity<APIResource<PostResponse>> getPost(
            @RequestHeader(value = "X-User-ID", required = false) UUID viewerId,
            @PathVariable("pid") UUID id) {
        return ResponseEntity.ok(APIResource.ok("Post retrieved", postService.getPostById(id, viewerId)));
    }

    @GetMapping("/author/{uid}")
    public ResponseEntity<APIResource<List<PostResponse>>> getPostsByAuthor(
            @RequestHeader(value = "X-User-ID", required = false) UUID viewerId,
            @PathVariable("uid") UUID authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(APIResource.ok("Posts retrieved",
                postService.getPostsByAuthor(authorId, viewerId, PageRequest.of(page, size))));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<APIResource<List<PostResponse>>> getPostsByStatus(
            @RequestHeader(value = "X-User-ID", required = false) UUID viewerId,
            @PathVariable PostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(APIResource.ok("Posts retrieved",
                postService.getPostsByStatus(status, viewerId, PageRequest.of(page, size))));
    }

    @GetMapping("/search")
    public ResponseEntity<APIResource<List<PostResponse>>> searchPosts(
            @RequestHeader(value = "X-User-ID", required = false) UUID viewerId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(APIResource.ok("Search results",
                postService.searchPosts(keyword, viewerId, PageRequest.of(page, size))));
    }

    @PatchMapping("/{pid}/content")
    public ResponseEntity<APIResource<PostResponse>> updatePostContent(
            @PathVariable("pid") UUID id,
            @RequestHeader("X-User-ID") UUID editorId,
            @RequestBody PostUpdateRequest request) {
        return ResponseEntity.ok(APIResource.ok("Post updated",
                postService.updatePostContent(id, editorId, request)));
    }

    @DeleteMapping("/{pid}")
    public ResponseEntity<Void> deletePost(
            @PathVariable("pid") UUID id,
            @RequestHeader("X-User-ID") UUID userId) {
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{pid}/archive")
    public ResponseEntity<Void> archivePost(
            @PathVariable("pid") UUID id,
            @RequestHeader("X-User-ID") UUID userId) {
        postService.archivePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{pid}/pin")
    public ResponseEntity<APIResource<PostResponse>> pinPost(
            @PathVariable("pid") UUID id,
            @RequestHeader("X-User-ID") UUID userId) {
        return ResponseEntity.ok(APIResource.ok("Post pinned", postService.pinPost(id, userId)));
    }

    @PatchMapping("/{pid}/unpin")
    public ResponseEntity<APIResource<PostResponse>> unpinPost(
            @PathVariable("pid") UUID id,
            @RequestHeader("X-User-ID") UUID userId) {
        return ResponseEntity.ok(APIResource.ok("Post unpinned", postService.unpinPost(id, userId)));
    }

    @PostMapping("/{pid}/share")
    public ResponseEntity<APIResource<PostResponse>> sharePost(
            @PathVariable("pid") UUID id,
            @RequestHeader("X-User-ID") UUID sharerId,
            @RequestBody(required = false) String shareContent) {
        return ResponseEntity.ok(APIResource.ok("Post shared",
                postService.sharePost(id, sharerId, shareContent)));
    }
}
