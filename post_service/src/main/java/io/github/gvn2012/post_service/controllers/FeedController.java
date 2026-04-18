package io.github.gvn2012.post_service.controllers;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.dtos.responses.PostResponse;
import io.github.gvn2012.post_service.services.interfaces.IFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts/feed")
@RequiredArgsConstructor
public class FeedController {

    private final IFeedService feedService;

    @GetMapping
    public ResponseEntity<APIResource<List<PostResponse>>> getHybridFeed(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursor,
            @RequestParam(defaultValue = "10") int limit) {
        List<PostResponse> feed = feedService.getHybridFeed(userId, cursor, limit);
        return ResponseEntity.ok(APIResource.ok("Feed retrieved", feed));
    }

    @GetMapping("/trending")
    public ResponseEntity<APIResource<List<PostResponse>>> getTrending(
            @RequestHeader(value = "X-User-ID", required = false) UUID viewerId,
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "10") int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return ResponseEntity.ok(APIResource.ok("Trending posts",
                feedService.getTrendingPosts(viewerId, since, limit)));
    }

    @GetMapping("/following")
    public ResponseEntity<APIResource<List<PostResponse>>> getFollowingFeed(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursor,
            @RequestParam(defaultValue = "10") int limit) {
        List<PostResponse> feed = feedService.getFollowingFeed(userId, cursor, limit);
        return ResponseEntity.ok(APIResource.ok("Following feed retrieved", feed));
    }
}
