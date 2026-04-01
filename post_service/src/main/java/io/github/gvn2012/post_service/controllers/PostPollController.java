package io.github.gvn2012.post_service.controllers;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.dtos.requests.PostPollRequest;
import io.github.gvn2012.post_service.dtos.responses.PostPollResponse;
import io.github.gvn2012.post_service.services.interfaces.IPostPollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts/polls")
@RequiredArgsConstructor
public class PostPollController {

    private final IPostPollService pollService;

    @PostMapping("/{postId}")
    public ResponseEntity<APIResource<PostPollResponse>> createPoll(
            @PathVariable UUID postId,
            @RequestBody PostPollRequest poll) {
        return ResponseEntity.ok(APIResource.ok("Poll created", pollService.createPoll(postId, poll)));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<APIResource<PostPollResponse>> getPoll(@PathVariable UUID postId) {
        return ResponseEntity.ok(APIResource.ok("Poll retrieved", pollService.getPollByPostId(postId)));
    }

    @PostMapping("/{pollId}/vote/{optionId}")
    public ResponseEntity<Void> voteOnPoll(
            @PathVariable UUID pollId,
            @PathVariable UUID optionId,
            @RequestHeader("X-User-ID") UUID userId) {
        pollService.voteOnPoll(pollId, optionId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{pollId}/results")
    public ResponseEntity<APIResource<PostPollResponse>> getPollResults(@PathVariable UUID pollId) {
        return ResponseEntity.ok(APIResource.ok("Poll results", pollService.getPollResults(pollId)));
    }

    @PostMapping("/{pollId}/close")
    public ResponseEntity<Void> closePoll(@PathVariable UUID pollId) {
        pollService.closePoll(pollId);
        return ResponseEntity.noContent().build();
    }
}
