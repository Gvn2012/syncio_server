package io.github.gvn2012.post_service.controllers;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.dtos.requests.PostAnnouncementRequest;
import io.github.gvn2012.post_service.dtos.responses.PostAnnouncementResponse;
import io.github.gvn2012.post_service.services.interfaces.IPostAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts/announcements")
@RequiredArgsConstructor
public class PostAnnouncementController {

    private final IPostAnnouncementService announcementService;

    @PostMapping("/{pid}")
    public ResponseEntity<APIResource<PostAnnouncementResponse>> createAnnouncement(
            @NonNull @PathVariable("pid") UUID postId,
            @RequestBody PostAnnouncementRequest announcement) {
        return ResponseEntity.ok(APIResource.ok("Announcement created",
                announcementService.createAnnouncement(postId, announcement)));
    }

    @GetMapping("/{pid}")
    public ResponseEntity<APIResource<PostAnnouncementResponse>> getAnnouncement(@NonNull @PathVariable("pid") UUID postId) {
        return ResponseEntity.ok(APIResource.ok("Announcement retrieved",
                announcementService.getAnnouncementByPostId(postId)));
    }

    @PostMapping("/{annid}/read")
    public ResponseEntity<Void> markAsRead(
            @NonNull @PathVariable("annid") UUID announcementId,
            @RequestHeader("X-User-ID") UUID userId) {
        announcementService.markAsRead(announcementId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{annid}/pin")
    public ResponseEntity<Void> pinAnnouncement(
            @NonNull @PathVariable("annid") UUID announcementId,
            @RequestHeader("X-User-ID") UUID userId) {
        announcementService.pinAnnouncement(announcementId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{annid}/unpin")
    public ResponseEntity<Void> unpinAnnouncement(
            @NonNull @PathVariable("annid") UUID announcementId,
            @RequestHeader("X-User-ID") UUID userId) {
        announcementService.unpinAnnouncement(announcementId, userId);
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
