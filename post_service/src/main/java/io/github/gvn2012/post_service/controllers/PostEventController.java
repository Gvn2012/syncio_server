package io.github.gvn2012.post_service.controllers;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.dtos.requests.PostEventRequest;
import io.github.gvn2012.post_service.dtos.responses.PostEventParticipantResponse;
import io.github.gvn2012.post_service.dtos.responses.PostEventResponse;
import io.github.gvn2012.post_service.services.interfaces.IPostEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts/events")
@RequiredArgsConstructor
public class PostEventController {

    private final IPostEventService eventService;

    @PostMapping("/{pid}")
    public ResponseEntity<APIResource<PostEventResponse>> createEvent(
            @PathVariable("pid") UUID postId,
            @RequestBody PostEventRequest event) {
        return ResponseEntity.ok(APIResource.ok("Event created", eventService.createEvent(postId, event)));
    }

    @GetMapping("/{pid}")
    public ResponseEntity<APIResource<PostEventResponse>> getEvent(@PathVariable("pid") UUID postId) {
        return ResponseEntity.ok(APIResource.ok("Event retrieved", eventService.getEventByPostId(postId)));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<APIResource<List<PostEventResponse>>> getUpcomingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(APIResource.ok("Upcoming events",
                eventService.getUpcomingEvents(PageRequest.of(page, size))));
    }

    @PostMapping("/{evid}/respond/{status}")
    public ResponseEntity<Void> respondToEvent(
            @PathVariable("evid") UUID eventId,
            @PathVariable String status,
            @RequestHeader("X-User-ID") UUID userId) {
        eventService.respondToEvent(eventId, userId, status);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{evid}/cancel")
    public ResponseEntity<Void> cancelEvent(
            @PathVariable("evid") UUID eventId,
            @RequestHeader("X-User-ID") UUID userId) {
        eventService.cancelEvent(eventId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{evid}/participants")
    public ResponseEntity<APIResource<List<PostEventParticipantResponse>>> getParticipants(@PathVariable("evid") UUID eventId) {
        return ResponseEntity.ok(APIResource.ok("Participants retrieved",
                eventService.getParticipants(eventId)));
    }
}
