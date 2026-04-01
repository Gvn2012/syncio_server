package io.github.gvn2012.post_service.controllers;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.services.interfaces.IFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final IFeedService feedService;

    @GetMapping
    public ResponseEntity<APIResource<List<Post>>> getHybridFeed(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestParam(defaultValue = "25") int limit) {
        List<Post> feed = feedService.getHybridFeed(userId, LocalDateTime.now(), limit);
        return ResponseEntity.ok(APIResource.ok("Feed retrieved", feed));
    }

    @GetMapping("/trending")
    public ResponseEntity<APIResource<List<Post>>> getTrendingPosts(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "25") int limit) {
        List<Post> trending = feedService.getTrendingPosts(LocalDateTime.now().minusDays(days), limit);
        return ResponseEntity.ok(APIResource.ok("Trending posts retrieved", trending));
    }

    @GetMapping("/following")
    public ResponseEntity<APIResource<List<Post>>> getFollowingFeed(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestParam(defaultValue = "25") int limit) {
        List<Post> feed = feedService.getFollowingFeed(userId, LocalDateTime.now(), limit);
        return ResponseEntity.ok(APIResource.ok("Following feed retrieved", feed));
    }
}
