package io.github.gvn2012.post_service.controllers;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.dtos.requests.PostAnnouncementRequest;
import io.github.gvn2012.post_service.dtos.responses.PostAnnouncementResponse;
import io.github.gvn2012.post_service.services.interfaces.IPostAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts/announcements")
@RequiredArgsConstructor
public class PostAnnouncementController {

    private final IPostAnnouncementService announcementService;

    @PostMapping("/{postId}")
    public ResponseEntity<APIResource<PostAnnouncementResponse>> createAnnouncement(
            @PathVariable UUID postId,
            @RequestBody PostAnnouncementRequest announcement) {
        return ResponseEntity.ok(APIResource.ok("Announcement created",
                announcementService.createAnnouncement(postId, announcement)));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<APIResource<PostAnnouncementResponse>> getAnnouncement(@PathVariable UUID postId) {
        return ResponseEntity.ok(APIResource.ok("Announcement retrieved",
                announcementService.getAnnouncementByPostId(postId)));
    }

    @PostMapping("/{announcementId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID announcementId,
            @RequestHeader("X-User-ID") UUID userId) {
        announcementService.markAsRead(announcementId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{announcementId}/pin")
    public ResponseEntity<Void> pinAnnouncement(@PathVariable UUID announcementId) {
        announcementService.pinAnnouncement(announcementId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{announcementId}/unpin")
    public ResponseEntity<Void> unpinAnnouncement(@PathVariable UUID announcementId) {
        announcementService.unpinAnnouncement(announcementId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    public ResponseEntity<APIResource<List<PostAnnouncementResponse>>> getActiveAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(APIResource.ok("Active announcements",
                announcementService.getActiveAnnouncements(PageRequest.of(page, size))));
    }
}
